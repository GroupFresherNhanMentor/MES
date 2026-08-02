package fpt.qn.mes.master.product.infrastructure.persistence.productstatus;

import static fpt.qn.mes.jooq.Tables.PRODUCT_STATUSES;
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
import fpt.qn.mes.jooq.tables.records.ProductStatusesRecord;
import fpt.qn.mes.master.product.domain.entities.ProductStatus;
import fpt.qn.mes.master.product.domain.repository.ProductStatusRepository;
import fpt.qn.mes.master.product.domain.repository.criteria.ProductStatusSearchCriteria;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@Repository
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductStatusPersistenceAdapter extends BaseRepository<ProductStatusesRecord> implements ProductStatusRepository {

    private static final Users CREATOR = USERS.as("creator");
    private static final Users UPDATER = USERS.as("updater");

    private static final Map<String, Field<?>> SORT_FIELDS = Map.of(
        "name",       PRODUCT_STATUSES.NAME,
        "created_at", PRODUCT_STATUSES.CREATED_AT
    );
    private static final Field<?> DEFAULT_SORT_FIELD = PRODUCT_STATUSES.CREATED_AT;

    ProductStatusRecordMapper mapper;

    public ProductStatusPersistenceAdapter(DSLContext ctx, ProductStatusRecordMapper mapper) {
        super(ctx, PRODUCT_STATUSES);
        this.mapper = mapper;
    }

    @Override
    public Optional<ProductStatus> findById(UUID id) {
        return ctx.select()
            .from(PRODUCT_STATUSES)
            .leftJoin(CREATOR).on(PRODUCT_STATUSES.CREATED_BY.eq(CREATOR.ID))
            .leftJoin(UPDATER).on(PRODUCT_STATUSES.UPDATED_BY.eq(UPDATER.ID))
            .where(PRODUCT_STATUSES.ID.eq(id))
            .fetchOptional(r -> mapper.toDomain(r.into(PRODUCT_STATUSES), r.into(CREATOR), r.into(UPDATER)));
    }

    @Override
    public Optional<ProductStatus> findByName(String name) {
        return ctx.select()
            .from(PRODUCT_STATUSES)
            .leftJoin(CREATOR).on(PRODUCT_STATUSES.CREATED_BY.eq(CREATOR.ID))
            .leftJoin(UPDATER).on(PRODUCT_STATUSES.UPDATED_BY.eq(UPDATER.ID))
            .where(PRODUCT_STATUSES.NAME.eq(name))
            .fetchOptional(r -> mapper.toDomain(r.into(PRODUCT_STATUSES), r.into(CREATOR), r.into(UPDATER)));
    }

    @Override
    public boolean existsById(UUID id) {
        return ctx.fetchExists(PRODUCT_STATUSES, PRODUCT_STATUSES.ID.eq(id));
    }

    @Override
    public boolean existsByName(String name) {
        return ctx.fetchExists(PRODUCT_STATUSES, PRODUCT_STATUSES.NAME.eq(name));
    }

    @Override
    public List<ProductStatus> findAll() {
        return ctx.select()
            .from(PRODUCT_STATUSES)
            .leftJoin(CREATOR).on(PRODUCT_STATUSES.CREATED_BY.eq(CREATOR.ID))
            .leftJoin(UPDATER).on(PRODUCT_STATUSES.UPDATED_BY.eq(UPDATER.ID))
            .orderBy(PRODUCT_STATUSES.NAME.asc())
            .fetch(r -> mapper.toDomain(r.into(PRODUCT_STATUSES), r.into(CREATOR), r.into(UPDATER)));
    }

    @Override
    public ProductStatus save(ProductStatus status) {
        ProductStatusesRecord r = mapper.toRecord(status);
        if (r.getId() == null) r.setId(UuidV7.generate());
        ctx.insertInto(PRODUCT_STATUSES)
            .set(r)
            .onConflict(PRODUCT_STATUSES.ID)
            .doUpdate()
            .set(r)
            .execute();
        return status;
    }

    @Override
    public PaginationResult<ProductStatus> search(ProductStatusSearchCriteria criteria) {
        Condition condition = buildCondition(criteria);
        List<SortField<?>> orderBy = SortUtils.resolveSorts(criteria.getSort(), SORT_FIELDS, DEFAULT_SORT_FIELD);
        long total = ctx.fetchCount(PRODUCT_STATUSES, condition);
        List<ProductStatus> items = ctx.select()
            .from(PRODUCT_STATUSES)
            .leftJoin(CREATOR).on(PRODUCT_STATUSES.CREATED_BY.eq(CREATOR.ID))
            .leftJoin(UPDATER).on(PRODUCT_STATUSES.UPDATED_BY.eq(UPDATER.ID))
            .where(condition)
            .orderBy(orderBy)
            .limit(criteria.getSize())
            .offset(criteria.getPage() * criteria.getSize())
            .fetch(r -> mapper.toDomain(r.into(PRODUCT_STATUSES), r.into(CREATOR), r.into(UPDATER)));
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
