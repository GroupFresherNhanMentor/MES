package fpt.qn.mes.quality.infrastructure.persistence;

import static fpt.qn.mes.jooq.Tables.DEFECT_TYPES;

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
import fpt.qn.mes.common.util.UuidV7;
import fpt.qn.mes.common.repository.SortUtils;
import fpt.qn.mes.jooq.tables.records.DefectTypesRecord;
import fpt.qn.mes.quality.domain.entities.DefectType;
import fpt.qn.mes.quality.domain.repository.DefectTypeRepository;
import fpt.qn.mes.quality.domain.repository.criteria.DefectTypeSearchCriteria;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@Repository
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DefectTypePersistenceAdapter extends BaseRepository<DefectTypesRecord> implements DefectTypeRepository {

    private static final Map<String, Field<?>> SORT_FIELDS = Map.of(
        "name",        DEFECT_TYPES.NAME,
        "description", DEFECT_TYPES.DESCRIPTION
    );
    private static final Field<?> DEFAULT_SORT_FIELD = DEFECT_TYPES.NAME;

    DSLContext dslCtx;
    DefectTypeRecordMapper mapper;

    public DefectTypePersistenceAdapter(DSLContext ctx, DefectTypeRecordMapper mapper) {
        super(ctx, DEFECT_TYPES);
        this.dslCtx = ctx;
        this.mapper = mapper;
    }

    @Override
    public Optional<DefectType> findById(UUID id) {
        return dslCtx.selectFrom(DEFECT_TYPES)
            .where(DEFECT_TYPES.ID.eq(id))
            .fetchOptional(r -> mapper.toDomain(r));
    }

    @Override
    public DefectType save(DefectType defectType) {
        DefectTypesRecord r = mapper.toRecord(defectType);
        if (r.getId() == null) r.setId(UuidV7.generate());
        dslCtx.insertInto(DEFECT_TYPES)
            .set(r)
            .onConflict(DEFECT_TYPES.ID)
            .doUpdate()
            .set(r)
            .execute();
        return mapper.toDomain(r);
    }

    @Override
    public PaginationResult<DefectType> search(DefectTypeSearchCriteria criteria) {
        Condition condition = buildCondition(criteria);
        List<SortField<?>> orderBy = SortUtils.resolveSorts(criteria.getSort(), SORT_FIELDS, DEFAULT_SORT_FIELD);
        long total = dslCtx.fetchCount(DEFECT_TYPES, condition);
        List<DefectType> items = dslCtx.selectFrom(DEFECT_TYPES)
            .where(condition)
            .orderBy(orderBy)
            .limit(criteria.getSize())
            .offset(criteria.getPage() * criteria.getSize())
            .fetch(r -> mapper.toDomain(r));
        return PaginationResult.<DefectType>builder().total(total).items(items).build();
    }

    private Condition buildCondition(DefectTypeSearchCriteria criteria) {
        Condition condition = DSL.noCondition();
        if (criteria.getName() != null && !criteria.getName().isBlank()) {
            condition = condition.and(DEFECT_TYPES.NAME.containsIgnoreCase(criteria.getName()));
        }
        return condition;
    }
}
