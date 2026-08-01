package fpt.qn.mes.workorder.infrastructure.persistence.adapter;

import static fpt.qn.mes.jooq.Tables.MOVEMENT_TYPES;
import static fpt.qn.mes.jooq.Tables.STOCK_BALANCES;
import static fpt.qn.mes.jooq.Tables.STOCK_LOTS;
import static fpt.qn.mes.jooq.Tables.STOCK_MOVEMENTS;
import static fpt.qn.mes.jooq.Tables.STOCK_STATUSES;
import static fpt.qn.mes.jooq.Tables.WORK_ORDER_MATERIALS;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import fpt.qn.mes.common.util.UuidV7;
import fpt.qn.mes.workorder.application.port.out.dto.ReservationAllocation;
import fpt.qn.mes.workorder.application.port.out.dto.ReservationStock;
import fpt.qn.mes.workorder.application.port.out.WorkOrderReservationPort;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Repository
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class WorkOrderReservationPersistenceAdapter implements WorkOrderReservationPort {

    DSLContext ctx;

    @Override
    public List<ReservationStock> findAvailableStock(UUID warehouseId, Collection<UUID> productIds,
            UUID availableStatusId) {
        if (productIds == null || productIds.isEmpty()) {
            return List.of();
        }

        return ctx.select(
                        STOCK_BALANCES.ID,
                        STOCK_BALANCES.WAREHOUSE_ID,
                        STOCK_BALANCES.LOCATION_ID,
                        STOCK_BALANCES.PRODUCT_ID,
                        STOCK_BALANCES.LOT_ID,
                        STOCK_BALANCES.QUANTITY,
                        STOCK_LOTS.CREATED_AT)
                .from(STOCK_BALANCES)
                .join(STOCK_LOTS).on(STOCK_LOTS.ID.eq(STOCK_BALANCES.LOT_ID))
                .where(STOCK_BALANCES.WAREHOUSE_ID.eq(warehouseId))
                .and(STOCK_BALANCES.PRODUCT_ID.in(productIds))
                .and(STOCK_BALANCES.STOCK_STATUS_ID.eq(availableStatusId))
                .and(STOCK_BALANCES.QUANTITY.gt(BigDecimal.ZERO))
                .orderBy(STOCK_BALANCES.PRODUCT_ID.asc(), STOCK_LOTS.CREATED_AT.asc(),
                        STOCK_LOTS.ID.asc(), STOCK_BALANCES.LOCATION_ID.asc())
                .forUpdate()
                .fetch(record -> new ReservationStock(
                        record.get(STOCK_BALANCES.ID),
                        record.get(STOCK_BALANCES.WAREHOUSE_ID),
                        record.get(STOCK_BALANCES.LOCATION_ID),
                        record.get(STOCK_BALANCES.PRODUCT_ID),
                        record.get(STOCK_BALANCES.LOT_ID),
                        record.get(STOCK_BALANCES.QUANTITY),
                        record.get(STOCK_LOTS.CREATED_AT) == null
                                ? null
                                : record.get(STOCK_LOTS.CREATED_AT).toInstant()));
    }

    @Override
    public UUID findStockStatusId(String statusName) {
        return ctx.select(STOCK_STATUSES.ID)
                .from(STOCK_STATUSES)
                .where(STOCK_STATUSES.NAME.eq(statusName))
                .fetchOptionalInto(UUID.class)
                .orElse(null);
    }

    @Override
    public UUID findMovementTypeId(String movementTypeName) {
        return ctx.select(MOVEMENT_TYPES.ID)
                .from(MOVEMENT_TYPES)
                .where(MOVEMENT_TYPES.NAME.eq(movementTypeName))
                .fetchOptionalInto(UUID.class)
                .orElse(null);
    }

    @Override
    public void applyReservation(UUID workOrderId, List<ReservationAllocation> allocations,
            Map<UUID, BigDecimal> reservedByProduct,
            UUID availableStatusId, UUID reservedStatusId, UUID reserveMovementTypeId, UUID actorId) {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        for (ReservationAllocation allocation : allocations) {
            // Update stock balances
            ctx.update(STOCK_BALANCES)
                    .set(STOCK_BALANCES.QUANTITY, STOCK_BALANCES.QUANTITY.subtract(allocation.quantity()))
                    .set(STOCK_BALANCES.VERSION, STOCK_BALANCES.VERSION.add(1L))
                    .set(STOCK_BALANCES.UPDATED_AT, now)
                    .where(STOCK_BALANCES.ID.eq(allocation.balanceId()))
                    .and(STOCK_BALANCES.STOCK_STATUS_ID.eq(availableStatusId))
                    .and(STOCK_BALANCES.QUANTITY.ge(allocation.quantity()))
                    .execute();

            // Increase stock balances
            ctx.insertInto(STOCK_BALANCES)
                    .columns(STOCK_BALANCES.ID, STOCK_BALANCES.WAREHOUSE_ID, STOCK_BALANCES.LOCATION_ID,
                            STOCK_BALANCES.PRODUCT_ID, STOCK_BALANCES.LOT_ID, STOCK_BALANCES.STOCK_STATUS_ID,
                            STOCK_BALANCES.QUANTITY, STOCK_BALANCES.VERSION, STOCK_BALANCES.CREATED_AT,
                            STOCK_BALANCES.UPDATED_AT)
                    .values(UuidV7.generate(), allocation.warehouseId(), allocation.locationId(),
                            allocation.productId(), allocation.lotId(), reservedStatusId, allocation.quantity(),
                            0L, now, now)
                    .onConflict(STOCK_BALANCES.WAREHOUSE_ID, STOCK_BALANCES.LOCATION_ID,
                            STOCK_BALANCES.PRODUCT_ID, STOCK_BALANCES.LOT_ID, STOCK_BALANCES.STOCK_STATUS_ID)
                    .doUpdate()
                    .set(STOCK_BALANCES.QUANTITY, STOCK_BALANCES.QUANTITY.add(allocation.quantity()))
                    .set(STOCK_BALANCES.VERSION, STOCK_BALANCES.VERSION.add(1L))
                    .set(STOCK_BALANCES.UPDATED_AT, now)
                    .execute();

            // Write log to stock movements
            ctx.insertInto(STOCK_MOVEMENTS)
                    .columns(STOCK_MOVEMENTS.ID, STOCK_MOVEMENTS.MOVEMENT_TYPE_ID, STOCK_MOVEMENTS.PRODUCT_ID,
                            STOCK_MOVEMENTS.LOT_ID, STOCK_MOVEMENTS.WORK_ORDER_ID,
                            STOCK_MOVEMENTS.FROM_WAREHOUSE_ID, STOCK_MOVEMENTS.FROM_LOCATION_ID,
                            STOCK_MOVEMENTS.TO_WAREHOUSE_ID, STOCK_MOVEMENTS.TO_LOCATION_ID,
                            STOCK_MOVEMENTS.QUANTITY, STOCK_MOVEMENTS.FROM_STATUS_ID, STOCK_MOVEMENTS.TO_STATUS_ID,
                            STOCK_MOVEMENTS.REASON, STOCK_MOVEMENTS.CREATED_BY)
                    .values(UuidV7.generate(), reserveMovementTypeId, allocation.productId(), allocation.lotId(),
                            workOrderId, allocation.warehouseId(), allocation.locationId(), allocation.warehouseId(),
                            allocation.locationId(), allocation.quantity(), availableStatusId, reservedStatusId,
                            "Work Order material reservation", actorId)
                    .execute();
        }

        // Update quantity
        reservedByProduct.forEach((productId, quantity) ->
                ctx.update(WORK_ORDER_MATERIALS)
                        .set(WORK_ORDER_MATERIALS.RESERVED_QUANTITY,
                                WORK_ORDER_MATERIALS.RESERVED_QUANTITY.add(quantity))
                        .where(WORK_ORDER_MATERIALS.WORK_ORDER_ID.eq(workOrderId))
                        .and(WORK_ORDER_MATERIALS.MATERIAL_PRODUCT_ID.eq(productId))
                        .execute());
    }

    @Override
    public boolean releaseReservation(UUID workOrderId, UUID availableStatusId, UUID reservedStatusId,
            UUID releaseMovementTypeId, UUID actorId) {
        // A release can be retried, so reconstruct the outstanding amount from immutable movement history.
        UUID reserveMovementTypeId = findMovementTypeId("RESERVE");
        if (reserveMovementTypeId == null) {
            return false;
        }
        var reserveMovements = ctx.selectFrom(STOCK_MOVEMENTS)
                .where(STOCK_MOVEMENTS.WORK_ORDER_ID.eq(workOrderId))
                .and(STOCK_MOVEMENTS.MOVEMENT_TYPE_ID.eq(reserveMovementTypeId))
                .orderBy(STOCK_MOVEMENTS.CREATED_AT.asc(), STOCK_MOVEMENTS.ID.asc())
                .fetch();
        UUID consumeMovementTypeId = findMovementTypeId("CONSUME_IN_PRODUCTION");
        UUID scrapMovementTypeId = findMovementTypeId("SCRAP");
        var releaseMovements = ctx.selectFrom(STOCK_MOVEMENTS)
                .where(STOCK_MOVEMENTS.WORK_ORDER_ID.eq(workOrderId))
                .and(STOCK_MOVEMENTS.MOVEMENT_TYPE_ID.in(releaseMovementTypeId, consumeMovementTypeId, scrapMovementTypeId))
                .fetch();

        // Track the net outstanding reservation for every original warehouse, location, product, and lot.
        Map<String, BigDecimal> remainingByStockKey = new HashMap<>();
        for (var movement : reserveMovements) {
            remainingByStockKey.merge(stockKey(movement.getFromWarehouseId(), movement.getFromLocationId(),
                    movement.getProductId(), movement.getLotId()), movement.getQuantity(), BigDecimal::add);
        }
        for (var movement : releaseMovements) {
            String key = stockKey(movement.getFromWarehouseId(), movement.getFromLocationId(),
                    movement.getProductId(), movement.getLotId());
            remainingByStockKey.merge(key, movement.getQuantity().negate(), BigDecimal::add);
        }
        if (remainingByStockKey.values().stream().anyMatch(quantity -> quantity.compareTo(BigDecimal.ZERO) < 0)) {
            // Historical releases may never exceed the reservations they reverse.
            return false;
        }

        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

        for (var movement : reserveMovements) {
            String key = stockKey(movement.getFromWarehouseId(), movement.getFromLocationId(),
                    movement.getProductId(), movement.getLotId());
            BigDecimal remaining = remainingByStockKey.getOrDefault(key, BigDecimal.ZERO);
            BigDecimal qty = movement.getQuantity().min(remaining);
            if (qty.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }

            // Lock the exact RESERVED balance before validating and debiting it.
            var reservedBalance = ctx.selectFrom(STOCK_BALANCES)
                    .where(STOCK_BALANCES.WAREHOUSE_ID.eq(movement.getFromWarehouseId()))
                    .and(STOCK_BALANCES.LOCATION_ID.eq(movement.getFromLocationId()))
                    .and(STOCK_BALANCES.PRODUCT_ID.eq(movement.getProductId()))
                    .and(STOCK_BALANCES.LOT_ID.eq(movement.getLotId()))
                    .and(STOCK_BALANCES.STOCK_STATUS_ID.eq(reservedStatusId))
                    .forUpdate()
                    .fetchOptional();
            if (reservedBalance.isEmpty() || reservedBalance.get().getQuantity().compareTo(qty) < 0) {
                return false;
            }

            // Repeat the quantity predicate in the update to protect against stale or invalid data.
            int updated = ctx.update(STOCK_BALANCES)
                    .set(STOCK_BALANCES.QUANTITY, STOCK_BALANCES.QUANTITY.subtract(qty))
                    .set(STOCK_BALANCES.VERSION, STOCK_BALANCES.VERSION.add(1L))
                    .set(STOCK_BALANCES.UPDATED_AT, now)
                    .where(STOCK_BALANCES.WAREHOUSE_ID.eq(movement.getFromWarehouseId()))
                    .and(STOCK_BALANCES.LOCATION_ID.eq(movement.getFromLocationId()))
                    .and(STOCK_BALANCES.PRODUCT_ID.eq(movement.getProductId()))
                    .and(STOCK_BALANCES.LOT_ID.eq(movement.getLotId()))
                    .and(STOCK_BALANCES.STOCK_STATUS_ID.eq(reservedStatusId))
                    .and(STOCK_BALANCES.QUANTITY.ge(qty))
                    .execute();
            if (updated != 1) {
                return false;
            }

            // Return the validated quantity to the same available stock balance.
            ctx.insertInto(STOCK_BALANCES)
                    .columns(STOCK_BALANCES.ID, STOCK_BALANCES.WAREHOUSE_ID, STOCK_BALANCES.LOCATION_ID,
                            STOCK_BALANCES.PRODUCT_ID, STOCK_BALANCES.LOT_ID, STOCK_BALANCES.STOCK_STATUS_ID,
                            STOCK_BALANCES.QUANTITY, STOCK_BALANCES.VERSION, STOCK_BALANCES.CREATED_AT,
                            STOCK_BALANCES.UPDATED_AT)
                    .values(UuidV7.generate(), movement.getFromWarehouseId(), movement.getFromLocationId(),
                            movement.getProductId(), movement.getLotId(), availableStatusId, qty,
                            0L, now, now)
                    .onConflict(STOCK_BALANCES.WAREHOUSE_ID, STOCK_BALANCES.LOCATION_ID,
                            STOCK_BALANCES.PRODUCT_ID, STOCK_BALANCES.LOT_ID, STOCK_BALANCES.STOCK_STATUS_ID)
                    .doUpdate()
                    .set(STOCK_BALANCES.QUANTITY, STOCK_BALANCES.QUANTITY.add(qty))
                    .set(STOCK_BALANCES.VERSION, STOCK_BALANCES.VERSION.add(1L))
                    .set(STOCK_BALANCES.UPDATED_AT, now)
                    .execute();

            // Persist the reversal so a later retry cannot release this quantity again.
            ctx.insertInto(STOCK_MOVEMENTS)
                    .columns(STOCK_MOVEMENTS.ID, STOCK_MOVEMENTS.MOVEMENT_TYPE_ID, STOCK_MOVEMENTS.PRODUCT_ID,
                            STOCK_MOVEMENTS.LOT_ID, STOCK_MOVEMENTS.WORK_ORDER_ID,
                            STOCK_MOVEMENTS.FROM_WAREHOUSE_ID, STOCK_MOVEMENTS.FROM_LOCATION_ID,
                            STOCK_MOVEMENTS.TO_WAREHOUSE_ID, STOCK_MOVEMENTS.TO_LOCATION_ID,
                            STOCK_MOVEMENTS.QUANTITY, STOCK_MOVEMENTS.FROM_STATUS_ID, STOCK_MOVEMENTS.TO_STATUS_ID,
                            STOCK_MOVEMENTS.REASON, STOCK_MOVEMENTS.CREATED_BY)
                    .values(UuidV7.generate(), releaseMovementTypeId, movement.getProductId(), movement.getLotId(),
                            workOrderId, movement.getFromWarehouseId(), movement.getFromLocationId(), movement.getFromWarehouseId(),
                            movement.getFromLocationId(), qty, reservedStatusId, availableStatusId,
                            "Work Order material release", actorId)
                    .execute();
            remainingByStockKey.put(key, remaining.subtract(qty));
        }

        // Material lines are cleared only after all stock balance updates and movements succeed.
        ctx.update(WORK_ORDER_MATERIALS)
                .set(WORK_ORDER_MATERIALS.RESERVED_QUANTITY, BigDecimal.ZERO)
                .where(WORK_ORDER_MATERIALS.WORK_ORDER_ID.eq(workOrderId))
                .execute();
        return true;
    }

    private String stockKey(UUID warehouseId, UUID locationId, UUID productId, UUID lotId) {
        // The four dimensions uniquely identify a stock balance for release accounting.
        return warehouseId + ":" + locationId + ":" + productId + ":" + lotId;
    }
}
