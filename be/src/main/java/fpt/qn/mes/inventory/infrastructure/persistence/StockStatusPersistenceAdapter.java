package fpt.qn.mes.inventory.infrastructure.persistence;

import static fpt.qn.mes.jooq.Tables.STOCK_STATUSES;
import static fpt.qn.mes.jooq.Tables.USERS;

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
import fpt.qn.mes.inventory.domain.entities.StockStatus;
import fpt.qn.mes.inventory.domain.repository.StockStatusRepository;
import fpt.qn.mes.inventory.domain.repository.criteria.StockStatusSearchCriteria;
import fpt.qn.mes.jooq.tables.Users;
import fpt.qn.mes.jooq.tables.records.StockStatusesRecord;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@Repository
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class StockStatusPersistenceAdapter extends BaseRepository<StockStatusesRecord> implements StockStatusRepository {

    private static final Users CREATOR = USERS.as("creator");
    private static final Users UPDATER = USERS.as("updater");

    private static final Map<String, Field<?>> SORT_FIELDS = Map.of(
            "name", STOCK_STATUSES.NAME,
            "created_at", STOCK_STATUSES.CREATED_AT
    );
    private static final Field<?> DEFAULT_SORT_FIELD = STOCK_STATUSES.CREATED_AT;

    StockStatusRecordMapper mapper;

    public StockStatusPersistenceAdapter(DSLContext ctx, StockStatusRecordMapper mapper) {
        super(ctx, STOCK_STATUSES);
        this.mapper = mapper;
    }

    @Override
    public Optional<StockStatus> findById(UUID id) {
        return ctx.select()
                .from(STOCK_STATUSES)
                .leftJoin(CREATOR).on(CREATOR.ID.eq(STOCK_STATUSES.CREATED_BY))
                .leftJoin(UPDATER).on(UPDATER.ID.eq(STOCK_STATUSES.UPDATED_BY))
                .where(STOCK_STATUSES.ID.eq(id))
                .fetchOptional(r -> mapper.toDomain(r.into(STOCK_STATUSES), r.into(CREATOR), r.into(UPDATER)));
    }

    @Override
    public Optional<StockStatus> findByName(String name) {
        return ctx.select()
                .from(STOCK_STATUSES)
                .leftJoin(CREATOR).on(CREATOR.ID.eq(STOCK_STATUSES.CREATED_BY))
                .leftJoin(UPDATER).on(UPDATER.ID.eq(STOCK_STATUSES.UPDATED_BY))
                .where(STOCK_STATUSES.NAME.eq(name))
                .fetchOptional(r -> mapper.toDomain(r.into(STOCK_STATUSES), r.into(CREATOR), r.into(UPDATER)));
    }

    @Override
    public Optional<UUID> findIdByName(String name) {
        if (name == null || name.isBlank()) return Optional.empty();
        return ctx.select(STOCK_STATUSES.ID)
                .from(STOCK_STATUSES)
                .where(STOCK_STATUSES.NAME.eq(name))
                .fetchOptional(STOCK_STATUSES.ID);
    }

    @Override
    public boolean existsById(UUID id) {
        return ctx.fetchExists(STOCK_STATUSES, STOCK_STATUSES.ID.eq(id));
    }

    @Override
    public boolean existsByName(String name) {
        return ctx.fetchExists(STOCK_STATUSES, STOCK_STATUSES.NAME.eq(name));
    }

    @Override
    public StockStatus save(StockStatus stockStatus) {
        StockStatusesRecord r = mapper.toRecord(stockStatus);
        ctx.insertInto(STOCK_STATUSES).set(r)
                .onConflict(STOCK_STATUSES.ID).doUpdate().set(r)
                .execute();
        return stockStatus;
    }

    @Override
    public PaginationResult<StockStatus> search(StockStatusSearchCriteria criteria) {
        Condition condition = buildCondition(criteria);
        List<SortField<?>> orderBy = SortUtils.resolveSorts(criteria.getSort(), SORT_FIELDS, DEFAULT_SORT_FIELD);
        long total = ctx.fetchCount(STOCK_STATUSES, condition);
        List<StockStatus> items = ctx.select()
                .from(STOCK_STATUSES)
                .leftJoin(CREATOR).on(CREATOR.ID.eq(STOCK_STATUSES.CREATED_BY))
                .leftJoin(UPDATER).on(UPDATER.ID.eq(STOCK_STATUSES.UPDATED_BY))
                .where(condition)
                .orderBy(orderBy)
                .limit(criteria.getSize())
                .offset((long) criteria.getPage() * criteria.getSize())
                .fetch(r -> mapper.toDomain(r.into(STOCK_STATUSES), r.into(CREATOR), r.into(UPDATER)));
        return PaginationResult.<StockStatus>builder().total(total).items(items).build();
    }

    private Condition buildCondition(StockStatusSearchCriteria criteria) {
        Condition condition = DSL.noCondition();
        if (criteria.getQuery() != null && !criteria.getQuery().isBlank()) {
            condition = condition.and(
                    STOCK_STATUSES.NAME.containsIgnoreCase(criteria.getQuery())
                            .or(STOCK_STATUSES.DESCRIPTION.containsIgnoreCase(criteria.getQuery())));
        }
        if (criteria.getName() != null && !criteria.getName().isBlank()) {
            condition = condition.and(STOCK_STATUSES.NAME.containsIgnoreCase(criteria.getName()));
        }
        return condition;
    }
}
