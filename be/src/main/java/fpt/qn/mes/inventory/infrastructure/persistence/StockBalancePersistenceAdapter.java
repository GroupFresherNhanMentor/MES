package fpt.qn.mes.inventory.infrastructure.persistence;

import static fpt.qn.mes.jooq.Tables.PRODUCTS;
import static fpt.qn.mes.jooq.Tables.STOCK_BALANCES;
import static fpt.qn.mes.jooq.Tables.STOCK_LOTS;
import static fpt.qn.mes.jooq.Tables.STOCK_STATUSES;
import static fpt.qn.mes.jooq.Tables.WAREHOUSE_LOCATIONS;
import static fpt.qn.mes.jooq.Tables.WAREHOUSES;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import fpt.qn.mes.common.domainQuery.PaginationResult;
import fpt.qn.mes.common.repository.BaseRepository;
import fpt.qn.mes.common.repository.SortUtils;
import fpt.qn.mes.common.util.UuidV7;
import fpt.qn.mes.inventory.domain.entities.StockBalance;
import fpt.qn.mes.inventory.domain.repository.StockBalanceRepository;
import fpt.qn.mes.inventory.domain.repository.criteria.StockBalanceSearchCriteria;
import fpt.qn.mes.jooq.tables.records.StockBalancesRecord;

