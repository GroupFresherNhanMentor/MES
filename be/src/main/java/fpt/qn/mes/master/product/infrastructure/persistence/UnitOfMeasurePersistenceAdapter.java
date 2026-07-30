package fpt.qn.mes.master.product.infrastructure.persistence;

import static fpt.qn.mes.jooq.Tables.UNITS_OF_MEASURE;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import fpt.qn.mes.common.util.UuidV7;

import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.SortField;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;

import fpt.qn.mes.common.domainQuery.PaginationResult;
import fpt.qn.mes.common.repository.BaseRepository;
import fpt.qn.mes.common.repository.SortUtils;
import fpt.qn.mes.jooq.tables.records.UnitsOfMeasureRecord;
import fpt.qn.mes.master.product.domain.entities.UnitOfMeasure;
import fpt.qn.mes.master.product.domain.repository.UnitOfMeasureRepository;
import fpt.qn.mes.master.product.domain.repository.criteria.UnitOfMeasureSearchCriteria;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@Repository
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UnitOfMeasurePersistenceAdapter extends BaseRepository<UnitsOfMeasureRecord> implements UnitOfMeasureRepository {

    private static final Map<String, Field<?>> SORT_FIELDS = Map.of(
        "name",        UNITS_OF_MEASURE.NAME,
        "description", UNITS_OF_MEASURE.DESCRIPTION
    );
    private static final Field<?> DEFAULT_SORT_FIELD = UNITS_OF_MEASURE.NAME;

    DSLContext dslCtx;
    UnitOfMeasureRecordMapper mapper;

    public UnitOfMeasurePersistenceAdapter(DSLContext ctx, UnitOfMeasureRecordMapper mapper) {
        super(ctx, UNITS_OF_MEASURE);
        this.dslCtx = ctx;
        this.mapper = mapper;
    }

    @Override
    public Optional<UnitOfMeasure> findById(UUID id) {
        return dslCtx.selectFrom(UNITS_OF_MEASURE)
            .where(UNITS_OF_MEASURE.ID.eq(id))
            .fetchOptional(r -> mapper.toDomain(r));
    }

    @Override
    public List<UnitOfMeasure> findAll() {
        return dslCtx.selectFrom(UNITS_OF_MEASURE)
            .orderBy(UNITS_OF_MEASURE.NAME.asc())
            .fetch(r -> mapper.toDomain(r));
    }

    @Override
    public UnitOfMeasure save(UnitOfMeasure unit) {
        UnitsOfMeasureRecord r = mapper.toRecord(unit);
        if (r.getId() == null) r.setId(UuidV7.generate());
        dslCtx.insertInto(UNITS_OF_MEASURE)
            .set(r)
            .onConflict(UNITS_OF_MEASURE.ID)
            .doUpdate()
            .set(r)
            .execute();
        return mapper.toDomain(r);
    }

    @Override
    public PaginationResult<UnitOfMeasure> search(UnitOfMeasureSearchCriteria criteria) {
        Condition condition = buildCondition(criteria);
        List<SortField<?>> orderBy = SortUtils.resolveSorts(criteria.getSort(), SORT_FIELDS, DEFAULT_SORT_FIELD);
        long total = dslCtx.fetchCount(UNITS_OF_MEASURE, condition);
        List<UnitOfMeasure> items = dslCtx.selectFrom(UNITS_OF_MEASURE)
            .where(condition)
            .orderBy(orderBy)
            .limit(criteria.getSize())
            .offset(criteria.getPage() * criteria.getSize())
            .fetch(r -> mapper.toDomain(r));
        return PaginationResult.<UnitOfMeasure>builder().total(total).items(items).build();
    }

    private Condition buildCondition(UnitOfMeasureSearchCriteria criteria) {
        Condition condition = DSL.noCondition();
        if (criteria.getName() != null && !criteria.getName().isBlank()) {
            condition = condition.and(UNITS_OF_MEASURE.NAME.containsIgnoreCase(criteria.getName()));
        }
        return condition;
    }
}
