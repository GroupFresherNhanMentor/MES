package fpt.qn.mes.master.product.infrastructure.persistence.producttype;

import static fpt.qn.mes.jooq.Tables.PRODUCT_TYPES;
import static fpt.qn.mes.jooq.Tables.USERS;

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
import fpt.qn.mes.jooq.tables.Users;
import fpt.qn.mes.jooq.tables.records.ProductTypesRecord;
import fpt.qn.mes.master.product.domain.entities.ProductType;
import fpt.qn.mes.master.product.domain.repository.ProductTypeRepository;
import fpt.qn.mes.master.product.domain.repository.criteria.ProductTypeSearchCriteria;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@Repository
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductTypePersistenceAdapter extends BaseRepository<ProductTypesRecord> implements ProductTypeRepository {

    private static final Users CREATOR = USERS.as("creator");
    private static final Users UPDATER = USERS.as("updater");

    private static final Map<String, Field<?>> SORT_FIELDS = Map.of(
        "name",       PRODUCT_TYPES.NAME,
        "created_at", PRODUCT_TYPES.CREATED_AT
    );
    private static final Field<?> DEFAULT_SORT_FIELD = PRODUCT_TYPES.CREATED_AT;

    ProductTypeRecordMapper mapper;

    public ProductTypePersistenceAdapter(DSLContext ctx, ProductTypeRecordMapper mapper) {
        super(ctx, PRODUCT_TYPES);
        this.mapper = mapper;
    }

    @Override
    public Optional<ProductType> findById(UUID id) {
        return ctx.select()
            .from(PRODUCT_TYPES)
            .leftJoin(CREATOR).on(PRODUCT_TYPES.CREATED_BY.eq(CREATOR.ID))
            .leftJoin(UPDATER).on(PRODUCT_TYPES.UPDATED_BY.eq(UPDATER.ID))
            .where(PRODUCT_TYPES.ID.eq(id))
            .fetchOptional(r -> mapper.toDomain(r.into(PRODUCT_TYPES), r.into(CREATOR), r.into(UPDATER)));
    }

    @Override
    public boolean existsById(UUID id) {
        return ctx.fetchExists(PRODUCT_TYPES, PRODUCT_TYPES.ID.eq(id));
    }

    @Override
    public boolean existsByName(String name) {
        return ctx.fetchExists(PRODUCT_TYPES, PRODUCT_TYPES.NAME.eq(name));
    }

    @Override
    public List<ProductType> findAll() {
        return ctx.select()
            .from(PRODUCT_TYPES)
            .leftJoin(CREATOR).on(PRODUCT_TYPES.CREATED_BY.eq(CREATOR.ID))
            .leftJoin(UPDATER).on(PRODUCT_TYPES.UPDATED_BY.eq(UPDATER.ID))
            .orderBy(PRODUCT_TYPES.NAME.asc())
            .fetch(r -> mapper.toDomain(r.into(PRODUCT_TYPES), r.into(CREATOR), r.into(UPDATER)));
    }

    @Override
    public ProductType save(ProductType type) {
        ProductTypesRecord r = mapper.toRecord(type);
        if (r.getId() == null) r.setId(UuidV7.generate());
        ctx.insertInto(PRODUCT_TYPES)
            .set(r)
            .onConflict(PRODUCT_TYPES.ID)
            .doUpdate()
            .set(r)
            .execute();
        return type;
    }

    @Override
    public PaginationResult<ProductType> search(ProductTypeSearchCriteria criteria) {
        Condition condition = buildCondition(criteria);
        List<SortField<?>> orderBy = SortUtils.resolveSorts(criteria.getSort(), SORT_FIELDS, DEFAULT_SORT_FIELD);
        long total = ctx.fetchCount(PRODUCT_TYPES, condition);
        List<ProductType> items = ctx.select()
            .from(PRODUCT_TYPES)
            .leftJoin(CREATOR).on(PRODUCT_TYPES.CREATED_BY.eq(CREATOR.ID))
            .leftJoin(UPDATER).on(PRODUCT_TYPES.UPDATED_BY.eq(UPDATER.ID))
            .where(condition)
            .orderBy(orderBy)
            .limit(criteria.getSize())
            .offset(criteria.getPage() * criteria.getSize())
            .fetch(r -> mapper.toDomain(r.into(PRODUCT_TYPES), r.into(CREATOR), r.into(UPDATER)));
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
