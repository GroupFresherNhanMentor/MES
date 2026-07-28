package fpt.qn.mes.quality.infrastructure.persistence;

import static fpt.qn.mes.jooq.Tables.QC_STATUSES;

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
import fpt.qn.mes.jooq.tables.records.QcStatusesRecord;
import fpt.qn.mes.quality.domain.entities.QcStatus;
import fpt.qn.mes.quality.domain.repository.QcStatusRepository;
import fpt.qn.mes.quality.domain.repository.criteria.QcStatusSearchCriteria;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@Repository
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class QcStatusPersistenceAdapter extends BaseRepository<QcStatusesRecord> implements QcStatusRepository {

    private static final Map<String, Field<?>> SORT_FIELDS = Map.of(
        "name",        QC_STATUSES.NAME,
        "description", QC_STATUSES.DESCRIPTION
    );
    private static final Field<?> DEFAULT_SORT_FIELD = QC_STATUSES.NAME;

    DSLContext dslCtx;
    QcStatusRecordMapper mapper;

    public QcStatusPersistenceAdapter(DSLContext ctx, QcStatusRecordMapper mapper) {
        super(ctx, QC_STATUSES);
        this.dslCtx = ctx;
        this.mapper = mapper;
    }

    @Override
    public Optional<QcStatus> findById(UUID id) {
        return dslCtx.selectFrom(QC_STATUSES)
            .where(QC_STATUSES.ID.eq(id))
            .fetchOptional(r -> mapper.toDomain(r));
    }

    @Override
    public List<QcStatus> findAll() {
        return dslCtx.selectFrom(QC_STATUSES)
            .orderBy(QC_STATUSES.NAME.asc())
            .fetch(r -> mapper.toDomain(r));
    }

    @Override
    public QcStatus save(QcStatus status) {
        QcStatusesRecord r = mapper.toRecord(status);
        if (r.getId() == null) r.setId(UUID.randomUUID());
        dslCtx.insertInto(QC_STATUSES)
            .set(r)
            .onConflict(QC_STATUSES.ID)
            .doUpdate()
            .set(r)
            .execute();
        return mapper.toDomain(r);
    }

    @Override
    public PaginationResult<QcStatus> search(QcStatusSearchCriteria criteria) {
        Condition condition = buildCondition(criteria);
        List<SortField<?>> orderBy = SortUtils.resolveSorts(criteria.getSort(), SORT_FIELDS, DEFAULT_SORT_FIELD);
        long total = dslCtx.fetchCount(QC_STATUSES, condition);
        List<QcStatus> items = dslCtx.selectFrom(QC_STATUSES)
            .where(condition)
            .orderBy(orderBy)
            .limit(criteria.getSize())
            .offset(criteria.getPage() * criteria.getSize())
            .fetch(r -> mapper.toDomain(r));
        return PaginationResult.<QcStatus>builder().total(total).items(items).build();
    }

    private Condition buildCondition(QcStatusSearchCriteria criteria) {
        Condition condition = DSL.noCondition();
        if (criteria.getName() != null && !criteria.getName().isBlank()) {
            condition = condition.and(QC_STATUSES.NAME.containsIgnoreCase(criteria.getName()));
        }
        return condition;
    }
}
