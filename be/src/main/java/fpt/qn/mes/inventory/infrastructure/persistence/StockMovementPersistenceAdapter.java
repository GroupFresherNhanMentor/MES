package fpt.qn.mes.inventory.infrastructure.persistence;

import static fpt.qn.mes.jooq.Tables.STOCK_MOVEMENTS;

import java.util.List;
import java.util.Map;
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
import fpt.qn.mes.inventory.domain.entities.StockMovement;
import fpt.qn.mes.inventory.domain.repository.StockMovementRepository;
import fpt.qn.mes.inventory.domain.repository.criteria.StockMovementSearchCriteria;
import fpt.qn.mes.jooq.tables.records.StockMovementsRecord;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@Repository
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class StockMovementPersistenceAdapter extends BaseRepository<StockMovementsRecord> implements StockMovementRepository {

    private static final Map<String, Field<?>> SORT_FIELDS = Map.of(
            "createdAt",   STOCK_MOVEMENTS.CREATED_AT,
            "quantity",    STOCK_MOVEMENTS.QUANTITY,
            "referenceNo", STOCK_MOVEMENTS.REFERENCE_NO
    );

    private static final Field<?> DEFAULT_SORT_FIELD = STOCK_MOVEMENTS.CREATED_AT;

    InventoryRecordMapper mapper;

    public StockMovementPersistenceAdapter(DSLContext ctx, InventoryRecordMapper mapper) {
        super(ctx, STOCK_MOVEMENTS);
        this.mapper = mapper;
    }

    @Override
    public StockMovement save(StockMovement movement) {
        StockMovementsRecord record = mapper.toRecord(movement);
        if (record.getId() == null) {
            record.setId(UuidV7.generate());
        }
        ctx.attach(record);
        record.store();
        return mapper.toDomain(record);
    }

    @Override
    public List<StockMovement> search(StockMovementSearchCriteria criteria) {
        Condition condition = buildCondition(criteria);
        int page = criteria.getPage();
        int size = criteria.getSize();

        List<SortField<?>> orderBy = SortUtils.resolveSorts(criteria.getSort(), SORT_FIELDS, DEFAULT_SORT_FIELD);

        return ctx.selectFrom(STOCK_MOVEMENTS)
                .where(condition)
                .orderBy(orderBy)
                .limit(size)
                .offset(page * size)
                .fetch()
                .map(r -> mapper.toDomain(r));
    }

    @Override
    public long count(StockMovementSearchCriteria criteria) {
        return count(buildCondition(criteria));
    }

    private Condition buildCondition(StockMovementSearchCriteria criteria) {
        Condition condition = DSL.noCondition();
        if (criteria.getMovementTypeId() != null) {
            condition = condition.and(STOCK_MOVEMENTS.MOVEMENT_TYPE_ID.eq(criteria.getMovementTypeId()));
        }
        if (criteria.getProductId() != null) {
            condition = condition.and(STOCK_MOVEMENTS.PRODUCT_ID.eq(criteria.getProductId()));
        }
        if (criteria.getLotId() != null) {
            condition = condition.and(STOCK_MOVEMENTS.LOT_ID.eq(criteria.getLotId()));
        }
        if (criteria.getWarehouseId() != null) {
            condition = condition.and(STOCK_MOVEMENTS.FROM_WAREHOUSE_ID.eq(criteria.getWarehouseId())
                    .or(STOCK_MOVEMENTS.TO_WAREHOUSE_ID.eq(criteria.getWarehouseId())));
        }
        if (criteria.getLocationId() != null) {
            condition = condition.and(STOCK_MOVEMENTS.FROM_LOCATION_ID.eq(criteria.getLocationId())
                    .or(STOCK_MOVEMENTS.TO_LOCATION_ID.eq(criteria.getLocationId())));
        }
        if (criteria.getReferenceNo() != null && !criteria.getReferenceNo().isBlank()) {
            condition = condition.and(STOCK_MOVEMENTS.REFERENCE_NO.containsIgnoreCase(criteria.getReferenceNo()));
        }
        return condition;
    }
}
