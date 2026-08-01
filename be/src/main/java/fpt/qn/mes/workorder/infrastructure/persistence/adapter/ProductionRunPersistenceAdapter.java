package fpt.qn.mes.workorder.infrastructure.persistence.adapter;

import static fpt.qn.mes.jooq.Tables.MACHINES;
import static fpt.qn.mes.jooq.Tables.MACHINE_STATUSES;
import static fpt.qn.mes.jooq.Tables.PRODUCTION_RUNS;
import static fpt.qn.mes.jooq.Tables.WORK_ORDER_EVENTS;
import static fpt.qn.mes.jooq.Tables.WORK_ORDER_EVENT_TYPES;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import fpt.qn.mes.common.util.UuidV7;
import fpt.qn.mes.workorder.application.port.out.ProductionRunPort;
import fpt.qn.mes.workorder.application.port.out.dto.ActiveProductionRun;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Repository
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductionRunPersistenceAdapter implements ProductionRunPort {

    DSLContext ctx;

    @Override
    public UUID createProductionRun(UUID workOrderId, UUID machineId, UUID productionLineId, UUID operatorId) {
        UUID runId = UuidV7.generate();
        ctx.insertInto(PRODUCTION_RUNS)
                .set(PRODUCTION_RUNS.ID, runId)
                .set(PRODUCTION_RUNS.WORK_ORDER_ID, workOrderId)
                .set(PRODUCTION_RUNS.MACHINE_ID, machineId)
                .set(PRODUCTION_RUNS.PRODUCTION_LINE_ID, productionLineId)
                .set(PRODUCTION_RUNS.OPERATOR_ID, operatorId)
                .set(PRODUCTION_RUNS.START_TIME, OffsetDateTime.now(ZoneOffset.UTC))
                .execute();
        return runId;
    }

    @Override
    public Optional<UUID> findActiveProductionRunId(UUID workOrderId) {
        return ctx.select(PRODUCTION_RUNS.ID)
                .from(PRODUCTION_RUNS)
                .where(PRODUCTION_RUNS.WORK_ORDER_ID.eq(workOrderId))
                .and(PRODUCTION_RUNS.END_TIME.isNull())
                .orderBy(PRODUCTION_RUNS.START_TIME.desc())
                .fetchOptional(PRODUCTION_RUNS.ID);
    }

    @Override
    public Optional<ActiveProductionRun> findActiveProductionRunForUpdate(UUID workOrderId) {
        // Lock the active run so only one completion transaction can close it.
        return ctx.select(PRODUCTION_RUNS.ID, PRODUCTION_RUNS.MACHINE_ID)
                .from(PRODUCTION_RUNS)
                .where(PRODUCTION_RUNS.WORK_ORDER_ID.eq(workOrderId))
                .and(PRODUCTION_RUNS.END_TIME.isNull())
                .orderBy(PRODUCTION_RUNS.START_TIME.asc(), PRODUCTION_RUNS.ID.asc())
                .forUpdate()
                .fetchOptional(record -> ActiveProductionRun.builder()
                        .id(record.get(PRODUCTION_RUNS.ID))
                        .machineId(record.get(PRODUCTION_RUNS.MACHINE_ID))
                        .build());
    }

    @Override
    public boolean closeActiveProductionRun(UUID productionRunId, BigDecimal actualQuantity,
            BigDecimal goodQuantity, BigDecimal defectQuantity, BigDecimal scrapQuantity) {
        // The end-time predicate prevents a second caller from overwriting final production results.
        return ctx.update(PRODUCTION_RUNS)
                .set(PRODUCTION_RUNS.END_TIME, OffsetDateTime.now(ZoneOffset.UTC))
                .set(PRODUCTION_RUNS.ACTUAL_QUANTITY, actualQuantity)
                .set(PRODUCTION_RUNS.GOOD_QUANTITY, goodQuantity)
                .set(PRODUCTION_RUNS.DEFECT_QUANTITY, defectQuantity)
                .set(PRODUCTION_RUNS.SCRAP_QUANTITY, scrapQuantity)
                .where(PRODUCTION_RUNS.ID.eq(productionRunId))
                .and(PRODUCTION_RUNS.END_TIME.isNull())
                .execute() == 1;
    }

    @Override
    public boolean isMachineRunning(UUID machineId) {
        return ctx.fetchExists(
                ctx.selectFrom(PRODUCTION_RUNS)
                        .where(PRODUCTION_RUNS.MACHINE_ID.eq(machineId))
                        .and(PRODUCTION_RUNS.END_TIME.isNull())
        );
    }

    @Override
    public void updateMachineStatus(UUID machineId, String statusName) {
        UUID statusId = ctx.select(MACHINE_STATUSES.ID)
                .from(MACHINE_STATUSES)
                .where(MACHINE_STATUSES.NAME.eq(statusName))
                .fetchOne(MACHINE_STATUSES.ID);

        if (statusId != null) {
            ctx.update(MACHINES)
                    .set(MACHINES.MACHINE_STATUS_ID, statusId)
                    .where(MACHINES.ID.eq(machineId))
                    .execute();
        }
    }

    @Override
    public void recordWorkOrderEvent(UUID workOrderId, UUID productionRunId, String eventTypeName, UUID operatorId) {
        recordWorkOrderEvent(workOrderId, productionRunId, eventTypeName, operatorId, null);
    }

    @Override
    public void recordWorkOrderEvent(UUID workOrderId, UUID productionRunId, String eventTypeName, UUID operatorId,
            String note) {
        // Resolve the event type by its seeded business name before creating the immutable event ledger row.
        UUID eventTypeId = ctx.select(WORK_ORDER_EVENT_TYPES.ID)
                .from(WORK_ORDER_EVENT_TYPES)
                .where(WORK_ORDER_EVENT_TYPES.NAME.eq(eventTypeName))
                .fetchOne(WORK_ORDER_EVENT_TYPES.ID);

        if (eventTypeId != null) {
            ctx.insertInto(WORK_ORDER_EVENTS)
                    .set(WORK_ORDER_EVENTS.ID, UuidV7.generate())
                    .set(WORK_ORDER_EVENTS.WORK_ORDER_ID, workOrderId)
                    .set(WORK_ORDER_EVENTS.PRODUCTION_RUN_ID, productionRunId)
                    .set(WORK_ORDER_EVENTS.EVENT_TYPE_ID, eventTypeId)
                    .set(WORK_ORDER_EVENTS.OPERATOR_ID, operatorId)
                    .set(WORK_ORDER_EVENTS.EVENT_TIMESTAMP, OffsetDateTime.now(ZoneOffset.UTC))
                    .set(WORK_ORDER_EVENTS.NOTE, note)
                    .execute();
        }
    }
}
