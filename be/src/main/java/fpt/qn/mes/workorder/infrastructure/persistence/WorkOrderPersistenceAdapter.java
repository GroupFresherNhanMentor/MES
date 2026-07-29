package fpt.qn.mes.workorder.infrastructure.persistence;

import static fpt.qn.mes.jooq.Tables.WORK_ORDERS;

import java.util.Optional;
import java.util.UUID;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import fpt.qn.mes.common.dto.response.PaginationResult;
import fpt.qn.mes.common.repository.BaseRepository;
import fpt.qn.mes.jooq.tables.records.WorkOrdersRecord;
import fpt.qn.mes.workorder.domain.entities.WorkOrder;
import fpt.qn.mes.workorder.domain.entities.WorkOrderEvent;
import fpt.qn.mes.workorder.domain.entities.WorkOrderMaterial;
import fpt.qn.mes.workorder.domain.repository.WorkOrderRepository;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

import fpt.qn.mes.workorder.domain.repository.criteria.WorkOrderSearchCriteria;

@Repository
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class WorkOrderPersistenceAdapter extends BaseRepository<WorkOrdersRecord> implements WorkOrderRepository {

    WorkOrderRecordMapper mapper;
    DSLContext dslCtx;

    public WorkOrderPersistenceAdapter(DSLContext ctx, WorkOrderRecordMapper mapper) {
        super(ctx, WORK_ORDERS); this.mapper = mapper; this.dslCtx = ctx;
    }

    @Override
    public Optional<WorkOrder> findById(UUID id) {
        return Optional.ofNullable(dslCtx.selectFrom(WORK_ORDERS)
                .where(WORK_ORDERS.ID.eq(id))
                .fetchOne())
                .map(r -> mapper.toDomain(r));
    }
    @Override
    public WorkOrder save(WorkOrder w) {
        WorkOrdersRecord record = mapper.toRecord(w);
        if (record.getId() == null) {
            record.setId(UUID.randomUUID());
        }
        dslCtx.attach(record);
        record.store();
        return mapper.toDomain(record);
    }
    @Override public WorkOrder update(WorkOrder w) { throw new UnsupportedOperationException("Not implemented"); }
    @Override public void deleteById(UUID id) {}

    @Override
    public PaginationResult<WorkOrder> findAll(WorkOrderSearchCriteria criteria) {
        var condition = buildCondition(criteria);
        int page = criteria.getPage();
        int size = criteria.getSize();

        var records = dslCtx.selectFrom(WORK_ORDERS)
                .where(condition)
                .orderBy(WORK_ORDERS.CREATED_AT.desc())
                .limit(size)
                .offset((long) page * size)
                .fetch();

        int total = dslCtx.fetchCount(dslCtx.selectFrom(WORK_ORDERS).where(condition));
        var items = records.stream().map(r -> mapper.toDomain(r)).toList();

        return PaginationResult.of(items, total, page, size);
    }

    private org.jooq.Condition buildCondition(WorkOrderSearchCriteria criteria) {
        var condition = org.jooq.impl.DSL.noCondition();
        if (criteria.getFinishedProductId() != null) {
            condition = condition.and(WORK_ORDERS.FINISHED_PRODUCT_ID.eq(criteria.getFinishedProductId()));
        }
        if (criteria.getStatusId() != null) {
            condition = condition.and(WORK_ORDERS.WORK_ORDER_STATUS_ID.eq(criteria.getStatusId()));
        }
        if (criteria.getCode() != null && !criteria.getCode().isBlank()) {
            condition = condition.and(WORK_ORDERS.CODE.likeIgnoreCase("%" + criteria.getCode() + "%"));
        }
        return condition;
    }

    @Override
    public WorkOrderMaterial saveMaterial(WorkOrderMaterial m) {
        var record = mapper.toRecord(m);
        if (record.getId() == null) {
            record.setId(UUID.randomUUID());
        }
        dslCtx.attach(record);
        record.store();
        return mapper.toDomain(record);
    }
    @Override public Optional<WorkOrderMaterial> findMaterialById(UUID materialId) { throw new UnsupportedOperationException("Not implemented"); }
    @Override public PaginationResult<WorkOrderMaterial> findMaterialsByWorkOrderId(UUID workOrderId, int page, int size) { throw new UnsupportedOperationException("Not implemented"); }

    @Override
    public java.util.List<WorkOrderMaterial> findMaterialsByWorkOrderId(UUID workOrderId) {
        return dslCtx.selectFrom(fpt.qn.mes.jooq.Tables.WORK_ORDER_MATERIALS)
                .where(fpt.qn.mes.jooq.Tables.WORK_ORDER_MATERIALS.WORK_ORDER_ID.eq(workOrderId))
                .fetch()
                .stream()
                .map(r -> mapper.toDomain(r))
                .toList();
    }

    @Override public void deleteMaterialById(UUID materialId) {}
    @Override public WorkOrderEvent saveEvent(WorkOrderEvent e) { throw new UnsupportedOperationException("Not implemented"); }
    @Override public PaginationResult<WorkOrderEvent> findEventsByWorkOrderId(UUID workOrderId, int page, int size) { throw new UnsupportedOperationException("Not implemented"); }

    @Override
    public java.util.List<WorkOrderEvent> findEventsByWorkOrderId(UUID workOrderId) {
        return dslCtx.selectFrom(fpt.qn.mes.jooq.Tables.WORK_ORDER_EVENTS)
                .where(fpt.qn.mes.jooq.Tables.WORK_ORDER_EVENTS.WORK_ORDER_ID.eq(workOrderId))
                .orderBy(fpt.qn.mes.jooq.Tables.WORK_ORDER_EVENTS.EVENT_TIMESTAMP.desc())
                .fetch()
                .stream()
                .map(r -> mapper.toDomain(r))
                .toList();
    }
}
