package fpt.qn.mes.master.location.infrastructure.persistence.locationstatus;

import static fpt.qn.mes.jooq.Tables.LOCATION_STATUSES;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;

import fpt.qn.mes.common.domainQuery.PaginationResult;
import fpt.qn.mes.common.repository.BaseRepository;
import fpt.qn.mes.jooq.tables.records.LocationStatusesRecord;
import fpt.qn.mes.master.location.domain.entities.LocationStatus;
import fpt.qn.mes.master.location.domain.repository.LocationStatusRepository;
import fpt.qn.mes.master.location.domain.repository.criteria.LocationStatusSearchCriteria;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@Repository
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class LocationStatusPersistenceAdapter extends BaseRepository<LocationStatusesRecord> implements LocationStatusRepository {

    LocationStatusRecordMapper mapper;

    public LocationStatusPersistenceAdapter(DSLContext ctx, LocationStatusRecordMapper mapper) {
        super(ctx, LOCATION_STATUSES);
        this.mapper = mapper;
    }

    @Override
    public Optional<LocationStatus> findById(UUID id) {
        return ctx.selectFrom(LOCATION_STATUSES)
                .where(LOCATION_STATUSES.ID.eq(id))
                .fetchOptional(r -> mapper.toDomain(r));
    }

    @Override
    public Optional<LocationStatus> findByName(String name) {
        return ctx.selectFrom(LOCATION_STATUSES)
                .where(LOCATION_STATUSES.NAME.eq(name))
                .fetchOptional(r -> mapper.toDomain(r));
    }

    @Override
    public boolean existsById(UUID id) {
        return ctx.fetchExists(LOCATION_STATUSES, LOCATION_STATUSES.ID.eq(id));
    }

    @Override
    public boolean existsByName(String name) {
        return ctx.fetchExists(LOCATION_STATUSES, LOCATION_STATUSES.NAME.eq(name));
    }

    @Override
    public LocationStatus save(LocationStatus status) {
        LocationStatusesRecord r = mapper.toRecord(status);
        ctx.insertInto(LOCATION_STATUSES).set(r)
                .onConflict(LOCATION_STATUSES.ID).doUpdate().set(r)
                .execute();
        return status;
    }

    @Override
    public PaginationResult<LocationStatus> search(LocationStatusSearchCriteria criteria) {
        Condition condition = DSL.noCondition();
        if (criteria.getName() != null && !criteria.getName().isBlank()) {
            condition = condition.and(LOCATION_STATUSES.NAME.containsIgnoreCase(criteria.getName()));
        }
        long total = ctx.fetchCount(LOCATION_STATUSES, condition);
        List<LocationStatus> items = ctx.selectFrom(LOCATION_STATUSES)
                .where(condition)
                .limit(criteria.getSize())
                .offset((long) criteria.getPage() * criteria.getSize())
                .fetch(r -> mapper.toDomain(r));
        return PaginationResult.<LocationStatus>builder().total(total).items(items).build();
    }
}
