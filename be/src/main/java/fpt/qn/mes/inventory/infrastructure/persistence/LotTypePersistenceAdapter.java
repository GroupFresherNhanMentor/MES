package fpt.qn.mes.inventory.infrastructure.persistence;

import static fpt.qn.mes.jooq.Tables.LOT_TYPES;
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
import fpt.qn.mes.inventory.domain.entities.LotType;
import fpt.qn.mes.inventory.domain.repository.LotTypeRepository;
import fpt.qn.mes.inventory.domain.repository.criteria.LotTypeSearchCriteria;
import fpt.qn.mes.jooq.tables.Users;
import fpt.qn.mes.jooq.tables.records.LotTypesRecord;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@Repository
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class LotTypePersistenceAdapter extends BaseRepository<LotTypesRecord> implements LotTypeRepository {

    private static final Users CREATOR = USERS.as("creator");
    private static final Users UPDATER = USERS.as("updater");

    private static final Map<String, Field<?>> SORT_FIELDS = Map.of(
            "name", LOT_TYPES.NAME,
            "created_at", LOT_TYPES.CREATED_AT
    );
    private static final Field<?> DEFAULT_SORT_FIELD = LOT_TYPES.CREATED_AT;

    LotTypeRecordMapper mapper;

    public LotTypePersistenceAdapter(DSLContext ctx, LotTypeRecordMapper mapper) {
        super(ctx, LOT_TYPES);
        this.mapper = mapper;
    }

    @Override
    public Optional<LotType> findById(UUID id) {
        return ctx.select()
                .from(LOT_TYPES)
                .leftJoin(CREATOR).on(CREATOR.ID.eq(LOT_TYPES.CREATED_BY))
                .leftJoin(UPDATER).on(UPDATER.ID.eq(LOT_TYPES.UPDATED_BY))
                .where(LOT_TYPES.ID.eq(id))
                .fetchOptional(r -> mapper.toDomain(r.into(LOT_TYPES), r.into(CREATOR), r.into(UPDATER)));
    }

    @Override
    public Optional<LotType> findByName(String name) {
        return ctx.select()
                .from(LOT_TYPES)
                .leftJoin(CREATOR).on(CREATOR.ID.eq(LOT_TYPES.CREATED_BY))
                .leftJoin(UPDATER).on(UPDATER.ID.eq(LOT_TYPES.UPDATED_BY))
                .where(LOT_TYPES.NAME.eq(name))
                .fetchOptional(r -> mapper.toDomain(r.into(LOT_TYPES), r.into(CREATOR), r.into(UPDATER)));
    }

    @Override
    public Optional<UUID> findIdByName(String name) {
        if (name == null || name.isBlank()) return Optional.empty();
        return ctx.select(LOT_TYPES.ID)
                .from(LOT_TYPES)
                .where(LOT_TYPES.NAME.eq(name))
                .fetchOptional(LOT_TYPES.ID);
    }

    @Override
    public boolean existsById(UUID id) {
        return ctx.fetchExists(LOT_TYPES, LOT_TYPES.ID.eq(id));
    }

    @Override
    public boolean existsByName(String name) {
        return ctx.fetchExists(LOT_TYPES, LOT_TYPES.NAME.eq(name));
    }

    @Override
    public LotType save(LotType lotType) {
        LotTypesRecord r = mapper.toRecord(lotType);
        ctx.insertInto(LOT_TYPES).set(r)
                .onConflict(LOT_TYPES.ID).doUpdate().set(r)
                .execute();
        return lotType;
    }

    @Override
    public PaginationResult<LotType> search(LotTypeSearchCriteria criteria) {
        Condition condition = buildCondition(criteria);
        List<SortField<?>> orderBy = SortUtils.resolveSorts(criteria.getSort(), SORT_FIELDS, DEFAULT_SORT_FIELD);
        long total = ctx.fetchCount(LOT_TYPES, condition);
        List<LotType> items = ctx.select()
                .from(LOT_TYPES)
                .leftJoin(CREATOR).on(CREATOR.ID.eq(LOT_TYPES.CREATED_BY))
                .leftJoin(UPDATER).on(UPDATER.ID.eq(LOT_TYPES.UPDATED_BY))
                .where(condition)
                .orderBy(orderBy)
                .limit(criteria.getSize())
                .offset((long) criteria.getPage() * criteria.getSize())
                .fetch(r -> mapper.toDomain(r.into(LOT_TYPES), r.into(CREATOR), r.into(UPDATER)));
        return PaginationResult.<LotType>builder().total(total).items(items).build();
    }

    private Condition buildCondition(LotTypeSearchCriteria criteria) {
        Condition condition = DSL.noCondition();
        if (criteria.getQuery() != null && !criteria.getQuery().isBlank()) {
            condition = condition.and(
                    LOT_TYPES.NAME.containsIgnoreCase(criteria.getQuery())
                            .or(LOT_TYPES.DESCRIPTION.containsIgnoreCase(criteria.getQuery())));
        }
        if (criteria.getName() != null && !criteria.getName().isBlank()) {
            condition = condition.and(LOT_TYPES.NAME.containsIgnoreCase(criteria.getName()));
        }
        return condition;
    }
}
