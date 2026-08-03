package fpt.qn.mes.workorder.infrastructure.persistence.adapter;

import static fpt.qn.mes.jooq.Tables.LOT_TYPES;
import static fpt.qn.mes.jooq.Tables.MACHINES;
import static fpt.qn.mes.jooq.Tables.MACHINE_STATUSES;
import static fpt.qn.mes.jooq.Tables.MOVEMENT_TYPES;
import static fpt.qn.mes.jooq.Tables.PRODUCTION_RUNS;
import static fpt.qn.mes.jooq.Tables.QC_STATUSES;
import static fpt.qn.mes.jooq.Tables.QUALITY_INSPECTIONS;
import static fpt.qn.mes.jooq.Tables.STOCK_BALANCES;
import static fpt.qn.mes.jooq.Tables.STOCK_LOTS;
import static fpt.qn.mes.jooq.Tables.STOCK_MOVEMENTS;
import static fpt.qn.mes.jooq.Tables.STOCK_STATUSES;
import static fpt.qn.mes.jooq.Tables.WAREHOUSE_LOCATIONS;
import static fpt.qn.mes.jooq.Tables.WAREHOUSES;
import static fpt.qn.mes.jooq.Tables.WORK_ORDER_EVENTS;
import static fpt.qn.mes.jooq.Tables.WORK_ORDER_EVENT_TYPES;
import static fpt.qn.mes.jooq.Tables.WORK_ORDER_MATERIALS;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import fpt.qn.mes.common.util.UuidV7;
import fpt.qn.mes.inventory.domain.constants.MovementTypeConstants;
import fpt.qn.mes.inventory.domain.constants.StockStatusConstants;
import fpt.qn.mes.workorder.application.dto.workorder.complete.CompleteWorkOrderRequest;
import fpt.qn.mes.workorder.application.port.out.dto.ActiveProductionRun;
import fpt.qn.mes.workorder.application.port.out.dto.CompletionOutputDestination;
import fpt.qn.mes.workorder.application.port.out.dto.CompletionReferences;
import fpt.qn.mes.workorder.application.port.out.dto.CompletionReservationAllocation;
import fpt.qn.mes.workorder.application.port.out.WorkOrderCompletionPort;
import fpt.qn.mes.workorder.domain.entities.WorkOrder;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Repository
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class WorkOrderCompletionPersistenceAdapter implements WorkOrderCompletionPort {

    DSLContext ctx;

    @Override
    public Optional<CompletionOutputDestination> findOutputDestination(UUID warehouseId, UUID locationId) {
        // A destination is valid only when the requested location belongs to the requested warehouse.
        return ctx.select(WAREHOUSES.ID, WAREHOUSE_LOCATIONS.ID)
                .from(WAREHOUSES)
                .join(WAREHOUSE_LOCATIONS).on(WAREHOUSE_LOCATIONS.WAREHOUSE_ID.eq(WAREHOUSES.ID))
                .where(WAREHOUSES.ID.eq(warehouseId))
                .and(WAREHOUSE_LOCATIONS.ID.eq(locationId))
                .fetchOptional(record -> CompletionOutputDestination.builder()
                        .warehouseId(record.get(WAREHOUSES.ID))
                        .locationId(record.get(WAREHOUSE_LOCATIONS.ID))
                        .build());
    }

    @Override
    public List<CompletionReservationAllocation> findOutstandingReservations(UUID workOrderId) {
        // Rebuild outstanding reservation per physical lot from the immutable stock movement ledger.
        Map<String, CompletionReservationAllocation> reservations = new HashMap<>();
        Map<String, BigDecimal> outstanding = new HashMap<>();
        var movements = ctx.select(STOCK_MOVEMENTS.asterisk(), MOVEMENT_TYPES.NAME)
                .from(STOCK_MOVEMENTS)
                .join(MOVEMENT_TYPES).on(MOVEMENT_TYPES.ID.eq(STOCK_MOVEMENTS.MOVEMENT_TYPE_ID))
                .where(STOCK_MOVEMENTS.WORK_ORDER_ID.eq(workOrderId))
                .and(MOVEMENT_TYPES.NAME.in(MovementTypeConstants.RESERVE,
                        MovementTypeConstants.RELEASE_RESERVATION,
                        MovementTypeConstants.CONSUME_IN_PRODUCTION, MovementTypeConstants.SCRAP))
                .orderBy(STOCK_MOVEMENTS.PRODUCT_ID.asc(), STOCK_MOVEMENTS.FROM_WAREHOUSE_ID.asc(),
                        STOCK_MOVEMENTS.FROM_LOCATION_ID.asc(), STOCK_MOVEMENTS.LOT_ID.asc(), STOCK_MOVEMENTS.ID.asc())
                .fetch();
        for (var movement : movements) {
            String key = stockKey(movement.get(STOCK_MOVEMENTS.FROM_WAREHOUSE_ID),
                    movement.get(STOCK_MOVEMENTS.FROM_LOCATION_ID), movement.get(STOCK_MOVEMENTS.PRODUCT_ID),
                    movement.get(STOCK_MOVEMENTS.LOT_ID));
            String movementName = movement.get(MOVEMENT_TYPES.NAME);
            BigDecimal quantity = movement.get(STOCK_MOVEMENTS.QUANTITY);
            // RESERVE increases the allocation; release and prior completion movements decrease it.
            if (MovementTypeConstants.RESERVE.equals(movementName)) {
                reservations.putIfAbsent(key, CompletionReservationAllocation.builder()
                        .warehouseId(movement.get(STOCK_MOVEMENTS.FROM_WAREHOUSE_ID))
                        .locationId(movement.get(STOCK_MOVEMENTS.FROM_LOCATION_ID))
                        .materialProductId(movement.get(STOCK_MOVEMENTS.PRODUCT_ID))
                        .lotId(movement.get(STOCK_MOVEMENTS.LOT_ID))
                        .build());
                outstanding.merge(key, quantity, (left, right) -> left.add(right));
            } else {
                outstanding.merge(key, quantity.negate(), (left, right) -> left.add(right));
            }
        }
        List<CompletionReservationAllocation> allocations = new ArrayList<>();
        for (var entry : reservations.entrySet()) {
            BigDecimal quantity = outstanding.getOrDefault(entry.getKey(), BigDecimal.ZERO);
            if (quantity.compareTo(BigDecimal.ZERO) > 0) {
                CompletionReservationAllocation reservation = entry.getValue();
                allocations.add(CompletionReservationAllocation.builder()
                        .warehouseId(reservation.getWarehouseId())
                        .locationId(reservation.getLocationId())
                        .materialProductId(reservation.getMaterialProductId())
                        .lotId(reservation.getLotId())
                        .reservedQuantity(quantity)
                        .build());
            }
        }
        return allocations;
    }

    @Override
    public CompletionReferences findCompletionReferences() {
        // Resolve mutable lookup IDs once so finalization can fail before any stock is changed.
        return CompletionReferences.builder()
                .availableStatusId(findStockStatus(StockStatusConstants.AVAILABLE))
                .reservedStatusId(findStockStatus(StockStatusConstants.RESERVED))
                .consumedStatusId(findStockStatus(StockStatusConstants.CONSUMED))
                .scrappedStatusId(findStockStatus(StockStatusConstants.SCRAPPED))
                .qualityInspectionStatusId(findStockStatus(StockStatusConstants.QUALITY_INSPECTION))
                .consumeMovementTypeId(findMovementType(MovementTypeConstants.CONSUME_IN_PRODUCTION))
                .scrapMovementTypeId(findMovementType(MovementTypeConstants.SCRAP))
                .releaseMovementTypeId(findMovementType(MovementTypeConstants.RELEASE_RESERVATION))
                .productionOutputMovementTypeId(findMovementType(MovementTypeConstants.PRODUCTION_OUTPUT))
                .productionLotTypeId(ctx.select(LOT_TYPES.ID).from(LOT_TYPES).where(LOT_TYPES.NAME.eq("PRODUCTION"))
                        .fetchOne(LOT_TYPES.ID))
                .pendingInspectionStatusId(ctx.select(QC_STATUSES.ID).from(QC_STATUSES)
                        .where(QC_STATUSES.NAME.eq("PENDING_INSPECTION")).fetchOne(QC_STATUSES.ID))
                .completeEventTypeId(ctx.select(WORK_ORDER_EVENT_TYPES.ID).from(WORK_ORDER_EVENT_TYPES)
                        .where(WORK_ORDER_EVENT_TYPES.NAME.eq("COMPLETE")).fetchOne(WORK_ORDER_EVENT_TYPES.ID))
                .availableMachineStatusId(ctx.select(MACHINE_STATUSES.ID).from(MACHINE_STATUSES)
                        .where(MACHINE_STATUSES.NAME.eq("AVAILABLE")).fetchOne(MACHINE_STATUSES.ID))
                .build();
    }

    @Override
    public boolean finalizeCompletion(WorkOrder workOrder, ActiveProductionRun productionRun,
            CompleteWorkOrderRequest request, List<CompletionReservationAllocation> allocations,
            CompletionOutputDestination destination, CompletionReferences references, UUID actorId) {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        for (CompletionReservationAllocation allocation : allocations) {
            // Lock the exact reserved balance for this original lot before consuming or releasing it.
            var balance = ctx.selectFrom(STOCK_BALANCES)
                    .where(STOCK_BALANCES.WAREHOUSE_ID.eq(allocation.getWarehouseId()))
                    .and(STOCK_BALANCES.LOCATION_ID.eq(allocation.getLocationId()))
                    .and(STOCK_BALANCES.PRODUCT_ID.eq(allocation.getMaterialProductId()))
                    .and(STOCK_BALANCES.LOT_ID.eq(allocation.getLotId()))
                    .and(STOCK_BALANCES.STOCK_STATUS_ID.eq(references.getReservedStatusId()))
                    .forUpdate()
                    .fetchOptional();
            if (balance.isEmpty() || balance.get().getQuantity().compareTo(allocation.getReservedQuantity()) < 0) {
                return false;
            }
            int updated = ctx.update(STOCK_BALANCES)
                    .set(STOCK_BALANCES.QUANTITY, STOCK_BALANCES.QUANTITY.subtract(allocation.getReservedQuantity()))
                    .set(STOCK_BALANCES.VERSION, STOCK_BALANCES.VERSION.add(1L))
                    .set(STOCK_BALANCES.UPDATED_AT, now)
                    .where(STOCK_BALANCES.ID.eq(balance.get().getId()))
                    .and(STOCK_BALANCES.QUANTITY.ge(allocation.getReservedQuantity()))
                    .execute();
            if (updated != 1) {
                return false;
            }
            // Normal consumption and scrap partition the single physical deduction from RESERVED stock.
            BigDecimal normalQuantity = allocation.getConsumedQuantity().subtract(allocation.getScrapQuantity());
            insertMovement(references.getConsumeMovementTypeId(), allocation, normalQuantity, workOrder.getId(),
                    references.getReservedStatusId(), references.getConsumedStatusId(), actorId, "Production consumption");
            insertMovement(references.getScrapMovementTypeId(), allocation, allocation.getScrapQuantity(), workOrder.getId(),
                    references.getReservedStatusId(), references.getScrappedStatusId(), actorId, "Production material scrap");
            BigDecimal releasedQuantity = allocation.getReservedQuantity().subtract(allocation.getConsumedQuantity());
            if (releasedQuantity.compareTo(BigDecimal.ZERO) > 0) {
                // Return only the unconsumed remainder to the same lot and warehouse location.
                ctx.insertInto(STOCK_BALANCES)
                        .columns(STOCK_BALANCES.ID, STOCK_BALANCES.WAREHOUSE_ID, STOCK_BALANCES.LOCATION_ID,
                                STOCK_BALANCES.PRODUCT_ID, STOCK_BALANCES.LOT_ID, STOCK_BALANCES.STOCK_STATUS_ID,
                                STOCK_BALANCES.QUANTITY, STOCK_BALANCES.VERSION, STOCK_BALANCES.CREATED_AT,
                                STOCK_BALANCES.UPDATED_AT)
                        .values(UuidV7.generate(), allocation.getWarehouseId(), allocation.getLocationId(),
                                allocation.getMaterialProductId(), allocation.getLotId(), references.getAvailableStatusId(),
                                releasedQuantity, 0L, now, now)
                        .onConflict(STOCK_BALANCES.WAREHOUSE_ID, STOCK_BALANCES.LOCATION_ID, STOCK_BALANCES.PRODUCT_ID,
                                STOCK_BALANCES.LOT_ID, STOCK_BALANCES.STOCK_STATUS_ID)
                        .doUpdate().set(STOCK_BALANCES.QUANTITY, STOCK_BALANCES.QUANTITY.add(releasedQuantity))
                        .set(STOCK_BALANCES.VERSION, STOCK_BALANCES.VERSION.add(1L))
                        .set(STOCK_BALANCES.UPDATED_AT, now)
                        .execute();
                insertMovement(references.getReleaseMovementTypeId(), allocation, releasedQuantity, workOrder.getId(),
                        references.getReservedStatusId(), references.getAvailableStatusId(), actorId, "Production reservation release");
            }
        }
        for (CompletionReservationAllocation allocation : allocations) {
            // A material line can span multiple lots, so accumulate each lot contribution independently.
            ctx.update(WORK_ORDER_MATERIALS)
                    .set(WORK_ORDER_MATERIALS.RESERVED_QUANTITY, BigDecimal.ZERO)
                    .set(WORK_ORDER_MATERIALS.CONSUMED_QUANTITY,
                            WORK_ORDER_MATERIALS.CONSUMED_QUANTITY.add(allocation.getConsumedQuantity()))
                    .where(WORK_ORDER_MATERIALS.WORK_ORDER_ID.eq(workOrder.getId()))
                    .and(WORK_ORDER_MATERIALS.MATERIAL_PRODUCT_ID.eq(allocation.getMaterialProductId()))
                    .execute();
        }
        ctx.update(WORK_ORDER_MATERIALS)
                .set(WORK_ORDER_MATERIALS.RESERVED_QUANTITY, BigDecimal.ZERO)
                .where(WORK_ORDER_MATERIALS.WORK_ORDER_ID.eq(workOrder.getId()))
                .execute();
        // Good and defective output remain physically separate while both await quality inspection.
        createOutput(workOrder, request.getGoodQuantity(), "GOOD", destination, references, actorId, now);
        createOutput(workOrder, request.getDefectQuantity(), "DEFECT", destination, references, actorId, now);
        // Closing only an unclosed run is the final persistence guard against duplicate completion.
        if (ctx.update(PRODUCTION_RUNS)
                .set(PRODUCTION_RUNS.END_TIME, now)
                .set(PRODUCTION_RUNS.ACTUAL_QUANTITY, request.getActualQuantity())
                .set(PRODUCTION_RUNS.GOOD_QUANTITY, request.getGoodQuantity())
                .set(PRODUCTION_RUNS.DEFECT_QUANTITY, request.getDefectQuantity())
                .set(PRODUCTION_RUNS.SCRAP_QUANTITY, request.getScrapQuantity())
                .where(PRODUCTION_RUNS.ID.eq(productionRun.getId()))
                .and(PRODUCTION_RUNS.END_TIME.isNull())
                .execute() != 1) {
            return false;
        }
        // The machine becomes available only after its production run was closed successfully.
        ctx.update(MACHINES).set(MACHINES.MACHINE_STATUS_ID, references.getAvailableMachineStatusId())
                .where(MACHINES.ID.eq(productionRun.getMachineId())).execute();
        ctx.insertInto(WORK_ORDER_EVENTS)
                .set(WORK_ORDER_EVENTS.ID, UuidV7.generate())
                .set(WORK_ORDER_EVENTS.WORK_ORDER_ID, workOrder.getId())
                .set(WORK_ORDER_EVENTS.PRODUCTION_RUN_ID, productionRun.getId())
                .set(WORK_ORDER_EVENTS.EVENT_TYPE_ID, references.getCompleteEventTypeId())
                .set(WORK_ORDER_EVENTS.OPERATOR_ID, actorId)
                .set(WORK_ORDER_EVENTS.EVENT_TIMESTAMP, now)
                .set(WORK_ORDER_EVENTS.NOTE, request.getNote())
                .execute();
        return true;
    }

    private void createOutput(WorkOrder workOrder, BigDecimal quantity, String classification,
            CompletionOutputDestination destination, CompletionReferences references, UUID actorId, OffsetDateTime now) {
        // Each classification receives a distinct lot so QC can track good and defective output separately.
        UUID lotId = UuidV7.generate();
        ctx.insertInto(STOCK_LOTS).set(STOCK_LOTS.ID, lotId).set(STOCK_LOTS.LOT_NUMBER,
                "PROD-" + classification + "-" + lotId).set(STOCK_LOTS.PRODUCT_ID, workOrder.getFinishedProductId())
                .set(STOCK_LOTS.LOT_TYPE_ID, references.getProductionLotTypeId()).set(STOCK_LOTS.CREATED_AT, now).execute();
        ctx.insertInto(STOCK_BALANCES).set(STOCK_BALANCES.ID, UuidV7.generate())
                .set(STOCK_BALANCES.WAREHOUSE_ID, destination.getWarehouseId())
                .set(STOCK_BALANCES.LOCATION_ID, destination.getLocationId())
                .set(STOCK_BALANCES.PRODUCT_ID, workOrder.getFinishedProductId()).set(STOCK_BALANCES.LOT_ID, lotId)
                .set(STOCK_BALANCES.STOCK_STATUS_ID, references.getQualityInspectionStatusId()).set(STOCK_BALANCES.QUANTITY, quantity)
                .set(STOCK_BALANCES.VERSION, 0L).set(STOCK_BALANCES.CREATED_AT, now).set(STOCK_BALANCES.UPDATED_AT, now).execute();
        ctx.insertInto(QUALITY_INSPECTIONS).set(QUALITY_INSPECTIONS.ID, UuidV7.generate())
                .set(QUALITY_INSPECTIONS.WORK_ORDER_ID, workOrder.getId())
                .set(QUALITY_INSPECTIONS.PRODUCT_ID, workOrder.getFinishedProductId())
                .set(QUALITY_INSPECTIONS.LOT_ID, lotId)
                .set(QUALITY_INSPECTIONS.QUANTITY, quantity)
                .set(QUALITY_INSPECTIONS.REMAINING_QUANTITY, quantity)
                .set(QUALITY_INSPECTIONS.QC_STATUS_ID, references.getPendingInspectionStatusId()).set(QUALITY_INSPECTIONS.CREATED_AT, now).execute();
        // Zero classifications remain traceable through lot, balance, and inspection but have no physical movement.
        if (quantity.compareTo(BigDecimal.ZERO) > 0) {
            ctx.insertInto(STOCK_MOVEMENTS).set(STOCK_MOVEMENTS.ID, UuidV7.generate())
                    .set(STOCK_MOVEMENTS.MOVEMENT_TYPE_ID, references.getProductionOutputMovementTypeId())
                    .set(STOCK_MOVEMENTS.PRODUCT_ID, workOrder.getFinishedProductId()).set(STOCK_MOVEMENTS.LOT_ID, lotId)
                    .set(STOCK_MOVEMENTS.WORK_ORDER_ID, workOrder.getId()).set(STOCK_MOVEMENTS.TO_WAREHOUSE_ID, destination.getWarehouseId())
                    .set(STOCK_MOVEMENTS.TO_LOCATION_ID, destination.getLocationId()).set(STOCK_MOVEMENTS.QUANTITY, quantity)
                    .set(STOCK_MOVEMENTS.TO_STATUS_ID, references.getQualityInspectionStatusId()).set(STOCK_MOVEMENTS.REASON, "Production output " + classification)
                    .set(STOCK_MOVEMENTS.CREATED_BY, actorId).execute();
        }
    }

    private void insertMovement(UUID movementTypeId, CompletionReservationAllocation allocation, BigDecimal quantity,
            UUID workOrderId, UUID fromStatusId, UUID toStatusId, UUID actorId, String reason) {
        // Movement records are written only for real quantities because the ledger requires positive amounts.
        if (quantity.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }
        ctx.insertInto(STOCK_MOVEMENTS).set(STOCK_MOVEMENTS.ID, UuidV7.generate()).set(STOCK_MOVEMENTS.MOVEMENT_TYPE_ID, movementTypeId)
                .set(STOCK_MOVEMENTS.PRODUCT_ID, allocation.getMaterialProductId()).set(STOCK_MOVEMENTS.LOT_ID, allocation.getLotId())
                .set(STOCK_MOVEMENTS.WORK_ORDER_ID, workOrderId).set(STOCK_MOVEMENTS.FROM_WAREHOUSE_ID, allocation.getWarehouseId())
                .set(STOCK_MOVEMENTS.FROM_LOCATION_ID, allocation.getLocationId()).set(STOCK_MOVEMENTS.QUANTITY, quantity)
                .set(STOCK_MOVEMENTS.FROM_STATUS_ID, fromStatusId).set(STOCK_MOVEMENTS.TO_STATUS_ID, toStatusId)
                .set(STOCK_MOVEMENTS.REASON, reason).set(STOCK_MOVEMENTS.CREATED_BY, actorId).execute();
    }

    private UUID findStockStatus(String name) {
        return ctx.select(STOCK_STATUSES.ID).from(STOCK_STATUSES).where(STOCK_STATUSES.NAME.eq(name)).fetchOne(STOCK_STATUSES.ID);
    }

    private UUID findMovementType(String name) {
        return ctx.select(MOVEMENT_TYPES.ID).from(MOVEMENT_TYPES).where(MOVEMENT_TYPES.NAME.eq(name)).fetchOne(MOVEMENT_TYPES.ID);
    }

    private String stockKey(UUID warehouseId, UUID locationId, UUID productId, UUID lotId) {
        return warehouseId + ":" + locationId + ":" + productId + ":" + lotId;
    }
}
