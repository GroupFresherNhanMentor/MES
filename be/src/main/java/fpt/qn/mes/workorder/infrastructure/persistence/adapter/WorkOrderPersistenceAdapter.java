package fpt.qn.mes.workorder.infrastructure.persistence.adapter;

import static fpt.qn.mes.jooq.Tables.WORK_ORDERS;
import static fpt.qn.mes.jooq.Tables.WORK_ORDER_EVENT_TYPES;
import static fpt.qn.mes.jooq.Tables.WORK_ORDER_EVENTS;
import static fpt.qn.mes.jooq.Tables.WORK_ORDER_MATERIALS;
import static fpt.qn.mes.jooq.Tables.WORK_ORDER_PRIORITIES;
import static fpt.qn.mes.jooq.Tables.WORK_ORDER_STATUSES;
import static fpt.qn.mes.jooq.Tables.WORK_ORDER_STATUS_TRANSITIONS;
import static org.jooq.impl.DSL.noCondition;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;

import fpt.qn.mes.common.domainQuery.PaginationResult;
import fpt.qn.mes.common.repository.BaseRepository;
import fpt.qn.mes.common.util.UuidV7;
import fpt.qn.mes.jooq.tables.records.WorkOrdersRecord;
import fpt.qn.mes.workorder.domain.entities.WorkOrder;
import fpt.qn.mes.workorder.domain.entities.WorkOrderEvent;
import fpt.qn.mes.workorder.domain.entities.WorkOrderEventType;
import fpt.qn.mes.workorder.domain.entities.WorkOrderMaterial;
import fpt.qn.mes.workorder.domain.entities.WorkOrderPriority;
import fpt.qn.mes.workorder.domain.entities.WorkOrderStatus;
import fpt.qn.mes.workorder.domain.repository.WorkOrderRepository;
import fpt.qn.mes.workorder.domain.repository.criteria.WorkOrderSearchCriteria;
import fpt.qn.mes.workorder.infrastructure.persistence.WorkOrderRecordMapper;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@Repository
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class WorkOrderPersistenceAdapter extends BaseRepository<WorkOrdersRecord> implements WorkOrderRepository {

    WorkOrderRecordMapper mapper;

    public WorkOrderPersistenceAdapter(DSLContext ctx, WorkOrderRecordMapper mapper) {
        super(ctx, WORK_ORDERS);
        this.mapper = mapper;
    }

    @Override
    public Optional<WorkOrder> findById(UUID id) {
        return ctx.selectFrom(WORK_ORDERS)
                .where(WORK_ORDERS.ID.eq(id))
                .fetchOptional(r -> mapper.toDomain(r));
    }

    @Override
    public Optional<WorkOrder> findForUpdate(UUID id) {
        return ctx.selectFrom(WORK_ORDERS)
                .where(WORK_ORDERS.ID.eq(id))
                .forUpdate()
                .fetchOptional(r -> mapper.toDomain(r));
    }

    @Override
    public WorkOrder save(WorkOrder w) {
        WorkOrdersRecord r = mapper.toRecord(w);
        if (r.getId() == null) {
            r.setId(UuidV7.generate());
        }
        ctx.insertInto(WORK_ORDERS).set(r)
                .onConflict(WORK_ORDERS.ID).doUpdate().set(r)
                .execute();
        return w;
    }

    @Override
    public WorkOrder update(WorkOrder w) {
        WorkOrdersRecord r = mapper.toRecord(w);
        ctx.update(WORK_ORDERS).set(r).where(WORK_ORDERS.ID.eq(w.getId())).execute();
        return w;
    }

    @Override
    public void deleteById(UUID id) {}

    public PaginationResult<WorkOrder> findAll(WorkOrderSearchCriteria criteria) {
        var condition = buildCondition(criteria);
        int page = criteria.getPage();
        int size = criteria.getSize();

        long total = ctx.fetchCount(WORK_ORDERS, condition);
        List<WorkOrder> items = ctx.selectFrom(WORK_ORDERS)
                .where(condition)
                .orderBy(WORK_ORDERS.CREATED_AT.desc())
                .limit(size)
                .offset((long) page * size)
                .fetch(r -> mapper.toDomain(r));

        return PaginationResult.<WorkOrder>builder().total(total).items(items).build();
    }

    private Condition buildCondition(WorkOrderSearchCriteria criteria) {
        var condition = noCondition();
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
        var r = mapper.toRecord(m);
        ctx.insertInto(WORK_ORDER_MATERIALS).set(r)
                .onConflict(WORK_ORDER_MATERIALS.ID).doUpdate().set(r)
                .execute();
        return m;
    }

    @Override
    public Optional<WorkOrderMaterial> findMaterialById(UUID materialId) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public PaginationResult<WorkOrderMaterial> findMaterialsByWorkOrderId(UUID workOrderId, int page, int size) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public List<WorkOrderMaterial> findMaterialsByWorkOrderId(UUID workOrderId) {
        return ctx.selectFrom(WORK_ORDER_MATERIALS)
                .where(WORK_ORDER_MATERIALS.WORK_ORDER_ID.eq(workOrderId))
                .fetch(r -> mapper.toDomain(r));
    }

    @Override
    public void deleteMaterialById(UUID materialId) {}

    @Override
    public WorkOrderEvent saveEvent(WorkOrderEvent e) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public PaginationResult<WorkOrderEvent> findEventsByWorkOrderId(UUID workOrderId, int page, int size) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public List<WorkOrderEvent> findEventsByWorkOrderId(UUID workOrderId) {
        return ctx.selectFrom(WORK_ORDER_EVENTS)
                .where(WORK_ORDER_EVENTS.WORK_ORDER_ID.eq(workOrderId))
                .orderBy(WORK_ORDER_EVENTS.EVENT_TIMESTAMP.desc())
                .fetch(r -> mapper.toDomain(r));
    }

    @Override
    public WorkOrderMaterial updateMaterial(WorkOrderMaterial m) {
        var r = mapper.toRecord(m);
        ctx.update(WORK_ORDER_MATERIALS).set(r).where(WORK_ORDER_MATERIALS.ID.eq(m.getId())).execute();
        return m;
    }

    @Override
    public Optional<String> findStatusNameById(UUID id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(ctx.select(WORK_ORDER_STATUSES.NAME)
                .from(WORK_ORDER_STATUSES)
                .where(WORK_ORDER_STATUSES.ID.eq(id))
                .fetchOneInto(String.class));
    }

    @Override
    public Optional<WorkOrderStatus> findStatusById(UUID id) {
        if (id == null) return Optional.empty();
        return ctx.selectFrom(WORK_ORDER_STATUSES)
                .where(WORK_ORDER_STATUSES.ID.eq(id))
                .fetchOptional(r -> WorkOrderStatus.builder()
                        .id(r.getId())
                        .name(r.getName())
                        .description(r.getDescription())
                        .isInitial(Boolean.TRUE.equals(r.getIsInitial()))
                        .isFinal(Boolean.TRUE.equals(r.getIsFinal()))
                        .build());
    }

    @Override
    public Optional<UUID> findStatusIdByName(String name) {
        return ctx.select(WORK_ORDER_STATUSES.ID)
                .from(WORK_ORDER_STATUSES)
                .where(WORK_ORDER_STATUSES.NAME.eq(name))
                .fetchOptionalInto(UUID.class);
    }

    @Override
    public boolean hasActiveTransition(UUID fromStatusId, UUID toStatusId) {
        return ctx.fetchExists(WORK_ORDER_STATUS_TRANSITIONS,
                WORK_ORDER_STATUS_TRANSITIONS.FROM_STATUS_ID.eq(fromStatusId)
                        .and(WORK_ORDER_STATUS_TRANSITIONS.TO_STATUS_ID.eq(toStatusId))
                        .and(WORK_ORDER_STATUS_TRANSITIONS.IS_ACTIVE.isTrue()));
    }

    @Override
    public boolean existsByCode(String code) {
        return code != null && ctx.fetchExists(WORK_ORDERS, WORK_ORDERS.CODE.eq(code));
    }

    @Override
    public boolean existsByCodeAndIdNot(String code, UUID excludeId) {
        return code != null && ctx.fetchExists(WORK_ORDERS,
                WORK_ORDERS.CODE.eq(code).and(WORK_ORDERS.ID.ne(excludeId)));
    }

    @Override
    public List<WorkOrderStatus> findAllStatuses(Boolean isInitial, Boolean isFinal) {
        Condition condition = DSL.noCondition();
        if (isInitial != null) {
            condition = condition.and(WORK_ORDER_STATUSES.IS_INITIAL.eq(isInitial));
        }
        if (isFinal != null) {
            condition = condition.and(WORK_ORDER_STATUSES.IS_FINAL.eq(isFinal));
        }
        return ctx.selectFrom(WORK_ORDER_STATUSES)
                .where(condition)
                .orderBy(WORK_ORDER_STATUSES.NAME.asc())
                .fetch(r -> WorkOrderStatus.builder()
                        .id(r.getId())
                        .name(r.getName())
                        .description(r.getDescription())
                        .isInitial(Boolean.TRUE.equals(r.getIsInitial()))
                        .isFinal(Boolean.TRUE.equals(r.getIsFinal()))
                        .build());
    }

    @Override
    public boolean existsByIdAndIsInitial(UUID id) {
        return ctx.fetchExists(WORK_ORDER_STATUSES,
                WORK_ORDER_STATUSES.ID.eq(id).and(WORK_ORDER_STATUSES.IS_INITIAL.isTrue()));
    }

    @Override
    public List<WorkOrderPriority> findAllPriorities() {
        return ctx.selectFrom(WORK_ORDER_PRIORITIES)
                .orderBy(WORK_ORDER_PRIORITIES.NAME.asc())
                .fetch(r -> WorkOrderPriority.builder()
                        .id(r.getId())
                        .name(r.getName())
                        .description(r.getDescription())
                        .build());
    }

    @Override
    public List<WorkOrderEventType> findAllEventTypes() {
        return ctx.selectFrom(WORK_ORDER_EVENT_TYPES)
                .orderBy(WORK_ORDER_EVENT_TYPES.NAME.asc())
                .fetch(r -> WorkOrderEventType.builder()
                        .id(r.getId())
                        .name(r.getName())
                        .description(r.getDescription())
                        .build());
    }
}
