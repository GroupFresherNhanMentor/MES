package fpt.qn.mes.master.product.infrastructure.persistence;

import static fpt.qn.mes.jooq.Tables.PRODUCT_STATUSES;

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
import fpt.qn.mes.jooq.tables.records.ProductStatusesRecord;
import fpt.qn.mes.master.product.domain.entities.ProductStatus;
import fpt.qn.mes.master.product.domain.repository.ProductStatusRepository;
import fpt.qn.mes.master.product.domain.repository.criteria.ProductStatusSearchCriteria;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@Repository
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductStatusPersistenceAdapter extends BaseRepository<ProductStatusesRecord> implements ProductStatusRepository {

    private static final Map<String, Field<?>> SORT_FIELDS = Map.of(
        "name",        PRODUCT_STATUSES.NAME,
        "description", PRODUCT_STATUSES.DESCRIPTION
    );
    private static final Field<?> DEFAULT_SORT_FIELD = PRODUCT_STATUSES.NAME;

    DSLContext dslCtx;
    ProductStatusRecordMapper mapper;

    public ProductStatusPersistenceAdapter(DSLContext ctx, ProductStatusRecordMapper mapper) {
        super(ctx, PRODUCT_STATUSES);
        this.dslCtx = ctx;
        this.mapper = mapper;
    }

    @Override
    public Optional<ProductStatus> findById(UUID id) {
        return dslCtx.selectFrom(PRODUCT_STATUSES)
            .where(PRODUCT_STATUSES.ID.eq(id))
            .fetchOptional(r -> mapper.toDomain(r));
    }

    @Override
    public List<ProductStatus> findAll() {
        return dslCtx.selectFrom(PRODUCT_STATUSES)
            .orderBy(PRODUCT_STATUSES.NAME.asc())
            .fetch(r -> mapper.toDomain(r));
    }

    @Override
    public ProductStatus save(ProductStatus status) {
        ProductStatusesRecord r = mapper.toRecord(status);
        if (r.getId() == null) r.setId(UuidV7.generate());
        dslCtx.insertInto(PRODUCT_STATUSES)
            .set(r)
            .onConflict(PRODUCT_STATUSES.ID)
            .doUpdate()
            .set(r)
            .execute();
        return mapper.toDomain(r);
    }

    @Override
    public PaginationResult<ProductStatus> search(ProductStatusSearchCriteria criteria) {
        Condition condition = buildCondition(criteria);
        List<SortField<?>> orderBy = SortUtils.resolveSorts(criteria.getSort(), SORT_FIELDS, DEFAULT_SORT_FIELD);
        long total = dslCtx.fetchCount(PRODUCT_STATUSES, condition);
        List<ProductStatus> items = dslCtx.selectFrom(PRODUCT_STATUSES)
            .where(condition)
            .orderBy(orderBy)
            .limit(criteria.getSize())
            .offset(criteria.getPage() * criteria.getSize())
            .fetch(r -> mapper.toDomain(r));
        return PaginationResult.<ProductStatus>builder().total(total).items(items).build();
    }

    private Condition buildCondition(ProductStatusSearchCriteria criteria) {
        Condition condition = DSL.noCondition();
        if (criteria.getName() != null && !criteria.getName().isBlank()) {
            condition = condition.and(PRODUCT_STATUSES.NAME.containsIgnoreCase(criteria.getName()));
        }
        return condition;
    }
}
