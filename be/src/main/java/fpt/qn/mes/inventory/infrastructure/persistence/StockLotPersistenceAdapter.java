package fpt.qn.mes.inventory.infrastructure.persistence;

import static fpt.qn.mes.jooq.Tables.STOCK_LOTS;

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

import fpt.qn.mes.common.repository.BaseRepository;
import fpt.qn.mes.common.repository.SortUtils;
import fpt.qn.mes.inventory.domain.entities.StockLot;
import fpt.qn.mes.inventory.domain.repository.StockLotRepository;
import fpt.qn.mes.inventory.domain.repository.criteria.StockLotSearchCriteria;
import fpt.qn.mes.jooq.tables.records.StockLotsRecord;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@Repository
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class StockLotPersistenceAdapter extends BaseRepository<StockLotsRecord> implements StockLotRepository {

    private static final Map<String, Field<?>> SORT_FIELDS = Map.of(
            "createdAt",  STOCK_LOTS.CREATED_AT,
            "lotNumber",  STOCK_LOTS.LOT_NUMBER,
            "expiryDate", STOCK_LOTS.EXPIRY_DATE
    );

    private static final Field<?> DEFAULT_SORT_FIELD = STOCK_LOTS.CREATED_AT;

    InventoryRecordMapper mapper;

    public StockLotPersistenceAdapter(DSLContext ctx, InventoryRecordMapper mapper) {
        super(ctx, STOCK_LOTS);
        this.mapper = mapper;
    }

    @Override
    public Optional<StockLot> findById(UUID id) {
        StockLotsRecord record = ctx.selectFrom(STOCK_LOTS)
                .where(STOCK_LOTS.ID.eq(id))
                .fetchOne();
        return Optional.ofNullable(mapper.toDomain(record));
    }

    @Override
    public Optional<StockLot> findByLotNumber(String lotNumber) {
        if (lotNumber == null || lotNumber.isBlank()) {
            return Optional.empty();
        }
        StockLotsRecord record = ctx.selectFrom(STOCK_LOTS)
                .where(STOCK_LOTS.LOT_NUMBER.eq(lotNumber))
                .fetchOne();
        return Optional.ofNullable(mapper.toDomain(record));
    }

    @Override
    public StockLot save(StockLot lot) {
        StockLotsRecord record = mapper.toRecord(lot);
        if (record.getId() == null) {
            record.setId(UUID.randomUUID());
        }
        ctx.attach(record);
        record.store();
        return mapper.toDomain(record);
    }

    @Override
    public List<StockLot> search(StockLotSearchCriteria criteria) {
        Condition condition = buildCondition(criteria);
        int page = criteria.getPage();
        int size = criteria.getSize();

        List<SortField<?>> orderBy = SortUtils.resolveSorts(criteria.getSort(), SORT_FIELDS, DEFAULT_SORT_FIELD);

        return ctx.selectFrom(STOCK_LOTS)
                .where(condition)
                .orderBy(orderBy)
                .limit(size)
                .offset(page * size)
                .fetch()
                .map(r -> mapper.toDomain(r));
    }

    @Override
    public long count(StockLotSearchCriteria criteria) {
        return count(buildCondition(criteria));
    }

    private Condition buildCondition(StockLotSearchCriteria criteria) {
        Condition condition = DSL.noCondition();
        if (criteria.getProductId() != null) {
            condition = condition.and(STOCK_LOTS.PRODUCT_ID.eq(criteria.getProductId()));
        }
        if (criteria.getLotTypeId() != null) {
            condition = condition.and(STOCK_LOTS.LOT_TYPE_ID.eq(criteria.getLotTypeId()));
        }
        if (criteria.getLotNumber() != null && !criteria.getLotNumber().isBlank()) {
            condition = condition.and(STOCK_LOTS.LOT_NUMBER.containsIgnoreCase(criteria.getLotNumber()));
        }
        if (criteria.getExpiryBefore() != null) {
            condition = condition.and(STOCK_LOTS.EXPIRY_DATE.lessOrEqual(criteria.getExpiryBefore()));
        }
        return condition;
    }
}
