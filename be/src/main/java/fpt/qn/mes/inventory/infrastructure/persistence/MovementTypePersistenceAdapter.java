package fpt.qn.mes.inventory.infrastructure.persistence;

import static fpt.qn.mes.jooq.Tables.MOVEMENT_TYPES;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;

import fpt.qn.mes.common.repository.BaseRepository;
import fpt.qn.mes.inventory.domain.entities.MovementType;
import fpt.qn.mes.inventory.domain.repository.MovementTypeRepository;
import fpt.qn.mes.inventory.domain.repository.criteria.MovementTypeSearchCriteria;
import fpt.qn.mes.jooq.tables.records.MovementTypesRecord;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@Repository
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MovementTypePersistenceAdapter extends BaseRepository<MovementTypesRecord> implements MovementTypeRepository {

    public MovementTypePersistenceAdapter(DSLContext ctx) {
        super(ctx, MOVEMENT_TYPES);
    }

    @Override
    public Optional<MovementType> findById(UUID id) {
        if (id == null) {
            return Optional.empty();
        }
        return fetchById(id).map(r -> MovementType.builder()
                .id(r.getId())
                .name(r.getName())
                .description(r.getDescription())
                .build());
    }

    @Override
    public Optional<UUID> findIdByName(String name) {
        if (name == null || name.isBlank()) {
            return Optional.empty();
        }
        return ctx.select(MOVEMENT_TYPES.ID)
                .from(MOVEMENT_TYPES)
                .where(MOVEMENT_TYPES.NAME.eq(name))
                .fetchOptional(MOVEMENT_TYPES.ID);
    }

    @Override
    public List<MovementType> search(MovementTypeSearchCriteria criteria) {
        Condition condition = buildCondition(criteria);
        int page = criteria != null ? criteria.getPage() : 0;
        int size = criteria != null ? criteria.getSize() : 20;

        return ctx.selectFrom(MOVEMENT_TYPES)
                .where(condition)
                .limit(size)
                .offset(page * size)
                .fetch(r -> MovementType.builder()
                        .id(r.getId())
                        .name(r.getName())
                        .description(r.getDescription())
                        .build());
    }

    @Override
    public long count(MovementTypeSearchCriteria criteria) {
        Condition condition = buildCondition(criteria);
        return ctx.fetchCount(ctx.selectFrom(MOVEMENT_TYPES).where(condition));
    }

    private Condition buildCondition(MovementTypeSearchCriteria criteria) {
        if (criteria == null) {
            return DSL.noCondition();
        }
        List<Condition> conditions = new ArrayList<>();
        if (criteria.getQuery() != null && !criteria.getQuery().isBlank()) {
            String pattern = "%" + criteria.getQuery().trim() + "%";
            conditions.add(MOVEMENT_TYPES.NAME.likeIgnoreCase(pattern)
                    .or(MOVEMENT_TYPES.DESCRIPTION.likeIgnoreCase(pattern)));
        }
        if (criteria.getName() != null && !criteria.getName().isBlank()) {
            conditions.add(MOVEMENT_TYPES.NAME.eq(criteria.getName().trim()));
        }
        return conditions.stream().reduce(DSL.noCondition(), Condition::and);
    }
}
