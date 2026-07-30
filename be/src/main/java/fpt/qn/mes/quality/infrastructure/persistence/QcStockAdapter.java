package fpt.qn.mes.quality.infrastructure.persistence;

import static fpt.qn.mes.jooq.Tables.MOVEMENT_TYPES;
import static fpt.qn.mes.jooq.Tables.STOCK_BALANCES;
import static fpt.qn.mes.jooq.Tables.STOCK_MOVEMENTS;
import static fpt.qn.mes.jooq.Tables.STOCK_STATUSES;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import fpt.qn.mes.jooq.tables.records.StockMovementsRecord;
import fpt.qn.mes.quality.application.port.out.QcStockPort;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Repository
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class QcStockAdapter implements QcStockPort {

    DSLContext dslCtx;

    @Override
    public UUID transferStock(UUID lotId, UUID productId, BigDecimal quantity,
            UUID fromStatusId, UUID toStatusId, UUID movementTypeId,
            UUID workOrderId, UUID inspectorId) {

        // Decrease stock from status (QUALITY_INSPECTION)
        int decreased = dslCtx.update(STOCK_BALANCES)
            .set(STOCK_BALANCES.QUANTITY, STOCK_BALANCES.QUANTITY.subtract(quantity))
            .set(STOCK_BALANCES.UPDATED_AT, OffsetDateTime.now())
            .where(STOCK_BALANCES.LOT_ID.eq(lotId))
            .and(STOCK_BALANCES.PRODUCT_ID.eq(productId))
            .and(STOCK_BALANCES.STOCK_STATUS_ID.eq(fromStatusId))
            .and(STOCK_BALANCES.QUANTITY.greaterOrEqual(quantity))
            .execute();

        if (decreased == 0) {
            // Try to find the balance row — might not exist
            var existing = dslCtx.selectFrom(STOCK_BALANCES)
                .where(STOCK_BALANCES.LOT_ID.eq(lotId))
                .and(STOCK_BALANCES.PRODUCT_ID.eq(productId))
                .and(STOCK_BALANCES.STOCK_STATUS_ID.eq(fromStatusId))
                .fetchOne();

            if (existing == null || existing.getQuantity().compareTo(quantity) < 0) {
                throw new IllegalStateException("Insufficient stock in QUALITY_INSPECTION status");
            }
        }

        // Increase or create stock in target status
        var targetBalance = dslCtx.selectFrom(STOCK_BALANCES)
            .where(STOCK_BALANCES.LOT_ID.eq(lotId))
            .and(STOCK_BALANCES.PRODUCT_ID.eq(productId))
            .and(STOCK_BALANCES.STOCK_STATUS_ID.eq(toStatusId))
            .fetchOne();

        if (targetBalance != null) {
            dslCtx.update(STOCK_BALANCES)
                .set(STOCK_BALANCES.QUANTITY, STOCK_BALANCES.QUANTITY.add(quantity))
                .set(STOCK_BALANCES.UPDATED_AT, OffsetDateTime.now())
                .where(STOCK_BALANCES.ID.eq(targetBalance.getId()))
                .execute();
        } else {
            // Needs warehouse_id and location_id from the source balance
            var sourceBalance = dslCtx.selectFrom(STOCK_BALANCES)
                .where(STOCK_BALANCES.LOT_ID.eq(lotId))
                .and(STOCK_BALANCES.PRODUCT_ID.eq(productId))
                .and(STOCK_BALANCES.STOCK_STATUS_ID.eq(fromStatusId))
                .limit(1)
                .fetchOne();

            dslCtx.insertInto(STOCK_BALANCES)
                .columns(
                    STOCK_BALANCES.ID, STOCK_BALANCES.WAREHOUSE_ID, STOCK_BALANCES.LOCATION_ID,
                    STOCK_BALANCES.PRODUCT_ID, STOCK_BALANCES.LOT_ID, STOCK_BALANCES.STOCK_STATUS_ID,
                    STOCK_BALANCES.QUANTITY, STOCK_BALANCES.CREATED_AT, STOCK_BALANCES.UPDATED_AT
                )
                .values(
                    UUID.randomUUID(),
                    sourceBalance != null ? sourceBalance.getWarehouseId() : null,
                    sourceBalance != null ? sourceBalance.getLocationId() : null,
                    productId, lotId, toStatusId,
                    quantity, OffsetDateTime.now(), OffsetDateTime.now()
                )
                .execute();
        }

        // Create stock movement record
        StockMovementsRecord movement = new StockMovementsRecord();
        UUID movementId = UUID.randomUUID();
        movement.setId(movementId);
        movement.setMovementTypeId(movementTypeId);
        movement.setProductId(productId);
        movement.setLotId(lotId);
        movement.setWorkOrderId(workOrderId);
        movement.setQuantity(quantity);
        movement.setFromStatusId(fromStatusId);
        movement.setToStatusId(toStatusId);
        movement.setCreatedBy(inspectorId);
        movement.setCreatedAt(OffsetDateTime.now());
        dslCtx.insertInto(STOCK_MOVEMENTS).set(movement).execute();

        return movementId;
    }

    @Override
    public UUID getQualityInspectionStatusId() {
        return dslCtx.selectFrom(STOCK_STATUSES)
            .where(STOCK_STATUSES.NAME.eq("QUALITY_INSPECTION"))
            .fetchOptional(r -> r.getId())
            .orElseThrow(() -> new IllegalStateException("QUALITY_INSPECTION stock status not found"));
    }

    @Override
    public UUID getAvailableStatusId() {
        return dslCtx.selectFrom(STOCK_STATUSES)
            .where(STOCK_STATUSES.NAME.eq("AVAILABLE"))
            .fetchOptional(r -> r.getId())
            .orElseThrow(() -> new IllegalStateException("AVAILABLE stock status not found"));
    }

    @Override
    public UUID getOnHoldStatusId() {
        return dslCtx.selectFrom(STOCK_STATUSES)
            .where(STOCK_STATUSES.NAME.eq("ON_HOLD"))
            .fetchOptional(r -> r.getId())
            .orElseThrow(() -> new IllegalStateException("ON_HOLD stock status not found"));
    }

    @Override
    public UUID getScrappedStatusId() {
        return dslCtx.selectFrom(STOCK_STATUSES)
            .where(STOCK_STATUSES.NAME.eq("SCRAPPED"))
            .fetchOptional(r -> r.getId())
            .orElseThrow(() -> new IllegalStateException("SCRAPPED stock status not found"));
    }

    @Override
    public UUID getQcReleaseMovementTypeId() {
        return dslCtx.selectFrom(MOVEMENT_TYPES)
            .where(MOVEMENT_TYPES.NAME.eq("QC_RELEASE"))
            .fetchOptional(r -> r.getId())
            .orElseThrow(() -> new IllegalStateException("QC_RELEASE movement type not found"));
    }

    @Override
    public UUID getQcHoldMovementTypeId() {
        return dslCtx.selectFrom(MOVEMENT_TYPES)
            .where(MOVEMENT_TYPES.NAME.eq("QC_HOLD"))
            .fetchOptional(r -> r.getId())
            .orElseThrow(() -> new IllegalStateException("QC_HOLD movement type not found"));
    }

    @Override
    public UUID getScrapMovementTypeId() {
        return dslCtx.selectFrom(MOVEMENT_TYPES)
            .where(MOVEMENT_TYPES.NAME.eq("SCRAP"))
            .fetchOptional(r -> r.getId())
            .orElseThrow(() -> new IllegalStateException("SCRAP movement type not found"));
    }
}
