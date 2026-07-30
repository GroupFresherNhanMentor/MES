package fpt.qn.mes.master.product.infrastructure.persistence;

import static fpt.qn.mes.jooq.Tables.PRODUCT_TYPES;

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
import fpt.qn.mes.jooq.tables.records.ProductTypesRecord;
import fpt.qn.mes.master.product.domain.entities.ProductType;
import fpt.qn.mes.master.product.domain.repository.ProductTypeRepository;
import fpt.qn.mes.master.product.domain.repository.criteria.ProductTypeSearchCriteria;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@Repository
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductTypePersistenceAdapter extends BaseRepository<ProductTypesRecord> implements ProductTypeRepository {

    private static final Map<String, Field<?>> SORT_FIELDS = Map.of(
        "name",        PRODUCT_TYPES.NAME,
        "description", PRODUCT_TYPES.DESCRIPTION
    );
    private static final Field<?> DEFAULT_SORT_FIELD = PRODUCT_TYPES.NAME;

    DSLContext dslCtx;
    ProductTypeRecordMapper mapper;

    public ProductTypePersistenceAdapter(DSLContext ctx, ProductTypeRecordMapper mapper) {
        super(ctx, PRODUCT_TYPES);
        this.dslCtx = ctx;
        this.mapper = mapper;
    }

    @Override
    public Optional<ProductType> findById(UUID id) {
        return dslCtx.selectFrom(PRODUCT_TYPES)
            .where(PRODUCT_TYPES.ID.eq(id))
            .fetchOptional(r -> mapper.toDomain(r));
    }

    @Override
    public List<ProductType> findAll() {
        return dslCtx.selectFrom(PRODUCT_TYPES)
            .orderBy(PRODUCT_TYPES.NAME.asc())
            .fetch(r -> mapper.toDomain(r));
    }

    @Override
    public ProductType save(ProductType type) {
        ProductTypesRecord r = mapper.toRecord(type);
        if (r.getId() == null) r.setId(UuidV7.generate());
        dslCtx.insertInto(PRODUCT_TYPES)
            .set(r)
            .onConflict(PRODUCT_TYPES.ID)
            .doUpdate()
            .set(r)
            .execute();
        return mapper.toDomain(r);
    }

    @Override
    public PaginationResult<ProductType> search(ProductTypeSearchCriteria criteria) {
        Condition condition = buildCondition(criteria);
        List<SortField<?>> orderBy = SortUtils.resolveSorts(criteria.getSort(), SORT_FIELDS, DEFAULT_SORT_FIELD);
        long total = dslCtx.fetchCount(PRODUCT_TYPES, condition);
        List<ProductType> items = dslCtx.selectFrom(PRODUCT_TYPES)
            .where(condition)
            .orderBy(orderBy)
            .limit(criteria.getSize())
            .offset(criteria.getPage() * criteria.getSize())
            .fetch(r -> mapper.toDomain(r));
        return PaginationResult.<ProductType>builder().total(total).items(items).build();
    }

    private Condition buildCondition(ProductTypeSearchCriteria criteria) {
        Condition condition = DSL.noCondition();
        if (criteria.getName() != null && !criteria.getName().isBlank()) {
            condition = condition.and(PRODUCT_TYPES.NAME.containsIgnoreCase(criteria.getName()));
        }
        return condition;
    }
}
