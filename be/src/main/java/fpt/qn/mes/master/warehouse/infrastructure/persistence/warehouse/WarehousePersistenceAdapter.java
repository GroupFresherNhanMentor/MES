package fpt.qn.mes.master.warehouse.infrastructure.persistence.warehouse;

import static fpt.qn.mes.jooq.Tables.WAREHOUSE_STATUSES;
import static fpt.qn.mes.jooq.Tables.WAREHOUSES;
import static fpt.qn.mes.jooq.Tables.USERS;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.SortField;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;

import fpt.qn.mes.common.domainQuery.PaginationResult;
import fpt.qn.mes.common.repository.BaseRepository;
import fpt.qn.mes.common.repository.SortUtils;
import fpt.qn.mes.jooq.tables.Users;
import fpt.qn.mes.jooq.tables.records.WarehousesRecord;
import fpt.qn.mes.master.warehouse.domain.entities.Warehouse;
import fpt.qn.mes.master.warehouse.domain.repository.WarehouseRepository;
import fpt.qn.mes.master.warehouse.domain.repository.criteria.WarehouseSearchCriteria;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@Repository
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class WarehousePersistenceAdapter extends BaseRepository<WarehousesRecord> implements WarehouseRepository {

    private static final Users CREATOR = USERS.as("creator");
    private static final Users UPDATER = USERS.as("updater");

    private static final Map<String, Field<?>> SORT_FIELDS = Map.of(
        "name", WAREHOUSES.NAME,
        "code", WAREHOUSES.CODE,
        "createdAt", WAREHOUSES.CREATED_AT
    );
    private static final Field<?> DEFAULT_SORT_FIELD = WAREHOUSES.CREATED_AT;

    WarehouseRecordMapper mapper;

    public WarehousePersistenceAdapter(DSLContext ctx, WarehouseRecordMapper mapper) {
        super(ctx, WAREHOUSES);
        this.mapper = mapper;
    }

    @Override
    public Optional<Warehouse> findById(UUID id) {
        return ctx.select()
                .from(WAREHOUSES)
                .leftJoin(WAREHOUSE_STATUSES).on(WAREHOUSE_STATUSES.ID.eq(WAREHOUSES.WAREHOUSE_STATUS_ID))
                .leftJoin(CREATOR).on(WAREHOUSES.CREATED_BY.eq(CREATOR.ID))
                .leftJoin(UPDATER).on(WAREHOUSES.UPDATED_BY.eq(UPDATER.ID))
                .where(WAREHOUSES.ID.eq(id))
                .fetchOptional(r -> mapper.toDomain(r.into(WAREHOUSES), r.into(WAREHOUSE_STATUSES), r.into(CREATOR), r.into(UPDATER)));
    }

    @Override
    public Optional<Warehouse> findByCode(String code) {
        return ctx.select()
                .from(WAREHOUSES)
                .leftJoin(WAREHOUSE_STATUSES).on(WAREHOUSE_STATUSES.ID.eq(WAREHOUSES.WAREHOUSE_STATUS_ID))
                .leftJoin(CREATOR).on(WAREHOUSES.CREATED_BY.eq(CREATOR.ID))
                .leftJoin(UPDATER).on(WAREHOUSES.UPDATED_BY.eq(UPDATER.ID))
                .where(WAREHOUSES.CODE.eq(code))
                .fetchOptional(r -> mapper.toDomain(r.into(WAREHOUSES), r.into(WAREHOUSE_STATUSES), r.into(CREATOR), r.into(UPDATER)));
    }

    @Override
    public List<Warehouse> findByIds(Collection<UUID> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return ctx.select()
                .from(WAREHOUSES)
                .leftJoin(WAREHOUSE_STATUSES).on(WAREHOUSE_STATUSES.ID.eq(WAREHOUSES.WAREHOUSE_STATUS_ID))
                .leftJoin(CREATOR).on(WAREHOUSES.CREATED_BY.eq(CREATOR.ID))
                .leftJoin(UPDATER).on(WAREHOUSES.UPDATED_BY.eq(UPDATER.ID))
                .where(WAREHOUSES.ID.in(ids))
                .fetch(r -> mapper.toDomain(r.into(WAREHOUSES), r.into(WAREHOUSE_STATUSES), r.into(CREATOR), r.into(UPDATER)));
    }

    @Override
    public Warehouse save(Warehouse warehouse) {
        WarehousesRecord r = mapper.toRecord(warehouse);
        ctx.insertInto(WAREHOUSES).set(r)
            .onConflict(WAREHOUSES.ID).doUpdate().set(r)
            .execute();
        return warehouse;
    }

    @Override
    public Warehouse update(Warehouse warehouse) {
        WarehousesRecord r = mapper.toRecord(warehouse);
        ctx.update(WAREHOUSES).set(r).where(WAREHOUSES.ID.eq(r.getId())).execute();
        return warehouse;
    }

    @Override
    public boolean existsById(UUID id) {
        return ctx.fetchExists(WAREHOUSES, WAREHOUSES.ID.eq(id));
    }

    @Override
    public boolean existsByCode(String code) {
        return ctx.fetchExists(WAREHOUSES, WAREHOUSES.CODE.eq(code));
    }

    @Override
    public PaginationResult<Warehouse> search(WarehouseSearchCriteria criteria) {
        Condition condition = buildCondition(criteria);
        List<SortField<?>> orderBy = SortUtils.resolveSorts(criteria.getSort(), SORT_FIELDS, DEFAULT_SORT_FIELD);
        
        long total = ctx.fetchCount(WAREHOUSES, condition);
        List<Warehouse> items = ctx.select()
                .from(WAREHOUSES)
                .leftJoin(WAREHOUSE_STATUSES).on(WAREHOUSE_STATUSES.ID.eq(WAREHOUSES.WAREHOUSE_STATUS_ID))
                .leftJoin(CREATOR).on(WAREHOUSES.CREATED_BY.eq(CREATOR.ID))
                .leftJoin(UPDATER).on(WAREHOUSES.UPDATED_BY.eq(UPDATER.ID))
                .where(condition)
                .orderBy(orderBy)
                .limit(criteria.getSize())
                .offset((long) criteria.getPage() * criteria.getSize())
                .fetch(r -> mapper.toDomain(r.into(WAREHOUSES), r.into(WAREHOUSE_STATUSES), r.into(CREATOR), r.into(UPDATER)));
                
        return PaginationResult.<Warehouse>builder().total(total).items(items).build();
    }

    private Condition buildCondition(WarehouseSearchCriteria criteria) {
        Condition condition = DSL.noCondition();
        if (criteria.getCode() != null && !criteria.getCode().isBlank()) {
            condition = condition.and(WAREHOUSES.CODE.containsIgnoreCase(criteria.getCode()));
        }
        if (criteria.getName() != null && !criteria.getName().isBlank()) {
            condition = condition.and(WAREHOUSES.NAME.containsIgnoreCase(criteria.getName()));
        }
        if (criteria.getWarehouseStatusId() != null) {
            condition = condition.and(WAREHOUSES.WAREHOUSE_STATUS_ID.eq(criteria.getWarehouseStatusId()));
        }
        return condition;
    }
}