import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.SortField;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@Repository
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class StockBalancePersistenceAdapter extends BaseRepository<StockBalancesRecord>
        implements StockBalanceRepository {

    private static final Map<String, Field<?>> SORT_FIELDS = Map.of(
            "quantity",    STOCK_BALANCES.QUANTITY,
            "updated_at",  STOCK_BALANCES.UPDATED_AT,
            "created_at",  STOCK_BALANCES.CREATED_AT);
    private static final Field<?> DEFAULT_SORT_FIELD = STOCK_BALANCES.UPDATED_AT;

    InventoryRecordMapper mapper;

    public StockBalancePersistenceAdapter(DSLContext ctx, InventoryRecordMapper mapper) {
        super(ctx, STOCK_BALANCES);
        this.mapper = mapper;
    }

    @Override
    public Optional<StockBalance> findById(UUID id) {
        return ctx.select()
                .from(STOCK_BALANCES)
                .leftJoin(WAREHOUSES).on(WAREHOUSES.ID.eq(STOCK_BALANCES.WAREHOUSE_ID))
                .leftJoin(WAREHOUSE_LOCATIONS).on(WAREHOUSE_LOCATIONS.ID.eq(STOCK_BALANCES.LOCATION_ID))
                .leftJoin(PRODUCTS).on(PRODUCTS.ID.eq(STOCK_BALANCES.PRODUCT_ID))
                .leftJoin(STOCK_LOTS).on(STOCK_LOTS.ID.eq(STOCK_BALANCES.LOT_ID))
                .leftJoin(STOCK_STATUSES).on(STOCK_STATUSES.ID.eq(STOCK_BALANCES.STOCK_STATUS_ID))
                .where(STOCK_BALANCES.ID.eq(id))
                .fetchOptional(r -> mapper.toDomain(
                        r.into(STOCK_BALANCES), r.into(WAREHOUSES), r.into(WAREHOUSE_LOCATIONS),
                        r.into(PRODUCTS), r.into(STOCK_LOTS), r.into(STOCK_STATUSES)));
    }

    @Override
    public Optional<StockBalance> findForUpdate(UUID warehouseId, UUID locationId, UUID productId,
            UUID lotId, UUID stockStatusId) {
        Condition condition = STOCK_BALANCES.WAREHOUSE_ID.eq(warehouseId)
                .and(STOCK_BALANCES.PRODUCT_ID.eq(productId));
        if (locationId != null) {
            condition = condition.and(STOCK_BALANCES.LOCATION_ID.eq(locationId));
        } else {
            condition = condition.and(STOCK_BALANCES.LOCATION_ID.isNull());
        }
        if (lotId != null) {
            condition = condition.and(STOCK_BALANCES.LOT_ID.eq(lotId));
        } else {
            condition = condition.and(STOCK_BALANCES.LOT_ID.isNull());
        }
        if (stockStatusId != null) {
            condition = condition.and(STOCK_BALANCES.STOCK_STATUS_ID.eq(stockStatusId));
        }
        StockBalancesRecord record = ctx.selectFrom(STOCK_BALANCES)
                .where(condition).forUpdate().fetchOne();
        return Optional.ofNullable(mapper.toDomain(record));
    }

    @Override
    public StockBalance save(StockBalance balance) {
        UUID id = balance.getId() != null ? balance.getId() : UuidV7.generate();
        Long version = balance.getVersion() != null ? balance.getVersion() : 1L;
        StockBalancesRecord record = mapper.toRecord(balance);
        record.setId(id);
        record.setVersion(version);
        if (record.getCreatedAt() == null) {
            record.setCreatedAt(java.time.OffsetDateTime.now());
        }
        record.setUpdatedAt(java.time.OffsetDateTime.now());
        ctx.insertInto(STOCK_BALANCES)
                .set(record)
                .onDuplicateKeyUpdate()
                .set(record)
                .execute();
        return mapper.toDomain(record);
    }

    @Override
    public List<StockBalance> findByWarehouseAndProduct(UUID warehouseId, UUID productId) {
        return ctx.selectFrom(STOCK_BALANCES)
                .where(STOCK_BALANCES.WAREHOUSE_ID.eq(warehouseId)
                        .and(STOCK_BALANCES.PRODUCT_ID.eq(productId)))
                .fetch(r -> mapper.toDomain(r));
    }

    @Override
    public PaginationResult<StockBalance> search(StockBalanceSearchCriteria criteria) {
        Condition condition = buildCondition(criteria);
        List<SortField<?>> orderBy = SortUtils.resolveSorts(criteria.getSort(), SORT_FIELDS, DEFAULT_SORT_FIELD);
        long total = ctx.fetchCount(STOCK_BALANCES, condition);
        List<StockBalance> items = ctx.select()
                .from(STOCK_BALANCES)
                .leftJoin(WAREHOUSES).on(WAREHOUSES.ID.eq(STOCK_BALANCES.WAREHOUSE_ID))
                .leftJoin(WAREHOUSE_LOCATIONS).on(WAREHOUSE_LOCATIONS.ID.eq(STOCK_BALANCES.LOCATION_ID))
                .leftJoin(PRODUCTS).on(PRODUCTS.ID.eq(STOCK_BALANCES.PRODUCT_ID))
                .leftJoin(STOCK_LOTS).on(STOCK_LOTS.ID.eq(STOCK_BALANCES.LOT_ID))
                .leftJoin(STOCK_STATUSES).on(STOCK_STATUSES.ID.eq(STOCK_BALANCES.STOCK_STATUS_ID))
                .where(condition)
                .orderBy(orderBy)
                .limit(criteria.getSize())
                .offset((long) criteria.getPage() * criteria.getSize())
                .fetch(r -> mapper.toDomain(
                        r.into(STOCK_BALANCES), r.into(WAREHOUSES), r.into(WAREHOUSE_LOCATIONS),
                        r.into(PRODUCTS), r.into(STOCK_LOTS), r.into(STOCK_STATUSES)));
        return PaginationResult.<StockBalance>builder().total(total).items(items).build();
    }

    private Condition buildCondition(StockBalanceSearchCriteria criteria) {
        Condition condition = DSL.noCondition();
        if (criteria.getWarehouseId() != null) {
            condition = condition.and(STOCK_BALANCES.WAREHOUSE_ID.eq(criteria.getWarehouseId()));
        }
        if (criteria.getLocationId() != null) {
            condition = condition.and(STOCK_BALANCES.LOCATION_ID.eq(criteria.getLocationId()));
        }
        if (criteria.getProductId() != null) {
            condition = condition.and(STOCK_BALANCES.PRODUCT_ID.eq(criteria.getProductId()));
        }
        if (criteria.getLotId() != null) {
            condition = condition.and(STOCK_BALANCES.LOT_ID.eq(criteria.getLotId()));
        }
        if (criteria.getStockStatusId() != null) {
            condition = condition.and(STOCK_BALANCES.STOCK_STATUS_ID.eq(criteria.getStockStatusId()));
        }
        return condition;
    }
}
