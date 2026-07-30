package fpt.qn.mes.inventory.infrastructure.persistence;

import static fpt.qn.mes.jooq.Tables.MOVEMENT_TYPES;
import static fpt.qn.mes.jooq.Tables.STOCK_LOTS;
import static fpt.qn.mes.jooq.Tables.STOCK_MOVEMENTS;
import static fpt.qn.mes.jooq.Tables.STOCK_STATUSES;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.SortField;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;

import fpt.qn.mes.common.repository.BaseRepository;
import fpt.qn.mes.common.repository.SortUtils;
import fpt.qn.mes.common.util.UuidV7;
import fpt.qn.mes.inventory.domain.entities.MovementType;
import fpt.qn.mes.inventory.domain.entities.StockLot;
import fpt.qn.mes.inventory.domain.entities.StockMovement;
import fpt.qn.mes.inventory.domain.entities.StockStatus;
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

        var sm = STOCK_MOVEMENTS;
        var mt = MOVEMENT_TYPES.as("mt");
        var sl = STOCK_LOTS.as("sl");
        var fs = STOCK_STATUSES.as("fs");
        var ts = STOCK_STATUSES.as("ts");

        return ctx.select(
                    sm.ID,
                    sm.MOVEMENT_TYPE_ID,
                    mt.NAME.as("movementTypeName"),
                     sm.PRODUCT_ID,
                     sm.LOT_ID,
                     sm.WORK_ORDER_ID,
                    sl.LOT_NUMBER.as("lotNumber"),
                    sm.FROM_WAREHOUSE_ID,
                    sm.FROM_LOCATION_ID,
                    sm.TO_WAREHOUSE_ID,
                    sm.TO_LOCATION_ID,
                    sm.QUANTITY,
                    sm.FROM_STATUS_ID,
                    fs.NAME.as("fromStatusName"),
                    sm.TO_STATUS_ID,
                    ts.NAME.as("toStatusName"),
                    sm.REFERENCE_NO,
                    sm.REASON,
                    sm.CREATED_BY,
                    sm.CREATED_AT
                )
                .from(sm)
                .leftJoin(mt).on(sm.MOVEMENT_TYPE_ID.eq(mt.ID))
                .leftJoin(sl).on(sm.LOT_ID.eq(sl.ID))
                .leftJoin(fs).on(sm.FROM_STATUS_ID.eq(fs.ID))
                .leftJoin(ts).on(sm.TO_STATUS_ID.eq(ts.ID))
                .where(condition)
                .orderBy(orderBy)
                .limit(size)
                .offset(page * size)
                .fetch(this::mapRecordToStockMovement);
    }

    @Override
    public long count(StockMovementSearchCriteria criteria) {
        return count(buildCondition(criteria));
    }

    private StockMovement mapRecordToStockMovement(Record r) {
        if (r == null) return null;

        UUID movementTypeId = r.get(STOCK_MOVEMENTS.MOVEMENT_TYPE_ID);
        String movementTypeName = r.get("movementTypeName", String.class);
        MovementType movementType = movementTypeId != null ? MovementType.builder()
                .id(movementTypeId)
                .name(movementTypeName)
                .build() : null;

        UUID lotId = r.get(STOCK_MOVEMENTS.LOT_ID);
        String lotNumber = r.get("lotNumber", String.class);
        StockLot stockLot = lotId != null ? StockLot.builder()
                .id(lotId)
                .lotNumber(lotNumber)
                .build() : null;

        UUID fromStatusId = r.get(STOCK_MOVEMENTS.FROM_STATUS_ID);
        String fromStatusName = r.get("fromStatusName", String.class);
        StockStatus fromStatus = fromStatusId != null ? StockStatus.builder()
                .id(fromStatusId)
                .name(fromStatusName)
                .build() : null;

        UUID toStatusId = r.get(STOCK_MOVEMENTS.TO_STATUS_ID);
        String toStatusName = r.get("toStatusName", String.class);
        StockStatus toStatus = toStatusId != null ? StockStatus.builder()
                .id(toStatusId)
                .name(toStatusName)
                .build() : null;

        var createdAtOffset = r.get(STOCK_MOVEMENTS.CREATED_AT);

        return StockMovement.builder()
                .id(r.get(STOCK_MOVEMENTS.ID))
                .movementType(movementType)
                .productId(r.get(STOCK_MOVEMENTS.PRODUCT_ID))
                .workOrderId(r.get(STOCK_MOVEMENTS.WORK_ORDER_ID))
                .stockLot(stockLot)
                .fromWarehouseId(r.get(STOCK_MOVEMENTS.FROM_WAREHOUSE_ID))
                .fromLocationId(r.get(STOCK_MOVEMENTS.FROM_LOCATION_ID))
                .toWarehouseId(r.get(STOCK_MOVEMENTS.TO_WAREHOUSE_ID))
                .toLocationId(r.get(STOCK_MOVEMENTS.TO_LOCATION_ID))
                .quantity(r.get(STOCK_MOVEMENTS.QUANTITY))
                .fromStatus(fromStatus)
                .toStatus(toStatus)
                .referenceNo(r.get(STOCK_MOVEMENTS.REFERENCE_NO))
                .reason(r.get(STOCK_MOVEMENTS.REASON))
                .createdBy(r.get(STOCK_MOVEMENTS.CREATED_BY))
                .createdAt(createdAtOffset != null ? createdAtOffset.toInstant() : null)
                .build();
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
