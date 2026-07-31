package fpt.qn.mes.master.product.infrastructure.persistence.product;

import static fpt.qn.mes.jooq.Tables.PRODUCT_STATUSES;
import static fpt.qn.mes.jooq.Tables.PRODUCT_TYPES;
import static fpt.qn.mes.jooq.Tables.PRODUCTS;
import static fpt.qn.mes.jooq.Tables.UNITS_OF_MEASURE;
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
import fpt.qn.mes.jooq.tables.records.ProductsRecord;
import fpt.qn.mes.master.product.domain.entities.Product;
import fpt.qn.mes.master.product.domain.repository.ProductRepository;
import fpt.qn.mes.master.product.domain.repository.criteria.ProductSearchCriteria;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@Repository
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductPersistenceAdapter extends BaseRepository<ProductsRecord> implements ProductRepository {

    private static final Users CREATOR = USERS.as("creator");
    private static final Users UPDATER = USERS.as("updater");

    private static final Map<String, Field<?>> SORT_FIELDS = Map.of(
        "name",       PRODUCTS.NAME,
        "created_at", PRODUCTS.CREATED_AT
    );
    private static final Field<?> DEFAULT_SORT_FIELD = PRODUCTS.CREATED_AT;

    ProductRecordMapper mapper;

    public ProductPersistenceAdapter(DSLContext ctx, ProductRecordMapper mapper) {
        super(ctx, PRODUCTS);
        this.mapper = mapper;
    }

    @Override
    public Optional<Product> findById(UUID id) {
        return ctx.select()
                .from(PRODUCTS)
                .leftJoin(PRODUCT_TYPES).on(PRODUCT_TYPES.ID.eq(PRODUCTS.PRODUCT_TYPE_ID))
                .leftJoin(UNITS_OF_MEASURE).on(UNITS_OF_MEASURE.ID.eq(PRODUCTS.UNIT_ID))
                .leftJoin(PRODUCT_STATUSES).on(PRODUCT_STATUSES.ID.eq(PRODUCTS.PRODUCT_STATUS_ID))
                .leftJoin(CREATOR).on(PRODUCTS.CREATED_BY.eq(CREATOR.ID))
                .leftJoin(UPDATER).on(PRODUCTS.UPDATED_BY.eq(UPDATER.ID))
                .where(PRODUCTS.ID.eq(id))
                .fetchOptional(r -> mapper.toDomain(
                        r.into(PRODUCTS), r.into(PRODUCT_TYPES), r.into(UNITS_OF_MEASURE),
                        r.into(PRODUCT_STATUSES), r.into(CREATOR), r.into(UPDATER)));
    }

    @Override
    public Product save(Product product) {
        create(mapper.toRecord(product));
        return product;
    }

    @Override
    public Product update(Product product) {
        update(mapper.toRecord(product));
        return product;
    }

    @Override
    public void deleteById(UUID id) {
        // soft delete only — no-op
    }

    @Override
    public PaginationResult<Product> search(ProductSearchCriteria criteria) {
        Condition condition = buildCondition(criteria);
        List<SortField<?>> orderBy = SortUtils.resolveSorts(criteria.getSort(), SORT_FIELDS, DEFAULT_SORT_FIELD);
        long total = ctx.fetchCount(PRODUCTS, condition);
        List<Product> items = ctx.select()
                .from(PRODUCTS)
                .leftJoin(PRODUCT_TYPES).on(PRODUCT_TYPES.ID.eq(PRODUCTS.PRODUCT_TYPE_ID))
                .leftJoin(UNITS_OF_MEASURE).on(UNITS_OF_MEASURE.ID.eq(PRODUCTS.UNIT_ID))
                .leftJoin(PRODUCT_STATUSES).on(PRODUCT_STATUSES.ID.eq(PRODUCTS.PRODUCT_STATUS_ID))
                .leftJoin(CREATOR).on(PRODUCTS.CREATED_BY.eq(CREATOR.ID))
                .leftJoin(UPDATER).on(PRODUCTS.UPDATED_BY.eq(UPDATER.ID))
                .where(condition)
                .orderBy(orderBy)
                .limit(criteria.getSize())
                .offset((long) criteria.getPage() * criteria.getSize())
                .fetch(r -> mapper.toDomain(
                        r.into(PRODUCTS), r.into(PRODUCT_TYPES), r.into(UNITS_OF_MEASURE),
                        r.into(PRODUCT_STATUSES), r.into(CREATOR), r.into(UPDATER)));
        return PaginationResult.<Product>builder().total(total).items(items).build();
    }

    @Override
    public boolean existsByCode(String code) {
        return ctx.fetchExists(PRODUCTS, PRODUCTS.CODE.eq(code));
    }

    @Override
    public boolean existsByVersion(String version) {
        return ctx.fetchExists(PRODUCTS, PRODUCTS.VERSION.eq(version));
    }

    private Condition buildCondition(ProductSearchCriteria criteria) {
        Condition condition = DSL.noCondition();
        if (criteria.getCode() != null && !criteria.getCode().isBlank()) {
            condition = condition.and(PRODUCTS.CODE.containsIgnoreCase(criteria.getCode()));
        }
        if (criteria.getName() != null && !criteria.getName().isBlank()) {
            condition = condition.and(PRODUCTS.NAME.containsIgnoreCase(criteria.getName()));
        }
        if (criteria.getVersion() != null && !criteria.getVersion().isBlank()) {
            condition = condition.and(PRODUCTS.VERSION.containsIgnoreCase(criteria.getVersion()));
        }
        if (criteria.getProductTypeId() != null) {
            condition = condition.and(PRODUCTS.PRODUCT_TYPE_ID.eq(criteria.getProductTypeId()));
        }
        if (criteria.getUnitId() != null) {
            condition = condition.and(PRODUCTS.UNIT_ID.eq(criteria.getUnitId()));
        }
        if (criteria.getProductStatusId() != null) {
            condition = condition.and(PRODUCTS.PRODUCT_STATUS_ID.eq(criteria.getProductStatusId()));
        }
        return condition;
    }
}
