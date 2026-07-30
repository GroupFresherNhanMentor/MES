package fpt.qn.mes.inventory.infrastructure.persistence;

import static fpt.qn.mes.jooq.Tables.LOT_TYPES;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;

import fpt.qn.mes.common.repository.BaseRepository;
import fpt.qn.mes.inventory.domain.entities.LotType;
import fpt.qn.mes.inventory.domain.repository.LotTypeRepository;
import fpt.qn.mes.inventory.domain.repository.criteria.LotTypeSearchCriteria;
import fpt.qn.mes.jooq.tables.records.LotTypesRecord;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@Repository
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class LotTypePersistenceAdapter extends BaseRepository<LotTypesRecord> implements LotTypeRepository {

    public LotTypePersistenceAdapter(DSLContext ctx) {
        super(ctx, LOT_TYPES);
    }

    @Override
    public Optional<LotType> findById(UUID id) {
        if (id == null) {
            return Optional.empty();
        }
        return fetchById(id).map(r -> LotType.builder()
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
        return ctx.select(LOT_TYPES.ID)
                .from(LOT_TYPES)
                .where(LOT_TYPES.NAME.eq(name))
                .fetchOptional(LOT_TYPES.ID);
    }

    @Override
    public List<LotType> search(LotTypeSearchCriteria criteria) {
        Condition condition = buildCondition(criteria);
        int page = criteria != null ? criteria.getPage() : 0;
        int size = criteria != null ? criteria.getSize() : 20;

        return ctx.selectFrom(LOT_TYPES)
                .where(condition)
                .limit(size)
                .offset(page * size)
                .fetch(r -> LotType.builder()
                        .id(r.getId())
                        .name(r.getName())
                        .description(r.getDescription())
                        .build());
    }

    @Override
    public long count(LotTypeSearchCriteria criteria) {
        Condition condition = buildCondition(criteria);
        return ctx.fetchCount(ctx.selectFrom(LOT_TYPES).where(condition));
    }

    private Condition buildCondition(LotTypeSearchCriteria criteria) {
        if (criteria == null) {
            return DSL.noCondition();
        }
        List<Condition> conditions = new ArrayList<>();
        if (criteria.getQuery() != null && !criteria.getQuery().isBlank()) {
            String pattern = "%" + criteria.getQuery().trim() + "%";
            conditions.add(LOT_TYPES.NAME.likeIgnoreCase(pattern)
                    .or(LOT_TYPES.DESCRIPTION.likeIgnoreCase(pattern)));
        }
        if (criteria.getName() != null && !criteria.getName().isBlank()) {
            conditions.add(LOT_TYPES.NAME.eq(criteria.getName().trim()));
        }
        return conditions.stream().reduce(DSL.noCondition(), Condition::and);
    }
}
