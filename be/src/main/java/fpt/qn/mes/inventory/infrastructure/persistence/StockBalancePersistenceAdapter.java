package fpt.qn.mes.inventory.infrastructure.persistence;

import static fpt.qn.mes.jooq.Tables.STOCK_BALANCES;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.jooq.Condition;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import fpt.qn.mes.common.dto.response.PageResponse;
import fpt.qn.mes.inventory.domain.entities.StockBalance;
import fpt.qn.mes.inventory.domain.repository.StockBalanceRepository;
import fpt.qn.mes.inventory.domain.repository.StockBalanceSearchCriteria;
import fpt.qn.mes.jooq.tables.records.StockBalancesRecord;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Repository
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class StockBalancePersistenceAdapter implements StockBalanceRepository {

    DSLContext ctx;
    InventoryRecordMapper mapper;

    @Override
    public Optional<StockBalance> findForUpdate(UUID warehouseId, UUID locationId, UUID productId, UUID lotId) {
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

        StockBalancesRecord record = ctx.selectFrom(STOCK_BALANCES)
                .where(condition)
                .forUpdate()
                .fetchOne();

        return Optional.ofNullable(mapper.toDomain(record));
    }

    @Override
    public StockBalance save(StockBalance balance) {
        StockBalancesRecord record = mapper.toRecord(balance);
        if (record.getId() == null) {
            record.setId(UUID.randomUUID());
        }
        ctx.attach(record);
        record.store();
        return mapper.toDomain(record);
    }

    @Override
    public List<StockBalance> findByWarehouseAndProduct(UUID warehouseId, UUID productId) {
        return ctx.selectFrom(STOCK_BALANCES)
                .where(STOCK_BALANCES.WAREHOUSE_ID.eq(warehouseId)
                        .and(STOCK_BALANCES.PRODUCT_ID.eq(productId)))
                .fetch()
                .map(mapper::toDomain);
    }

    @Override
    public PageResponse<StockBalance> search(StockBalanceSearchCriteria criteria) {
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

        long totalElements = ctx.fetchCount(STOCK_BALANCES, conditions);

        int page = criteria.getPage();
        int size = criteria.getSize();
        int totalPages = size > 0 ? (int) Math.ceil((double) totalElements / size) : 0;

        List<StockBalance> items = ctx.selectFrom(STOCK_BALANCES)
                .where(conditions)
                .orderBy(STOCK_BALANCES.UPDATED_AT.desc())
                .limit(size)
                .offset(page * size)
                .fetch()
                .map(mapper::toDomain);

        return PageResponse.<StockBalance>builder()
                .items(items)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .pageNumber(page)
                .pageSize(size)
                .build();
    }
}
