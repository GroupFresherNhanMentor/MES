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
import org.jooq.SortField;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;

import fpt.qn.mes.common.domainQuery.PaginationResult;
import fpt.qn.mes.common.repository.BaseRepository;
import fpt.qn.mes.common.repository.SortUtils;
import fpt.qn.mes.inventory.domain.entities.StockMovement;
import fpt.qn.mes.inventory.domain.repository.StockMovementRepository;
import fpt.qn.mes.inventory.domain.repository.criteria.StockMovementSearchCriteria;
import fpt.qn.mes.jooq.tables.MovementTypes;
import fpt.qn.mes.jooq.tables.StockLots;
import fpt.qn.mes.jooq.tables.StockStatuses;
import fpt.qn.mes.jooq.tables.Users;
import fpt.qn.mes.jooq.tables.WarehouseLocations;
import fpt.qn.mes.jooq.tables.Warehouses;
import fpt.qn.mes.jooq.tables.records.StockMovementsRecord;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@Repository
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class StockMovementPersistenceAdapter extends BaseRepository<StockMovementsRecord> implements StockMovementRepository {

    private static final MovementTypes MT       = MOVEMENT_TYPES.as("mt");
    private static final StockLots     SL       = STOCK_LOTS.as("sl");
    private static final StockStatuses FS       = STOCK_STATUSES.as("fs");
    private static final StockStatuses TS       = STOCK_STATUSES.as("ts");
    private static final Warehouses    FROM_WH  = WAREHOUSES.as("from_wh");
    private static final Warehouses    TO_WH    = WAREHOUSES.as("to_wh");
    private static final WarehouseLocations FROM_LOC = WAREHOUSE_LOCATIONS.as("from_loc");
    private static final WarehouseLocations TO_LOC   = WAREHOUSE_LOCATIONS.as("to_loc");
    private static final Users         CREATOR  = USERS.as("creator");

    private static final Map<String, Field<?>> SORT_FIELDS = Map.of(
            "created_at",   STOCK_MOVEMENTS.CREATED_AT,
            "quantity",     STOCK_MOVEMENTS.QUANTITY,
            "reference_no", STOCK_MOVEMENTS.REFERENCE_NO);
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
                .fetchOptional(r -> mapper.toDomain(
                        r.into(STOCK_MOVEMENTS), r.into(MT), r.into(SL), r.into(FS), r.into(TS),
                        r.into(PRODUCTS), r.into(FROM_WH), r.into(TO_WH), r.into(FROM_LOC), r.into(TO_LOC),
                        r.into(CREATOR)));
    }

    @Override
    public PaginationResult<StockMovement> search(StockMovementSearchCriteria criteria) {
        Condition condition = buildCondition(criteria);
        List<SortField<?>> orderBy = SortUtils.resolveSorts(criteria.getSort(), SORT_FIELDS, DEFAULT_SORT_FIELD);
        long total = ctx.fetchCount(STOCK_MOVEMENTS, condition);
        List<StockMovement> items = ctx.select()
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
                .fetch(r -> mapper.toDomain(
                        r.into(STOCK_MOVEMENTS), r.into(MT), r.into(SL), r.into(FS), r.into(TS),
                        r.into(PRODUCTS), r.into(FROM_WH), r.into(TO_WH), r.into(FROM_LOC), r.into(TO_LOC),
                        r.into(CREATOR)));
        return PaginationResult.<StockMovement>builder().total(total).items(items).build();
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
