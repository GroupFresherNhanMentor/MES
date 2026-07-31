package fpt.qn.mes.inventory.infrastructure.persistence;

import static fpt.qn.mes.jooq.Tables.MOVEMENT_TYPES;
import static fpt.qn.mes.jooq.Tables.PRODUCTS;
import static fpt.qn.mes.jooq.Tables.STOCK_LOTS;
import static fpt.qn.mes.jooq.Tables.STOCK_MOVEMENTS;
import static fpt.qn.mes.jooq.Tables.STOCK_STATUSES;
import static fpt.qn.mes.jooq.Tables.USERS;
import static fpt.qn.mes.jooq.Tables.WAREHOUSE_LOCATIONS;
import static fpt.qn.mes.jooq.Tables.WAREHOUSES;

import java.util.List;
import java.util.Map;
import java.util.Optional;
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
import fpt.qn.mes.inventory.domain.entities.MovementType;
import fpt.qn.mes.inventory.domain.entities.StockLot;
import fpt.qn.mes.inventory.domain.entities.StockMovement;
import fpt.qn.mes.inventory.domain.entities.StockStatus;
import fpt.qn.mes.inventory.domain.repository.StockMovementRepository;
import fpt.qn.mes.inventory.domain.repository.criteria.StockMovementSearchCriteria;
import fpt.qn.mes.jooq.tables.MovementTypes;
import fpt.qn.mes.jooq.tables.StockLots;
import fpt.qn.mes.jooq.tables.StockStatuses;
import fpt.qn.mes.jooq.tables.Users;
import fpt.qn.mes.jooq.tables.WarehouseLocations;
import fpt.qn.mes.jooq.tables.Warehouses;
import fpt.qn.mes.jooq.tables.records.MovementTypesRecord;
import fpt.qn.mes.jooq.tables.records.ProductsRecord;
import fpt.qn.mes.jooq.tables.records.StockLotsRecord;
import fpt.qn.mes.jooq.tables.records.StockMovementsRecord;
import fpt.qn.mes.jooq.tables.records.StockStatusesRecord;
import fpt.qn.mes.jooq.tables.records.UsersRecord;
import fpt.qn.mes.jooq.tables.records.WarehouseLocationsRecord;
import fpt.qn.mes.jooq.tables.records.WarehousesRecord;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@Repository
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class StockMovementPersistenceAdapter extends BaseRepository<StockMovementsRecord> implements StockMovementRepository {

    private static final MovementTypes MT = MOVEMENT_TYPES.as("mt");
    private static final StockLots SL = STOCK_LOTS.as("sl");
    private static final StockStatuses FS = STOCK_STATUSES.as("fs");
    private static final StockStatuses TS = STOCK_STATUSES.as("ts");
    private static final Warehouses FROM_WH = WAREHOUSES.as("from_wh");
    private static final Warehouses TO_WH = WAREHOUSES.as("to_wh");
    private static final WarehouseLocations FROM_LOC = WAREHOUSE_LOCATIONS.as("from_loc");
    private static final WarehouseLocations TO_LOC = WAREHOUSE_LOCATIONS.as("to_loc");
    private static final Users CREATOR = USERS.as("creator");

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
        ctx.insertInto(STOCK_MOVEMENTS).set(record)
                .onConflict(STOCK_MOVEMENTS.ID).doUpdate().set(record)
                .execute();
        return movement;
    }

    @Override
    public Optional<StockMovement> findById(UUID id) {
        return ctx.select()
                .from(STOCK_MOVEMENTS)
                .leftJoin(PRODUCTS).on(STOCK_MOVEMENTS.PRODUCT_ID.eq(PRODUCTS.ID))
                .leftJoin(MT).on(STOCK_MOVEMENTS.MOVEMENT_TYPE_ID.eq(MT.ID))
                .leftJoin(SL).on(STOCK_MOVEMENTS.LOT_ID.eq(SL.ID))
                .leftJoin(FS).on(STOCK_MOVEMENTS.FROM_STATUS_ID.eq(FS.ID))
                .leftJoin(TS).on(STOCK_MOVEMENTS.TO_STATUS_ID.eq(TS.ID))
                .leftJoin(FROM_WH).on(STOCK_MOVEMENTS.FROM_WAREHOUSE_ID.eq(FROM_WH.ID))
                .leftJoin(TO_WH).on(STOCK_MOVEMENTS.TO_WAREHOUSE_ID.eq(TO_WH.ID))
                .leftJoin(FROM_LOC).on(STOCK_MOVEMENTS.FROM_LOCATION_ID.eq(FROM_LOC.ID))
                .leftJoin(TO_LOC).on(STOCK_MOVEMENTS.TO_LOCATION_ID.eq(TO_LOC.ID))
                .leftJoin(CREATOR).on(STOCK_MOVEMENTS.CREATED_BY.eq(CREATOR.ID))
                .where(STOCK_MOVEMENTS.ID.eq(id))
                .fetchOptional(r -> mapRecord(r));
    }

    @Override
    public List<StockMovement> search(StockMovementSearchCriteria criteria) {
        Condition condition = buildCondition(criteria);
        List<SortField<?>> orderBy = SortUtils.resolveSorts(criteria.getSort(), SORT_FIELDS, DEFAULT_SORT_FIELD);
        return ctx.select()
                .from(STOCK_MOVEMENTS)
                .leftJoin(PRODUCTS).on(STOCK_MOVEMENTS.PRODUCT_ID.eq(PRODUCTS.ID))
                .leftJoin(MT).on(STOCK_MOVEMENTS.MOVEMENT_TYPE_ID.eq(MT.ID))
                .leftJoin(SL).on(STOCK_MOVEMENTS.LOT_ID.eq(SL.ID))
                .leftJoin(FS).on(STOCK_MOVEMENTS.FROM_STATUS_ID.eq(FS.ID))
                .leftJoin(TS).on(STOCK_MOVEMENTS.TO_STATUS_ID.eq(TS.ID))
                .leftJoin(FROM_WH).on(STOCK_MOVEMENTS.FROM_WAREHOUSE_ID.eq(FROM_WH.ID))
                .leftJoin(TO_WH).on(STOCK_MOVEMENTS.TO_WAREHOUSE_ID.eq(TO_WH.ID))
                .leftJoin(FROM_LOC).on(STOCK_MOVEMENTS.FROM_LOCATION_ID.eq(FROM_LOC.ID))
                .leftJoin(TO_LOC).on(STOCK_MOVEMENTS.TO_LOCATION_ID.eq(TO_LOC.ID))
                .leftJoin(CREATOR).on(STOCK_MOVEMENTS.CREATED_BY.eq(CREATOR.ID))
                .where(condition)
                .orderBy(orderBy)
                .limit(criteria.getSize())
                .offset((long) criteria.getPage() * criteria.getSize())
                .fetch(r -> mapRecord(r));
    }

    @Override
    public long count(StockMovementSearchCriteria criteria) {
        return count(buildCondition(criteria));
    }

    private StockMovement mapRecord(Record r) {
        StockMovementsRecord sm = r.into(STOCK_MOVEMENTS);
        MovementTypesRecord mt = r.into(MT);
        StockLotsRecord sl = r.into(SL);
        StockStatusesRecord fs = r.into(FS);
        StockStatusesRecord ts = r.into(TS);
        ProductsRecord product = r.into(PRODUCTS);
        WarehousesRecord fromWhRec = r.into(FROM_WH);
        WarehousesRecord toWhRec = r.into(TO_WH);
        WarehouseLocationsRecord fromLocRec = r.into(FROM_LOC);
        WarehouseLocationsRecord toLocRec = r.into(TO_LOC);
        UsersRecord creator = r.into(CREATOR);

        MovementType movementType = mt.getId() != null
                ? MovementType.builder().id(mt.getId()).name(mt.getName()).build()
                : null;

        StockLot stockLot = sl.getId() != null
                ? StockLot.builder().id(sl.getId()).lotNumber(sl.getLotNumber()).build()
                : null;

        StockStatus fromStatus = fs.getId() != null
                ? StockStatus.builder().id(fs.getId()).name(fs.getName()).build()
                : null;

        StockStatus toStatus = ts.getId() != null
                ? StockStatus.builder().id(ts.getId()).name(ts.getName()).build()
                : null;

        StockMovement.ProductRef productRef = product.getId() != null
                ? StockMovement.ProductRef.builder()
                        .id(product.getId()).code(product.getCode()).name(product.getName()).build()
                : null;

        StockMovement.WarehouseRef fromWarehouse = fromWhRec.getId() != null
                ? StockMovement.WarehouseRef.builder()
                        .id(fromWhRec.getId()).code(fromWhRec.getCode()).name(fromWhRec.getName()).build()
                : null;

        StockMovement.WarehouseRef toWarehouse = toWhRec.getId() != null
                ? StockMovement.WarehouseRef.builder()
                        .id(toWhRec.getId()).code(toWhRec.getCode()).name(toWhRec.getName()).build()
                : null;

        StockMovement.WarehouseLocationRef fromLocation = fromLocRec.getId() != null
                ? StockMovement.WarehouseLocationRef.builder()
                        .id(fromLocRec.getId()).code(fromLocRec.getCode()).name(fromLocRec.getName()).build()
                : null;

        StockMovement.WarehouseLocationRef toLocation = toLocRec.getId() != null
                ? StockMovement.WarehouseLocationRef.builder()
                        .id(toLocRec.getId()).code(toLocRec.getCode()).name(toLocRec.getName()).build()
                : null;

        StockMovement.UserRef createdByUser = creator.getId() != null
                ? StockMovement.UserRef.builder()
                        .id(creator.getId())
                        .username(creator.getUsername())
                        .fullName(creator.getFullName())
                        .build()
                : null;

        return StockMovement.builder()
                .id(sm.getId())
                .movementType(movementType)
                .productId(sm.getProductId())
                .stockLot(stockLot)
                .fromWarehouseId(sm.getFromWarehouseId())
                .fromLocationId(sm.getFromLocationId())
                .toWarehouseId(sm.getToWarehouseId())
                .toLocationId(sm.getToLocationId())
                .quantity(sm.getQuantity())
                .fromStatus(fromStatus)
                .toStatus(toStatus)
                .referenceNo(sm.getReferenceNo())
                .reason(sm.getReason())
                .createdBy(sm.getCreatedBy())
                .createdAt(sm.getCreatedAt() != null ? sm.getCreatedAt().toInstant() : null)
                .product(productRef)
                .fromWarehouse(fromWarehouse)
                .toWarehouse(toWarehouse)
                .fromLocation(fromLocation)
                .toLocation(toLocation)
                .createdByUser(createdByUser)
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
