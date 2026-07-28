package fpt.qn.mes.inventory.infrastructure.persistence;

import static fpt.qn.mes.jooq.Tables.STOCK_BALANCES;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.jooq.Condition;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import fpt.qn.mes.inventory.domain.entities.StockBalance;
import fpt.qn.mes.inventory.domain.repository.StockBalanceRepository;
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
}
