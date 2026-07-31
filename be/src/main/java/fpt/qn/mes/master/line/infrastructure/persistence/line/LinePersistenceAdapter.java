package fpt.qn.mes.master.line.infrastructure.persistence.line;

import static fpt.qn.mes.jooq.Tables.LINE_STATUSES;
import static fpt.qn.mes.jooq.Tables.PRODUCTION_LINES;
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
import fpt.qn.mes.jooq.tables.Users;
import fpt.qn.mes.jooq.tables.records.ProductionLinesRecord;
import fpt.qn.mes.master.line.domain.entities.Line;
import fpt.qn.mes.master.line.domain.repository.LineRepository;
import fpt.qn.mes.master.line.domain.repository.criteria.LineSearchCriteria;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@Repository
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class LinePersistenceAdapter extends BaseRepository<ProductionLinesRecord> implements LineRepository {

    private static final Users CREATOR = USERS.as("creator");
    private static final Users UPDATER = USERS.as("updater");

    private static final Map<String, Field<?>> SORT_FIELDS = Map.of(
        "name", PRODUCTION_LINES.NAME,
        "code", PRODUCTION_LINES.CODE,
        "createdAt", PRODUCTION_LINES.CREATED_AT
    );
    private static final Field<?> DEFAULT_SORT_FIELD = PRODUCTION_LINES.CREATED_AT;

    LineRecordMapper mapper;

    public LinePersistenceAdapter(DSLContext ctx, LineRecordMapper mapper) {
        super(ctx, PRODUCTION_LINES);
        this.mapper = mapper;
    }

    @Override
    public Optional<Line> findById(UUID id) {
        return ctx.select()
                .from(PRODUCTION_LINES)
                .leftJoin(LINE_STATUSES).on(LINE_STATUSES.ID.eq(PRODUCTION_LINES.LINE_STATUS_ID))
                .leftJoin(CREATOR).on(PRODUCTION_LINES.CREATED_BY.eq(CREATOR.ID))
                .leftJoin(UPDATER).on(PRODUCTION_LINES.UPDATED_BY.eq(UPDATER.ID))
                .where(PRODUCTION_LINES.ID.eq(id))
                .fetchOptional(r -> mapper.toDomain(r.into(PRODUCTION_LINES), r.into(LINE_STATUSES), r.into(CREATOR), r.into(UPDATER)));
    }

    @Override
    public Line save(Line line) {
        ProductionLinesRecord r = mapper.toRecord(line);
        ctx.insertInto(PRODUCTION_LINES).set(r)
            .onConflict(PRODUCTION_LINES.ID).doUpdate().set(r)
            .execute();
        return line;
    }

    @Override
    public Line update(Line line) {
        ProductionLinesRecord r = mapper.toRecord(line);
        ctx.update(PRODUCTION_LINES).set(r).where(PRODUCTION_LINES.ID.eq(r.getId())).execute();
        return line;
    }

    @Override
    public boolean existsById(UUID id) {
        return ctx.fetchExists(PRODUCTION_LINES, PRODUCTION_LINES.ID.eq(id));
    }

    @Override
    public boolean existsByCode(String code) {
        return ctx.fetchExists(PRODUCTION_LINES, PRODUCTION_LINES.CODE.eq(code));
    }

    @Override
    public PaginationResult<Line> search(LineSearchCriteria criteria) {
        Condition condition = buildCondition(criteria);
        List<SortField<?>> orderBy = SortUtils.resolveSorts(criteria.getSort(), SORT_FIELDS, DEFAULT_SORT_FIELD);
        
        long total = ctx.fetchCount(PRODUCTION_LINES, condition);
        List<Line> items = ctx.select()
                .from(PRODUCTION_LINES)
                .leftJoin(LINE_STATUSES).on(LINE_STATUSES.ID.eq(PRODUCTION_LINES.LINE_STATUS_ID))
                .leftJoin(CREATOR).on(PRODUCTION_LINES.CREATED_BY.eq(CREATOR.ID))
                .leftJoin(UPDATER).on(PRODUCTION_LINES.UPDATED_BY.eq(UPDATER.ID))
                .where(condition)
                .orderBy(orderBy)
                .limit(criteria.getSize())
                .offset((long) criteria.getPage() * criteria.getSize())
                .fetch(r -> mapper.toDomain(r.into(PRODUCTION_LINES), r.into(LINE_STATUSES), r.into(CREATOR), r.into(UPDATER)));
                
        return PaginationResult.<Line>builder().total(total).items(items).build();
    }

    private Condition buildCondition(LineSearchCriteria criteria) {
        Condition condition = DSL.noCondition();
        if (criteria.getCode() != null && !criteria.getCode().isBlank()) {
    condition = condition.and(PRODUCTION_LINES.CODE.containsIgnoreCase(criteria.getCode())
        .or(PRODUCTION_LINES.NAME.containsIgnoreCase(criteria.getCode())));
} else if (criteria.getName() != null && !criteria.getName().isBlank()) {
    condition = condition.and(PRODUCTION_LINES.NAME.containsIgnoreCase(criteria.getName()));
}
        if (criteria.getLineStatusId() != null) {
            condition = condition.and(PRODUCTION_LINES.LINE_STATUS_ID.eq(criteria.getLineStatusId()));
        }
        return condition;
    }
}
