package fpt.qn.mes.inventory.infrastructure.persistence;

import static fpt.qn.mes.jooq.Tables.STOCK_STATUSES;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;

import fpt.qn.mes.common.repository.BaseRepository;
import fpt.qn.mes.inventory.domain.entities.StockStatus;
import fpt.qn.mes.inventory.domain.repository.StockStatusRepository;
import fpt.qn.mes.inventory.domain.repository.criteria.StockStatusSearchCriteria;
import fpt.qn.mes.jooq.tables.records.StockStatusesRecord;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@Repository
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class StockStatusPersistenceAdapter extends BaseRepository<StockStatusesRecord> implements StockStatusRepository {

    public StockStatusPersistenceAdapter(DSLContext ctx) {
        super(ctx, STOCK_STATUSES);
    }

    @Override
    public Optional<StockStatus> findById(UUID id) {
        if (id == null) {
            return Optional.empty();
        }
        return fetchById(id).map(r -> StockStatus.builder()
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
        return ctx.select(STOCK_STATUSES.ID)
                .from(STOCK_STATUSES)
                .where(STOCK_STATUSES.NAME.eq(name))
                .fetchOptional(STOCK_STATUSES.ID);
    }

    @Override
    public List<StockStatus> search(StockStatusSearchCriteria criteria) {
        Condition condition = buildCondition(criteria);
        int page = criteria != null ? criteria.getPage() : 0;
        int size = criteria != null ? criteria.getSize() : 20;

        return ctx.selectFrom(STOCK_STATUSES)
                .where(condition)
                .limit(size)
                .offset(page * size)
                .fetch(r -> StockStatus.builder()
                        .id(r.getId())
                        .name(r.getName())
                        .description(r.getDescription())
                        .build());
    }

    @Override
    public long count(StockStatusSearchCriteria criteria) {
        Condition condition = buildCondition(criteria);
        return ctx.fetchCount(ctx.selectFrom(STOCK_STATUSES).where(condition));
    }

    private Condition buildCondition(StockStatusSearchCriteria criteria) {
        if (criteria == null) {
            return DSL.noCondition();
        }
        List<Condition> conditions = new ArrayList<>();
        if (criteria.getQuery() != null && !criteria.getQuery().isBlank()) {
            String pattern = "%" + criteria.getQuery().trim() + "%";
            conditions.add(STOCK_STATUSES.NAME.likeIgnoreCase(pattern)
                    .or(STOCK_STATUSES.DESCRIPTION.likeIgnoreCase(pattern)));
        }
        if (criteria.getName() != null && !criteria.getName().isBlank()) {
            conditions.add(STOCK_STATUSES.NAME.eq(criteria.getName().trim()));
        }
        return conditions.stream().reduce(DSL.noCondition(), Condition::and);
    }
}
