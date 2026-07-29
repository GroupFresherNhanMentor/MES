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

@Repository
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class WorkOrderPersistenceAdapter extends BaseRepository<WorkOrdersRecord> implements WorkOrderRepository {

    WorkOrderRecordMapper mapper;
    DSLContext dslCtx;

    public WorkOrderPersistenceAdapter(DSLContext ctx, WorkOrderRecordMapper mapper) {
        super(ctx, WORK_ORDERS); this.mapper = mapper; this.dslCtx = ctx;
    }

    @Override public Optional<WorkOrder> findById(UUID id) { throw new UnsupportedOperationException("Not implemented"); }
    @Override public WorkOrder save(WorkOrder w) { throw new UnsupportedOperationException("Not implemented"); }
    @Override public WorkOrder update(WorkOrder w) { throw new UnsupportedOperationException("Not implemented"); }
    @Override public void deleteById(UUID id) {}
    @Override
    public PaginationResult<WorkOrder> findAll(fpt.qn.mes.workorder.domain.repository.criteria.WorkOrderSearchCriteria criteria) {
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

    private org.jooq.Condition buildCondition(fpt.qn.mes.workorder.domain.repository.criteria.WorkOrderSearchCriteria criteria) {
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
    @Override public WorkOrderMaterial saveMaterial(WorkOrderMaterial m) { throw new UnsupportedOperationException("Not implemented"); }
    @Override public Optional<WorkOrderMaterial> findMaterialById(UUID materialId) { throw new UnsupportedOperationException("Not implemented"); }
    @Override public PaginationResult<WorkOrderMaterial> findMaterialsByWorkOrderId(UUID workOrderId, int page, int size) { throw new UnsupportedOperationException("Not implemented"); }
    @Override public void deleteMaterialById(UUID materialId) {}
    @Override public WorkOrderEvent saveEvent(WorkOrderEvent e) { throw new UnsupportedOperationException("Not implemented"); }
    @Override public PaginationResult<WorkOrderEvent> findEventsByWorkOrderId(UUID workOrderId, int page, int size) { throw new UnsupportedOperationException("Not implemented"); }
}
