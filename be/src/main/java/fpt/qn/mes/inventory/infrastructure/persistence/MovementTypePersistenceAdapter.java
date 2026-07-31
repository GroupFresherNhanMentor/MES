package fpt.qn.mes.inventory.infrastructure.persistence;

import static fpt.qn.mes.jooq.Tables.MOVEMENT_TYPES;

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
import fpt.qn.mes.inventory.domain.entities.MovementType;
import fpt.qn.mes.inventory.domain.repository.MovementTypeRepository;
import fpt.qn.mes.inventory.domain.repository.criteria.MovementTypeSearchCriteria;
import fpt.qn.mes.jooq.tables.records.MovementTypesRecord;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@Repository
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MovementTypePersistenceAdapter extends BaseRepository<MovementTypesRecord> implements MovementTypeRepository {

    private static final Map<String, Field<?>> SORT_FIELDS = Map.of(
            "name", MOVEMENT_TYPES.NAME,
            "created_at", MOVEMENT_TYPES.CREATED_AT
    );
    private static final Field<?> DEFAULT_SORT_FIELD = MOVEMENT_TYPES.CREATED_AT;

    MovementTypeRecordMapper mapper;

    public MovementTypePersistenceAdapter(DSLContext ctx, MovementTypeRecordMapper mapper) {
        super(ctx, MOVEMENT_TYPES);
        this.mapper = mapper;
    }

    @Override
    public Optional<MovementType> findById(UUID id) {
        return ctx.selectFrom(MOVEMENT_TYPES)
                .where(MOVEMENT_TYPES.ID.eq(id))
                .fetchOptional(r -> mapper.toDomain(r));
    }

    @Override
    public Optional<MovementType> findByName(String name) {
        return ctx.selectFrom(MOVEMENT_TYPES)
                .where(MOVEMENT_TYPES.NAME.eq(name))
                .fetchOptional(r -> mapper.toDomain(r));
    }

    @Override
    public Optional<UUID> findIdByName(String name) {
        if (name == null || name.isBlank()) return Optional.empty();
        return ctx.select(MOVEMENT_TYPES.ID)
                .from(MOVEMENT_TYPES)
                .where(MOVEMENT_TYPES.NAME.eq(name))
                .fetchOptional(MOVEMENT_TYPES.ID);
    }

    @Override
    public boolean existsById(UUID id) {
        return ctx.fetchExists(MOVEMENT_TYPES, MOVEMENT_TYPES.ID.eq(id));
    }

    @Override
    public boolean existsByName(String name) {
        return ctx.fetchExists(MOVEMENT_TYPES, MOVEMENT_TYPES.NAME.eq(name));
    }

    @Override
    public MovementType save(MovementType movementType) {
        MovementTypesRecord r = mapper.toRecord(movementType);
        ctx.insertInto(MOVEMENT_TYPES).set(r)
                .onConflict(MOVEMENT_TYPES.ID).doUpdate().set(r)
                .execute();
        return movementType;
    }

    @Override
    public PaginationResult<MovementType> search(MovementTypeSearchCriteria criteria) {
        Condition condition = buildCondition(criteria);
        List<SortField<?>> orderBy = SortUtils.resolveSorts(criteria.getSort(), SORT_FIELDS, DEFAULT_SORT_FIELD);
        long total = ctx.fetchCount(MOVEMENT_TYPES, condition);
        List<MovementType> items = ctx.selectFrom(MOVEMENT_TYPES)
                .where(condition)
                .orderBy(orderBy)
                .limit(criteria.getSize())
                .offset((long) criteria.getPage() * criteria.getSize())
                .fetch(r -> mapper.toDomain(r));
        return PaginationResult.<MovementType>builder().total(total).items(items).build();
    }

    private Condition buildCondition(MovementTypeSearchCriteria criteria) {
        Condition condition = DSL.noCondition();
        if (criteria.getQuery() != null && !criteria.getQuery().isBlank()) {
            condition = condition.and(
                    MOVEMENT_TYPES.NAME.containsIgnoreCase(criteria.getQuery())
                            .or(MOVEMENT_TYPES.DESCRIPTION.containsIgnoreCase(criteria.getQuery())));
        }
        if (criteria.getName() != null && !criteria.getName().isBlank()) {
            condition = condition.and(MOVEMENT_TYPES.NAME.containsIgnoreCase(criteria.getName()));
        }
        return condition;
    }
}
