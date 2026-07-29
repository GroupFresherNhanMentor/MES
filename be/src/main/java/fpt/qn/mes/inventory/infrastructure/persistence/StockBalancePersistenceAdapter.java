package fpt.qn.mes.inventory.infrastructure.persistence;

import static fpt.qn.mes.jooq.Tables.STOCK_BALANCES;

import java.util.ArrayList;
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

import fpt.qn.mes.common.repository.BaseRepository;
import fpt.qn.mes.common.repository.SortUtils;
import fpt.qn.mes.inventory.domain.entities.StockBalance;
import fpt.qn.mes.inventory.domain.repository.StockBalanceRepository;
import fpt.qn.mes.inventory.domain.repository.criteria.StockBalanceSearchCriteria;
import fpt.qn.mes.jooq.tables.records.StockBalancesRecord;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@Repository
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class StockBalancePersistenceAdapter extends BaseRepository<StockBalancesRecord>
        implements StockBalanceRepository {

    private static final Map<String, Field<?>> SORT_FIELDS = Map.of("createdAt",
            STOCK_BALANCES.CREATED_AT, "updatedAt", STOCK_BALANCES.UPDATED_AT, "quantity",
            STOCK_BALANCES.QUANTITY, "warehouseId", STOCK_BALANCES.WAREHOUSE_ID, "locationId",
            STOCK_BALANCES.LOCATION_ID, "productId", STOCK_BALANCES.PRODUCT_ID, "lotId",
            STOCK_BALANCES.LOT_ID, "stockStatusId", STOCK_BALANCES.STOCK_STATUS_ID);

    private static final Field<?> DEFAULT_SORT_FIELD = STOCK_BALANCES.UPDATED_AT;

    InventoryRecordMapper mapper;

    public StockBalancePersistenceAdapter(DSLContext ctx, InventoryRecordMapper mapper) {
        super(ctx, STOCK_BALANCES);
        this.mapper = mapper;
    }

    @Override
    public Optional<StockBalance> findForUpdate(UUID warehouseId, UUID locationId, UUID productId,
            UUID lotId) {
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

        StockBalancesRecord record =
                ctx.selectFrom(STOCK_BALANCES).where(condition).forUpdate().fetchOne();

        return Optional.ofNullable(mapper.toDomain(record));
    }

    @Override
    public StockBalance save(StockBalance balance) {
        UUID id = balance.getId() != null ? balance.getId() : UUID.randomUUID();
        StockBalancesRecord record = mapper.toRecord(balance);
        record.setId(id);

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
                .fetch().map(mapper::toDomain);
    }

    @Override
    public List<StockBalance> search(StockBalanceSearchCriteria criteria) {
        Condition condition = buildCondition(criteria);
        int page = criteria.getPage();
        int size = criteria.getSize();

        List<SortField<?>> orderBy =
                SortUtils.resolveSorts(criteria.getSort(), SORT_FIELDS, DEFAULT_SORT_FIELD);

        return ctx.selectFrom(STOCK_BALANCES).where(condition).orderBy(orderBy).limit(size)
                .offset(page * size).fetch().map(mapper::toDomain);
    }

    @Override
    public long count(StockBalanceSearchCriteria criteria) {
        return count(buildCondition(criteria));
    }

    private Condition buildCondition(StockBalanceSearchCriteria criteria) {
        List<Condition> conditions = new ArrayList<>();
        if (criteria.getWarehouseId() != null) {
            conditions.add(STOCK_BALANCES.WAREHOUSE_ID.eq(criteria.getWarehouseId()));
        }
        if (criteria.getLocationId() != null) {
            conditions.add(STOCK_BALANCES.LOCATION_ID.eq(criteria.getLocationId()));
        }
        if (criteria.getProductId() != null) {
            conditions.add(STOCK_BALANCES.PRODUCT_ID.eq(criteria.getProductId()));
        }
        if (criteria.getLotId() != null) {
            conditions.add(STOCK_BALANCES.LOT_ID.eq(criteria.getLotId()));
        }
        if (criteria.getStockStatusId() != null) {
            conditions.add(STOCK_BALANCES.STOCK_STATUS_ID.eq(criteria.getStockStatusId()));
        }
        return conditions.isEmpty() ? DSL.noCondition() : DSL.and(conditions);
    }

}
