package fpt.qn.mes.master.warehouse.infrastructure.persistence.warehousestatus;

import static fpt.qn.mes.jooq.Tables.WAREHOUSE_STATUSES;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;

import fpt.qn.mes.common.domainQuery.PaginationResult;
import fpt.qn.mes.common.repository.BaseRepository;
import fpt.qn.mes.jooq.tables.records.WarehouseStatusesRecord;
import fpt.qn.mes.master.warehouse.domain.entities.WarehouseStatus;
import fpt.qn.mes.master.warehouse.domain.repository.WarehouseStatusRepository;
import fpt.qn.mes.master.warehouse.domain.repository.criteria.WarehouseStatusSearchCriteria;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@Repository
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class WarehouseStatusPersistenceAdapter extends BaseRepository<WarehouseStatusesRecord> implements WarehouseStatusRepository {

    WarehouseStatusRecordMapper mapper;

    public WarehouseStatusPersistenceAdapter(DSLContext ctx, WarehouseStatusRecordMapper mapper) {
        super(ctx, WAREHOUSE_STATUSES);
        this.mapper = mapper;
    }

    @Override
    public Optional<WarehouseStatus> findById(UUID id) {
        return ctx.selectFrom(WAREHOUSE_STATUSES)
                .where(WAREHOUSE_STATUSES.ID.eq(id))
                .fetchOptional(r -> mapper.toDomain(r));
    }

    @Override
    public Optional<WarehouseStatus> findByName(String name) {
        return ctx.selectFrom(WAREHOUSE_STATUSES)
                .where(WAREHOUSE_STATUSES.NAME.eq(name))
                .fetchOptional(r -> mapper.toDomain(r));
    }

    @Override
    public boolean existsById(UUID id) {
        return ctx.fetchExists(WAREHOUSE_STATUSES, WAREHOUSE_STATUSES.ID.eq(id));
    }

    @Override
    public boolean existsByName(String name) {
        return ctx.fetchExists(WAREHOUSE_STATUSES, WAREHOUSE_STATUSES.NAME.eq(name));
    }

    @Override
    public WarehouseStatus save(WarehouseStatus status) {
        WarehouseStatusesRecord r = mapper.toRecord(status);
        ctx.insertInto(WAREHOUSE_STATUSES).set(r)
                .onConflict(WAREHOUSE_STATUSES.ID).doUpdate().set(r)
                .execute();
        return status;
    }

    @Override
    public PaginationResult<WarehouseStatus> search(WarehouseStatusSearchCriteria criteria) {
        Condition condition = DSL.noCondition();
        if (criteria.getName() != null && !criteria.getName().isBlank()) {
            condition = condition.and(WAREHOUSE_STATUSES.NAME.containsIgnoreCase(criteria.getName()));
        }
        long total = ctx.fetchCount(WAREHOUSE_STATUSES, condition);
        List<WarehouseStatus> items = ctx.selectFrom(WAREHOUSE_STATUSES)
                .where(condition)
                .limit(criteria.getSize())
                .offset((long) criteria.getPage() * criteria.getSize())
                .fetch(r -> mapper.toDomain(r));
        return PaginationResult.<WarehouseStatus>builder().total(total).items(items).build();
    }
}
