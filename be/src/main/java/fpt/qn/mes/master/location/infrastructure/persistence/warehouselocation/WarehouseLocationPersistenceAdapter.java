package fpt.qn.mes.master.location.infrastructure.persistence.warehouselocation;

import static fpt.qn.mes.jooq.Tables.LOCATION_STATUSES;
import static fpt.qn.mes.jooq.Tables.WAREHOUSE_LOCATIONS;
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
import fpt.qn.mes.jooq.tables.records.WarehouseLocationsRecord;
import fpt.qn.mes.master.location.domain.entities.WarehouseLocation;
import fpt.qn.mes.master.location.domain.repository.WarehouseLocationRepository;
import fpt.qn.mes.master.location.domain.repository.criteria.WarehouseLocationSearchCriteria;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@Repository
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class WarehouseLocationPersistenceAdapter extends BaseRepository<WarehouseLocationsRecord> implements WarehouseLocationRepository {

    private static final Users CREATOR = USERS.as("creator");
    private static final Users UPDATER = USERS.as("updater");

    private static final Map<String, Field<?>> SORT_FIELDS = Map.of(
        "name", WAREHOUSE_LOCATIONS.NAME,
        "code", WAREHOUSE_LOCATIONS.CODE,
        "createdAt", WAREHOUSE_LOCATIONS.CREATED_AT
    );
    private static final Field<?> DEFAULT_SORT_FIELD = WAREHOUSE_LOCATIONS.CREATED_AT;

    WarehouseLocationRecordMapper mapper;

    public WarehouseLocationPersistenceAdapter(DSLContext ctx, WarehouseLocationRecordMapper mapper) {
        super(ctx, WAREHOUSE_LOCATIONS);
        this.mapper = mapper;
    }

    @Override
    public Optional<WarehouseLocation> findById(UUID id) {
        return ctx.select()
                .from(WAREHOUSE_LOCATIONS)
                .leftJoin(WAREHOUSES).on(WAREHOUSE_LOCATIONS.WAREHOUSE_ID.eq(WAREHOUSES.ID))
                .leftJoin(LOCATION_STATUSES).on(LOCATION_STATUSES.ID.eq(WAREHOUSE_LOCATIONS.LOCATION_STATUS_ID))
                .leftJoin(CREATOR).on(WAREHOUSE_LOCATIONS.CREATED_BY.eq(CREATOR.ID))
                .leftJoin(UPDATER).on(WAREHOUSE_LOCATIONS.UPDATED_BY.eq(UPDATER.ID))
                .where(WAREHOUSE_LOCATIONS.ID.eq(id))
                .fetchOptional(r -> mapper.toDomain(r.into(WAREHOUSE_LOCATIONS), r.into(WAREHOUSES), r.into(LOCATION_STATUSES), r.into(CREATOR), r.into(UPDATER)));
    }

    @Override
    public List<WarehouseLocation> findByIds(Collection<UUID> ids) {
        return ctx.select()
                .from(WAREHOUSE_LOCATIONS)
                .leftJoin(WAREHOUSES).on(WAREHOUSE_LOCATIONS.WAREHOUSE_ID.eq(WAREHOUSES.ID))
                .leftJoin(LOCATION_STATUSES).on(LOCATION_STATUSES.ID.eq(WAREHOUSE_LOCATIONS.LOCATION_STATUS_ID))
                .leftJoin(CREATOR).on(WAREHOUSE_LOCATIONS.CREATED_BY.eq(CREATOR.ID))
                .leftJoin(UPDATER).on(WAREHOUSE_LOCATIONS.UPDATED_BY.eq(UPDATER.ID))
                .where(WAREHOUSE_LOCATIONS.ID.in(ids))
                .fetch(r -> mapper.toDomain(r.into(WAREHOUSE_LOCATIONS), r.into(WAREHOUSES), r.into(LOCATION_STATUSES), r.into(CREATOR), r.into(UPDATER)));
    }

    @Override
    public WarehouseLocation save(WarehouseLocation location) {
        WarehouseLocationsRecord r = mapper.toRecord(location);
        ctx.insertInto(WAREHOUSE_LOCATIONS).set(r)
            .onConflict(WAREHOUSE_LOCATIONS.ID).doUpdate().set(r)
            .execute();
        return location;
    }

    @Override
    public WarehouseLocation update(WarehouseLocation location) {
        WarehouseLocationsRecord r = mapper.toRecord(location);
        ctx.update(WAREHOUSE_LOCATIONS).set(r).where(WAREHOUSE_LOCATIONS.ID.eq(r.getId())).execute();
        return location;
    }

    @Override
    public boolean existsById(UUID id) {
        return ctx.fetchExists(WAREHOUSE_LOCATIONS, WAREHOUSE_LOCATIONS.ID.eq(id));
    }

    @Override
    public boolean existsByWarehouseIdAndCode(UUID warehouseId, String code) {
        return ctx.fetchExists(WAREHOUSE_LOCATIONS, WAREHOUSE_LOCATIONS.WAREHOUSE_ID.eq(warehouseId).and(WAREHOUSE_LOCATIONS.CODE.eq(code)));
    }

    @Override
    public PaginationResult<WarehouseLocation> search(WarehouseLocationSearchCriteria criteria) {
        Condition condition = buildCondition(criteria);
        List<SortField<?>> orderBy = SortUtils.resolveSorts(criteria.getSort(), SORT_FIELDS, DEFAULT_SORT_FIELD);
        
        long total = ctx.fetchCount(WAREHOUSE_LOCATIONS, condition);
        List<WarehouseLocation> items = ctx.select()
                .from(WAREHOUSE_LOCATIONS)
                .leftJoin(WAREHOUSES).on(WAREHOUSE_LOCATIONS.WAREHOUSE_ID.eq(WAREHOUSES.ID))
                .leftJoin(LOCATION_STATUSES).on(LOCATION_STATUSES.ID.eq(WAREHOUSE_LOCATIONS.LOCATION_STATUS_ID))
                .leftJoin(CREATOR).on(WAREHOUSE_LOCATIONS.CREATED_BY.eq(CREATOR.ID))
                .leftJoin(UPDATER).on(WAREHOUSE_LOCATIONS.UPDATED_BY.eq(UPDATER.ID))
                .where(condition)
                .orderBy(orderBy)
                .limit(criteria.getSize())
                .offset((long) criteria.getPage() * criteria.getSize())
                .fetch(r -> mapper.toDomain(r.into(WAREHOUSE_LOCATIONS), r.into(WAREHOUSES), r.into(LOCATION_STATUSES), r.into(CREATOR), r.into(UPDATER)));
                
        return PaginationResult.<WarehouseLocation>builder().total(total).items(items).build();
    }

    private Condition buildCondition(WarehouseLocationSearchCriteria criteria) {
        Condition condition = DSL.noCondition();
        if (criteria.getWarehouseId() != null) {
            condition = condition.and(WAREHOUSE_LOCATIONS.WAREHOUSE_ID.eq(criteria.getWarehouseId()));
        }
        if (criteria.getCode() != null && !criteria.getCode().isBlank()) {
            condition = condition.and(WAREHOUSE_LOCATIONS.CODE.containsIgnoreCase(criteria.getCode()));
        }
        if (criteria.getName() != null && !criteria.getName().isBlank()) {
            condition = condition.and(WAREHOUSE_LOCATIONS.NAME.containsIgnoreCase(criteria.getName()));
        }
        if (criteria.getLocationStatusId() != null) {
            condition = condition.and(WAREHOUSE_LOCATIONS.LOCATION_STATUS_ID.eq(criteria.getLocationStatusId()));
        }
        return condition;
    }
}
