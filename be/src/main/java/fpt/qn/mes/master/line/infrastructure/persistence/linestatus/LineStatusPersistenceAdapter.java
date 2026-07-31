package fpt.qn.mes.master.line.infrastructure.persistence.linestatus;

import static fpt.qn.mes.jooq.Tables.LINE_STATUSES;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;

import fpt.qn.mes.common.domainQuery.PaginationResult;
import fpt.qn.mes.common.repository.BaseRepository;
import fpt.qn.mes.jooq.tables.records.LineStatusesRecord;
import fpt.qn.mes.master.line.domain.entities.LineStatus;
import fpt.qn.mes.master.line.domain.repository.LineStatusRepository;
import fpt.qn.mes.master.line.domain.repository.criteria.LineStatusSearchCriteria;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@Repository
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class LineStatusPersistenceAdapter extends BaseRepository<LineStatusesRecord> implements LineStatusRepository {

    LineStatusRecordMapper mapper;

    public LineStatusPersistenceAdapter(DSLContext ctx, LineStatusRecordMapper mapper) {
        super(ctx, LINE_STATUSES);
        this.mapper = mapper;
    }

    @Override
    public Optional<LineStatus> findById(UUID id) {
        return ctx.selectFrom(LINE_STATUSES)
                .where(LINE_STATUSES.ID.eq(id))
                .fetchOptional(r -> mapper.toDomain(r));
    }

    @Override
    public Optional<LineStatus> findByName(String name) {
        return ctx.selectFrom(LINE_STATUSES)
                .where(LINE_STATUSES.NAME.eq(name))
                .fetchOptional(r -> mapper.toDomain(r));
    }

    @Override
    public boolean existsById(UUID id) {
        return ctx.fetchExists(LINE_STATUSES, LINE_STATUSES.ID.eq(id));
    }

    @Override
    public boolean existsByName(String name) {
        return ctx.fetchExists(LINE_STATUSES, LINE_STATUSES.NAME.eq(name));
    }

    @Override
    public LineStatus save(LineStatus status) {
        LineStatusesRecord r = mapper.toRecord(status);
        ctx.insertInto(LINE_STATUSES).set(r)
                .onConflict(LINE_STATUSES.ID).doUpdate().set(r)
                .execute();
        return status;
    }

    @Override
    public PaginationResult<LineStatus> search(LineStatusSearchCriteria criteria) {
        Condition condition = DSL.noCondition();
        if (criteria.getName() != null && !criteria.getName().isBlank()) {
            condition = condition.and(LINE_STATUSES.NAME.containsIgnoreCase(criteria.getName()));
        }
        long total = ctx.fetchCount(LINE_STATUSES, condition);
        List<LineStatus> items = ctx.selectFrom(LINE_STATUSES)
                .where(condition)
                .limit(criteria.getSize())
                .offset((long) criteria.getPage() * criteria.getSize())
                .fetch(r -> mapper.toDomain(r));
        return PaginationResult.<LineStatus>builder().total(total).items(items).build();
    }
}
