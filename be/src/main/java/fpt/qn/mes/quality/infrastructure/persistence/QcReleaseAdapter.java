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

import fpt.qn.mes.common.util.UuidV7;
import fpt.qn.mes.inventory.domain.constants.MovementTypeConstants;
import fpt.qn.mes.inventory.domain.constants.StockStatusConstants;
import fpt.qn.mes.jooq.tables.records.StockMovementsRecord;
import fpt.qn.mes.quality.application.port.out.QcReleasePort;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Repository
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class QcReleaseAdapter implements QcReleasePort {

    DSLContext ctx;

    @Override
    public void releasePassedStock(UUID lotId, UUID productId, BigDecimal quantity, UUID workOrderId, UUID inspectorId) {
        UUID fromStatusId  = resolveStatusId(StockStatusConstants.QUALITY_INSPECTION);
        UUID toStatusId    = resolveStatusId(StockStatusConstants.AVAILABLE);
        UUID movementTypeId = resolveMovementTypeId(MovementTypeConstants.QC_RELEASE);

        // decrease stock in QUALITY_INSPECTION status
        int decreased = ctx.update(STOCK_BALANCES)
            .set(STOCK_BALANCES.QUANTITY, STOCK_BALANCES.QUANTITY.subtract(quantity))
            .set(STOCK_BALANCES.UPDATED_AT, OffsetDateTime.now())
            .where(STOCK_BALANCES.LOT_ID.eq(lotId))
            .and(STOCK_BALANCES.PRODUCT_ID.eq(productId))
            .and(STOCK_BALANCES.STOCK_STATUS_ID.eq(fromStatusId))
            .and(STOCK_BALANCES.QUANTITY.greaterOrEqual(quantity))
            .execute();

        if (decreased == 0) {
            throw new IllegalStateException(
                "Insufficient stock in QUALITY_INSPECTION status for lot " + lotId);
        }

        // get available stock in AVAILABLE status, if exists, then increase quantity, else insert new record
        var available = ctx.selectFrom(STOCK_BALANCES)
            .where(STOCK_BALANCES.LOT_ID.eq(lotId))
            .and(STOCK_BALANCES.PRODUCT_ID.eq(productId))
            .and(STOCK_BALANCES.STOCK_STATUS_ID.eq(toStatusId))
            .fetchOne();

        if (available != null) {
            ctx.update(STOCK_BALANCES)
                .set(STOCK_BALANCES.QUANTITY, STOCK_BALANCES.QUANTITY.add(quantity))
                .set(STOCK_BALANCES.UPDATED_AT, OffsetDateTime.now())
                .where(STOCK_BALANCES.ID.eq(available.getId()))
                .execute();
        } else {
            var source = ctx.selectFrom(STOCK_BALANCES)
                .where(STOCK_BALANCES.LOT_ID.eq(lotId))
                .and(STOCK_BALANCES.PRODUCT_ID.eq(productId))
                .and(STOCK_BALANCES.STOCK_STATUS_ID.eq(fromStatusId))
                .limit(1)
                .fetchOneInto(STOCK_BALANCES);

            ctx.insertInto(STOCK_BALANCES)
                .columns(
                    STOCK_BALANCES.ID, STOCK_BALANCES.WAREHOUSE_ID, STOCK_BALANCES.LOCATION_ID,
                    STOCK_BALANCES.PRODUCT_ID, STOCK_BALANCES.LOT_ID, STOCK_BALANCES.STOCK_STATUS_ID,
                    STOCK_BALANCES.QUANTITY, STOCK_BALANCES.CREATED_AT, STOCK_BALANCES.UPDATED_AT)
                .values(
                    UuidV7.generate(),
                    source.getWarehouseId(),
                    source.getLocationId(),
                    productId, lotId, toStatusId,
                    quantity, OffsetDateTime.now(), OffsetDateTime.now())
                .execute();
        }

        StockMovementsRecord movement = new StockMovementsRecord();
        movement.setId(UuidV7.generate());
        movement.setMovementTypeId(movementTypeId);
        movement.setProductId(productId);
        movement.setLotId(lotId);
        movement.setWorkOrderId(workOrderId);
        movement.setQuantity(quantity);
        movement.setFromStatusId(fromStatusId);
        movement.setToStatusId(toStatusId);
        movement.setCreatedBy(inspectorId);
        movement.setCreatedAt(OffsetDateTime.now());
        ctx.insertInto(STOCK_MOVEMENTS).set(movement).execute();
    }

    private UUID resolveStatusId(String name) {
        return ctx.selectFrom(STOCK_STATUSES)
            .where(STOCK_STATUSES.NAME.eq(name))
            .fetchOptional(r -> r.getId())
            .orElseThrow(() -> new IllegalStateException(name + " stock status not found"));
    }

    private UUID resolveMovementTypeId(String name) {
        return ctx.selectFrom(MOVEMENT_TYPES)
            .where(MOVEMENT_TYPES.NAME.eq(name))
            .fetchOptional(r -> r.getId())
            .orElseThrow(() -> new IllegalStateException(name + " movement type not found"));
    }
}
