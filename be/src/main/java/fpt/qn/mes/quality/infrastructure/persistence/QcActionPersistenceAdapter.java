package fpt.qn.mes.quality.infrastructure.persistence;

import static fpt.qn.mes.jooq.Tables.QC_ACTIONS;

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
import fpt.qn.mes.jooq.tables.records.QcActionsRecord;
import fpt.qn.mes.quality.domain.entities.QcAction;
import fpt.qn.mes.quality.domain.repository.QcActionRepository;
import fpt.qn.mes.quality.domain.repository.criteria.QcActionSearchCriteria;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@Repository
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class QcActionPersistenceAdapter extends BaseRepository<QcActionsRecord> implements QcActionRepository {

    private static final Map<String, Field<?>> SORT_FIELDS = Map.of(
        "name",        QC_ACTIONS.NAME,
        "description", QC_ACTIONS.DESCRIPTION
    );
    private static final Field<?> DEFAULT_SORT_FIELD = QC_ACTIONS.NAME;

    DSLContext dslCtx;
    QcActionRecordMapper mapper;

    public QcActionPersistenceAdapter(DSLContext ctx, QcActionRecordMapper mapper) {
        super(ctx, QC_ACTIONS);
        this.dslCtx = ctx;
        this.mapper = mapper;
    }

    @Override
    public Optional<QcAction> findById(UUID id) {
        return dslCtx.selectFrom(QC_ACTIONS)
            .where(QC_ACTIONS.ID.eq(id))
            .fetchOptional(r -> mapper.toDomain(r));
    }

    @Override
    public QcAction save(QcAction action) {
        QcActionsRecord r = mapper.toRecord(action);
        if (r.getId() == null) r.setId(UUID.randomUUID());
        dslCtx.insertInto(QC_ACTIONS)
            .set(r)
            .onConflict(QC_ACTIONS.ID)
            .doUpdate()
            .set(r)
            .execute();
        return mapper.toDomain(r);
    }

    @Override
    public PaginationResult<QcAction> search(QcActionSearchCriteria criteria) {
        Condition condition = buildCondition(criteria);
        List<SortField<?>> orderBy = SortUtils.resolveSorts(criteria.getSort(), SORT_FIELDS, DEFAULT_SORT_FIELD);
        long total = dslCtx.fetchCount(QC_ACTIONS, condition);
        List<QcAction> items = dslCtx.selectFrom(QC_ACTIONS)
            .where(condition)
            .orderBy(orderBy)
            .limit(criteria.getSize())
            .offset(criteria.getPage() * criteria.getSize())
            .fetch(r -> mapper.toDomain(r));
        return PaginationResult.<QcAction>builder().total(total).items(items).build();
    }

    private Condition buildCondition(QcActionSearchCriteria criteria) {
        Condition condition = DSL.noCondition();
        if (criteria.getName() != null && !criteria.getName().isBlank()) {
            condition = condition.and(QC_ACTIONS.NAME.containsIgnoreCase(criteria.getName()));
        }
        return condition;
    }
}
