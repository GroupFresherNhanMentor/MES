package fpt.qn.mes.report.infrastructure.persistence;

import static fpt.qn.mes.jooq.Tables.DEFECT_TYPES;
import static fpt.qn.mes.jooq.Tables.MACHINES;
import static fpt.qn.mes.jooq.Tables.MACHINE_DOWNTIMES;
import static fpt.qn.mes.jooq.Tables.MAINTENANCE_TICKETS;
import static fpt.qn.mes.jooq.Tables.MOVEMENT_TYPES;
import static fpt.qn.mes.jooq.Tables.PRODUCTION_RUNS;
import static fpt.qn.mes.jooq.Tables.PRODUCTS;
import static fpt.qn.mes.jooq.Tables.PRODUCT_TYPES;
import static fpt.qn.mes.jooq.Tables.QC_ACTIONS;
import static fpt.qn.mes.jooq.Tables.QUALITY_INSPECTIONS;
import static fpt.qn.mes.jooq.Tables.QUALITY_INSPECTION_RESULTS;
import static fpt.qn.mes.jooq.Tables.STOCK_BALANCES;
import static fpt.qn.mes.jooq.Tables.STOCK_LOTS;
import static fpt.qn.mes.jooq.Tables.STOCK_MOVEMENTS;
import static fpt.qn.mes.jooq.Tables.STOCK_STATUSES;
import static fpt.qn.mes.jooq.Tables.USERS;
import static fpt.qn.mes.jooq.Tables.WAREHOUSES;
import static fpt.qn.mes.jooq.Tables.WAREHOUSE_LOCATIONS;
import static fpt.qn.mes.jooq.Tables.WORK_ORDERS;
import static fpt.qn.mes.jooq.Tables.WORK_ORDER_MATERIALS;
import static fpt.qn.mes.jooq.Tables.WORK_ORDER_STATUSES;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.SelectConditionStep;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;
import fpt.qn.mes.common.domainQuery.PaginationResult;
import fpt.qn.mes.jooq.tables.Users;
import fpt.qn.mes.jooq.tables.WarehouseLocations;
import fpt.qn.mes.jooq.tables.Warehouses;
import fpt.qn.mes.report.domain.entities.DefectRate;
import fpt.qn.mes.report.domain.entities.InventorySummary;
import fpt.qn.mes.report.domain.entities.MachineDowntime;
import fpt.qn.mes.report.domain.entities.MaterialShortage;
import fpt.qn.mes.report.domain.entities.ProductionOutput;
import fpt.qn.mes.report.domain.entities.StockMovementHistory;
import fpt.qn.mes.report.domain.repository.ReportRepository;
import fpt.qn.mes.report.domain.repository.criteria.DefectRateSearchCriteria;
import fpt.qn.mes.report.domain.repository.criteria.InventorySummarySearchCriteria;
import fpt.qn.mes.report.domain.repository.criteria.MachineDowntimeSearchCriteria;
import fpt.qn.mes.report.domain.repository.criteria.ProductionOutputSearchCriteria;
import fpt.qn.mes.report.domain.repository.criteria.StockMovementHistorySearchCriteria;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Repository
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ReportPersistenceAdapter implements ReportRepository {

    DSLContext ctx;
    ReportRecordMapper mapper;

    @Override
    public PaginationResult<InventorySummary> getInventorySummary(InventorySummarySearchCriteria criteria) {
        Condition cond = DSL.noCondition();
        if (criteria.getWarehouseId() != null) {
            cond = cond.and(STOCK_BALANCES.WAREHOUSE_ID.eq(criteria.getWarehouseId()));
        }
        if (criteria.getProductType() != null && !criteria.getProductType().isBlank()) {
            cond = cond.and(PRODUCT_TYPES.NAME.equalIgnoreCase(criteria.getProductType()));
        }
        if (criteria.getProductCode() != null && !criteria.getProductCode().isBlank()) {
            cond = cond.and(PRODUCTS.CODE.containsIgnoreCase(criteria.getProductCode()));
        }

        Field<BigDecimal> availableQty = DSL.sum(
                DSL.when(STOCK_STATUSES.NAME.eq("AVAILABLE"), STOCK_BALANCES.QUANTITY).otherwise(BigDecimal.ZERO)
        ).as("available_qty");
        Field<BigDecimal> reservedQty = DSL.sum(
                DSL.when(STOCK_STATUSES.NAME.eq("RESERVED"), STOCK_BALANCES.QUANTITY).otherwise(BigDecimal.ZERO)
        ).as("reserved_qty");
        Field<BigDecimal> qiQty = DSL.sum(
                DSL.when(STOCK_STATUSES.NAME.eq("QUALITY_INSPECTION"), STOCK_BALANCES.QUANTITY).otherwise(BigDecimal.ZERO)
        ).as("qi_qty");
        Field<BigDecimal> onHoldQty = DSL.sum(
                DSL.when(STOCK_STATUSES.NAME.eq("ON_HOLD"), STOCK_BALANCES.QUANTITY).otherwise(BigDecimal.ZERO)
        ).as("on_hold_qty");
        Field<BigDecimal> scrappedQty = DSL.sum(
                DSL.when(STOCK_STATUSES.NAME.eq("SCRAPPED"), STOCK_BALANCES.QUANTITY).otherwise(BigDecimal.ZERO)
        ).as("scrapped_qty");

        SelectConditionStep<?> baseQuery = ctx.select(
                PRODUCTS.ID.as("product_id"),
                PRODUCTS.CODE.as("product_code"),
                PRODUCTS.NAME.as("product_name"),
                PRODUCT_TYPES.NAME.as("product_type"),
                WAREHOUSES.ID.as("warehouse_id"),
                WAREHOUSES.NAME.as("warehouse_name"),
                availableQty,
                reservedQty,
                qiQty,
                onHoldQty,
                scrappedQty
        )
        .from(STOCK_BALANCES)
        .join(PRODUCTS).on(STOCK_BALANCES.PRODUCT_ID.eq(PRODUCTS.ID))
        .leftJoin(PRODUCT_TYPES).on(PRODUCTS.PRODUCT_TYPE_ID.eq(PRODUCT_TYPES.ID))
        .join(WAREHOUSES).on(STOCK_BALANCES.WAREHOUSE_ID.eq(WAREHOUSES.ID))
        .join(STOCK_STATUSES).on(STOCK_BALANCES.STOCK_STATUS_ID.eq(STOCK_STATUSES.ID))
        .where(cond);

        long total = ctx.fetchCount(
                ctx.select(STOCK_BALANCES.PRODUCT_ID, STOCK_BALANCES.WAREHOUSE_ID)
                        .from(STOCK_BALANCES)
                        .join(PRODUCTS).on(STOCK_BALANCES.PRODUCT_ID.eq(PRODUCTS.ID))
                        .leftJoin(PRODUCT_TYPES).on(PRODUCTS.PRODUCT_TYPE_ID.eq(PRODUCT_TYPES.ID))
                        .join(WAREHOUSES).on(STOCK_BALANCES.WAREHOUSE_ID.eq(WAREHOUSES.ID))
                        .join(STOCK_STATUSES).on(STOCK_BALANCES.STOCK_STATUS_ID.eq(STOCK_STATUSES.ID))
                        .where(cond)
                        .groupBy(STOCK_BALANCES.PRODUCT_ID, STOCK_BALANCES.WAREHOUSE_ID)
        );

        List<InventorySummary> items = baseQuery
                .groupBy(PRODUCTS.ID, PRODUCTS.CODE, PRODUCTS.NAME, PRODUCT_TYPES.NAME, WAREHOUSES.ID, WAREHOUSES.NAME)
                .orderBy(PRODUCTS.CODE.asc(), WAREHOUSES.NAME.asc())
                .limit(criteria.getSize())
                .offset((long) criteria.getPage() * criteria.getSize())
                .fetch(mapper::toInventorySummary);

        return PaginationResult.<InventorySummary>builder()
                .total(total)
                .items(items)
                .build();
    }

    @Override
    public List<MaterialShortage> getMaterialShortages() {
        Condition cond = WORK_ORDER_MATERIALS.REQUIRED_QUANTITY.gt(WORK_ORDER_MATERIALS.RESERVED_QUANTITY)
                .and(WORK_ORDER_STATUSES.NAME.notIn("CANCELLED", "COMPLETED", "DRAFT"));

        var availStockTable = ctx.select(
                STOCK_BALANCES.PRODUCT_ID.as("product_id"),
                DSL.sum(STOCK_BALANCES.QUANTITY).as("avail_qty")
        )
        .from(STOCK_BALANCES)
        .join(STOCK_STATUSES).on(STOCK_BALANCES.STOCK_STATUS_ID.eq(STOCK_STATUSES.ID))
        .where(STOCK_STATUSES.NAME.eq("AVAILABLE"))
        .groupBy(STOCK_BALANCES.PRODUCT_ID)
        .asTable("avail_stock");

        Field<BigDecimal> availQty = DSL.coalesce(
                availStockTable.field("avail_qty", BigDecimal.class),
                BigDecimal.ZERO
        ).as("available_quantity");

        return ctx.select(
                WORK_ORDERS.ID.as("work_order_id"),
                WORK_ORDERS.CODE.as("work_order_code"),
                WORK_ORDER_MATERIALS.MATERIAL_PRODUCT_ID.as("material_product_id"),
                PRODUCTS.CODE.as("material_code"),
                PRODUCTS.NAME.as("material_name"),
                WORK_ORDER_MATERIALS.REQUIRED_QUANTITY.as("required_quantity"),
                WORK_ORDER_MATERIALS.RESERVED_QUANTITY.as("reserved_quantity"),
                availQty
        )
        .from(WORK_ORDER_MATERIALS)
        .join(WORK_ORDERS).on(WORK_ORDER_MATERIALS.WORK_ORDER_ID.eq(WORK_ORDERS.ID))
        .join(WORK_ORDER_STATUSES).on(WORK_ORDERS.WORK_ORDER_STATUS_ID.eq(WORK_ORDER_STATUSES.ID))
        .join(PRODUCTS).on(WORK_ORDER_MATERIALS.MATERIAL_PRODUCT_ID.eq(PRODUCTS.ID))
        .leftJoin(availStockTable).on(WORK_ORDER_MATERIALS.MATERIAL_PRODUCT_ID.eq(availStockTable.field("product_id", java.util.UUID.class)))
        .where(cond)
        .orderBy(WORK_ORDERS.CODE.asc(), PRODUCTS.CODE.asc())
        .fetch(mapper::toMaterialShortage);
    }

    @Override
    public PaginationResult<ProductionOutput> getProductionOutput(ProductionOutputSearchCriteria criteria) {
        Condition cond = DSL.noCondition();
        if (criteria.getFromDate() != null) {
            cond = cond.and(PRODUCTION_RUNS.START_TIME.ge(OffsetDateTime.of(criteria.getFromDate().atStartOfDay(), ZoneOffset.UTC)));
        }
        if (criteria.getToDate() != null) {
            cond = cond.and(PRODUCTION_RUNS.START_TIME.lt(OffsetDateTime.of(criteria.getToDate().plusDays(1).atStartOfDay(), ZoneOffset.UTC)));
        }

        Field<Date> prodDate = DSL.cast(PRODUCTION_RUNS.START_TIME, Date.class).as("prod_date");
        Field<BigDecimal> actualSum = DSL.sum(PRODUCTION_RUNS.ACTUAL_QUANTITY).as("actual_quantity");
        Field<BigDecimal> goodSum = DSL.sum(PRODUCTION_RUNS.GOOD_QUANTITY).as("good_quantity");
        Field<BigDecimal> defectSum = DSL.sum(PRODUCTION_RUNS.DEFECT_QUANTITY).as("defect_quantity");
        Field<BigDecimal> scrapSum = DSL.sum(PRODUCTION_RUNS.SCRAP_QUANTITY).as("scrap_quantity");

        SelectConditionStep<?> baseQuery = ctx.select(
                prodDate,
                WORK_ORDERS.ID.as("work_order_id"),
                WORK_ORDERS.CODE.as("work_order_code"),
                PRODUCTS.ID.as("product_id"),
                PRODUCTS.CODE.as("product_code"),
                PRODUCTS.NAME.as("product_name"),
                WORK_ORDERS.PLANNED_QUANTITY.as("planned_quantity"),
                actualSum,
                goodSum,
                defectSum,
                scrapSum
        )
        .from(PRODUCTION_RUNS)
        .join(WORK_ORDERS).on(PRODUCTION_RUNS.WORK_ORDER_ID.eq(WORK_ORDERS.ID))
        .join(PRODUCTS).on(WORK_ORDERS.FINISHED_PRODUCT_ID.eq(PRODUCTS.ID))
        .where(cond);

        long total = ctx.fetchCount(
                ctx.selectDistinct(DSL.cast(PRODUCTION_RUNS.START_TIME, Date.class), PRODUCTION_RUNS.WORK_ORDER_ID)
                        .from(PRODUCTION_RUNS)
                        .where(cond)
        );

        List<ProductionOutput> items = baseQuery
                .groupBy(prodDate, WORK_ORDERS.ID, WORK_ORDERS.CODE, PRODUCTS.ID, PRODUCTS.CODE, PRODUCTS.NAME, WORK_ORDERS.PLANNED_QUANTITY)
                .orderBy(prodDate.desc(), WORK_ORDERS.CODE.asc())
                .limit(criteria.getSize())
                .offset((long) criteria.getPage() * criteria.getSize())
                .fetch(mapper::toProductionOutput);

        return PaginationResult.<ProductionOutput>builder()
                .total(total)
                .items(items)
                .build();
    }

    @Override
    public List<DefectRate> getDefectRates(DefectRateSearchCriteria criteria) {
        Condition cond = DSL.noCondition();
        if (criteria.getFromDate() != null) {
            cond = cond.and(QUALITY_INSPECTION_RESULTS.INSPECTED_AT.ge(OffsetDateTime.of(criteria.getFromDate().atStartOfDay(), ZoneOffset.UTC)));
        }
        if (criteria.getToDate() != null) {
            cond = cond.and(QUALITY_INSPECTION_RESULTS.INSPECTED_AT.lt(OffsetDateTime.of(criteria.getToDate().plusDays(1).atStartOfDay(), ZoneOffset.UTC)));
        }

        Field<BigDecimal> totalInspected = DSL.sum(QUALITY_INSPECTION_RESULTS.QUANTITY).as("total_inspected");
        Field<BigDecimal> defectQty = DSL.sum(
                DSL.when(QUALITY_INSPECTION_RESULTS.IS_PASS.eq(false), QUALITY_INSPECTION_RESULTS.QUANTITY).otherwise(BigDecimal.ZERO)
        ).as("defect_quantity");
        Field<BigDecimal> scrapQty = DSL.sum(
                DSL.when(QC_ACTIONS.NAME.eq("SCRAP"), QUALITY_INSPECTION_RESULTS.QUANTITY).otherwise(BigDecimal.ZERO)
        ).as("scrap_quantity");

        Field<String> topDefectTypes = ctx.select(
                DSL.field("string_agg(t.name, ', ')", String.class)
        ).from(
                ctx.select(DEFECT_TYPES.NAME.as("name"))
                        .from(QUALITY_INSPECTION_RESULTS)
                        .join(QUALITY_INSPECTIONS).on(QUALITY_INSPECTION_RESULTS.INSPECTION_ID.eq(QUALITY_INSPECTIONS.ID))
                        .join(DEFECT_TYPES).on(QUALITY_INSPECTION_RESULTS.DEFECT_TYPE_ID.eq(DEFECT_TYPES.ID))
                        .where(QUALITY_INSPECTIONS.PRODUCT_ID.eq(PRODUCTS.ID)
                                .and(QUALITY_INSPECTION_RESULTS.IS_PASS.eq(false)))
                        .groupBy(DEFECT_TYPES.ID, DEFECT_TYPES.NAME)
                        .orderBy(DSL.count().desc())
                        .limit(3)
                        .asTable("t")
        ).asField("top_defect_types");

        return ctx.select(
                PRODUCTS.ID.as("product_id"),
                PRODUCTS.CODE.as("product_code"),
                PRODUCTS.NAME.as("product_name"),
                totalInspected,
                defectQty,
                scrapQty,
                topDefectTypes
        )
        .from(QUALITY_INSPECTION_RESULTS)
        .join(QUALITY_INSPECTIONS).on(QUALITY_INSPECTION_RESULTS.INSPECTION_ID.eq(QUALITY_INSPECTIONS.ID))
        .join(PRODUCTS).on(QUALITY_INSPECTIONS.PRODUCT_ID.eq(PRODUCTS.ID))
        .leftJoin(DEFECT_TYPES).on(QUALITY_INSPECTION_RESULTS.DEFECT_TYPE_ID.eq(DEFECT_TYPES.ID))
        .leftJoin(QC_ACTIONS).on(QUALITY_INSPECTION_RESULTS.ACTION_ID.eq(QC_ACTIONS.ID))
        .where(cond)
        .groupBy(PRODUCTS.ID, PRODUCTS.CODE, PRODUCTS.NAME)
        .orderBy(PRODUCTS.CODE.asc())
        .fetch(mapper::toDefectRate);
    }

    @Override
    public List<MachineDowntime> getMachineDowntimes(MachineDowntimeSearchCriteria criteria) {
        Condition downtimeCond = MACHINE_DOWNTIMES.MACHINE_ID.eq(MACHINES.ID);
        if (criteria.getFromDate() != null) {
            downtimeCond = downtimeCond.and(MACHINE_DOWNTIMES.START_TIME.ge(OffsetDateTime.of(criteria.getFromDate().atStartOfDay(), ZoneOffset.UTC)));
        }
        if (criteria.getToDate() != null) {
            downtimeCond = downtimeCond.and(MACHINE_DOWNTIMES.START_TIME.lt(OffsetDateTime.of(criteria.getToDate().plusDays(1).atStartOfDay(), ZoneOffset.UTC)));
        }

        Field<Long> totalMinutes = DSL.sum(DSL.coalesce(MACHINE_DOWNTIMES.TOTAL_DOWNTIME_MINUTES, 0L)).cast(Long.class).as("total_downtime_minutes");
        Field<Long> ticketCount = DSL.countDistinct(MAINTENANCE_TICKETS.ID).cast(Long.class).as("ticket_count");

        Condition lastReasonCond = MACHINE_DOWNTIMES.MACHINE_ID.eq(MACHINES.ID);
        if (criteria.getFromDate() != null) {
            lastReasonCond = lastReasonCond.and(MACHINE_DOWNTIMES.START_TIME.ge(OffsetDateTime.of(criteria.getFromDate().atStartOfDay(), ZoneOffset.UTC)));
        }
        if (criteria.getToDate() != null) {
            lastReasonCond = lastReasonCond.and(MACHINE_DOWNTIMES.START_TIME.lt(OffsetDateTime.of(criteria.getToDate().plusDays(1).atStartOfDay(), ZoneOffset.UTC)));
        }

        Field<String> lastReason = ctx.select(MACHINE_DOWNTIMES.ROOT_CAUSE)
                .from(MACHINE_DOWNTIMES)
                .where(lastReasonCond)
                .orderBy(MACHINE_DOWNTIMES.START_TIME.desc())
                .limit(1)
                .asField("last_reason");

        return ctx.select(
                MACHINES.ID.as("machine_id"),
                MACHINES.CODE.as("machine_code"),
                MACHINES.NAME.as("machine_name"),
                totalMinutes,
                ticketCount,
                lastReason
        )
        .from(MACHINES)
        .leftJoin(MACHINE_DOWNTIMES).on(downtimeCond)
        .leftJoin(MAINTENANCE_TICKETS).on(MACHINE_DOWNTIMES.TICKET_ID.eq(MAINTENANCE_TICKETS.ID))
        .groupBy(MACHINES.ID, MACHINES.CODE, MACHINES.NAME)
        .orderBy(MACHINES.CODE.asc())
        .fetch(mapper::toMachineDowntime);
    }

    @Override
    public PaginationResult<StockMovementHistory> getStockMovementHistory(StockMovementHistorySearchCriteria criteria) {
        Condition cond = DSL.noCondition();
        if (criteria.getProductId() != null) {
            cond = cond.and(STOCK_MOVEMENTS.PRODUCT_ID.eq(criteria.getProductId()));
        }
        if (criteria.getWarehouseId() != null) {
            cond = cond.and(STOCK_MOVEMENTS.FROM_WAREHOUSE_ID.eq(criteria.getWarehouseId())
                    .or(STOCK_MOVEMENTS.TO_WAREHOUSE_ID.eq(criteria.getWarehouseId())));
        }
        if (criteria.getMovementTypeId() != null) {
            cond = cond.and(STOCK_MOVEMENTS.MOVEMENT_TYPE_ID.eq(criteria.getMovementTypeId()));
        }
        if (criteria.getFromDate() != null) {
            cond = cond.and(STOCK_MOVEMENTS.CREATED_AT.ge(OffsetDateTime.of(criteria.getFromDate().atStartOfDay(), ZoneOffset.UTC)));
        }
        if (criteria.getToDate() != null) {
            cond = cond.and(STOCK_MOVEMENTS.CREATED_AT.lt(OffsetDateTime.of(criteria.getToDate().plusDays(1).atStartOfDay(), ZoneOffset.UTC)));
        }
        if (criteria.getReferenceType() != null && !criteria.getReferenceType().isBlank()) {
            if ("WORK_ORDER".equalsIgnoreCase(criteria.getReferenceType())) {
                cond = cond.and(STOCK_MOVEMENTS.WORK_ORDER_ID.isNotNull());
            } else if ("REFERENCE_NO".equalsIgnoreCase(criteria.getReferenceType())) {
                cond = cond.and(STOCK_MOVEMENTS.REFERENCE_NO.isNotNull());
            }
        }
        if (criteria.getReferenceId() != null) {
            cond = cond.and(STOCK_MOVEMENTS.WORK_ORDER_ID.eq(criteria.getReferenceId()));
        }

        Warehouses fromWh = WAREHOUSES.as("from_wh");
        Warehouses toWh = WAREHOUSES.as("to_wh");
        WarehouseLocations fromLoc = WAREHOUSE_LOCATIONS.as("from_loc");
        WarehouseLocations toLoc = WAREHOUSE_LOCATIONS.as("to_loc");
        Users creator = USERS.as("creator");

        SelectConditionStep<?> baseQuery = ctx.select(
                STOCK_MOVEMENTS.ID.as("movement_id"),
                STOCK_MOVEMENTS.CREATED_AT.as("movement_time"),
                MOVEMENT_TYPES.NAME.as("movement_type"),
                PRODUCTS.ID.as("product_id"),
                PRODUCTS.CODE.as("product_code"),
                PRODUCTS.NAME.as("product_name"),
                STOCK_LOTS.LOT_NUMBER.as("lot_number"),
                fromWh.NAME.as("from_warehouse"),
                fromLoc.NAME.as("from_location"),
                toWh.NAME.as("to_warehouse"),
                toLoc.NAME.as("to_location"),
                STOCK_MOVEMENTS.QUANTITY.as("quantity"),
                STOCK_MOVEMENTS.WORK_ORDER_ID.as("work_order_id"),
                creator.FULL_NAME.as("created_by_name"),
                STOCK_MOVEMENTS.REASON.as("reason")
        )
        .from(STOCK_MOVEMENTS)
        .join(MOVEMENT_TYPES).on(STOCK_MOVEMENTS.MOVEMENT_TYPE_ID.eq(MOVEMENT_TYPES.ID))
        .join(PRODUCTS).on(STOCK_MOVEMENTS.PRODUCT_ID.eq(PRODUCTS.ID))
        .leftJoin(STOCK_LOTS).on(STOCK_MOVEMENTS.LOT_ID.eq(STOCK_LOTS.ID))
        .leftJoin(fromWh).on(STOCK_MOVEMENTS.FROM_WAREHOUSE_ID.eq(fromWh.ID))
        .leftJoin(fromLoc).on(STOCK_MOVEMENTS.FROM_LOCATION_ID.eq(fromLoc.ID))
        .leftJoin(toWh).on(STOCK_MOVEMENTS.TO_WAREHOUSE_ID.eq(toWh.ID))
        .leftJoin(toLoc).on(STOCK_MOVEMENTS.TO_LOCATION_ID.eq(toLoc.ID))
        .leftJoin(creator).on(STOCK_MOVEMENTS.CREATED_BY.eq(creator.ID))
        .where(cond);

        long total = ctx.fetchCount(
                ctx.selectFrom(STOCK_MOVEMENTS).where(cond)
        );

        List<StockMovementHistory> items = baseQuery
                .orderBy(STOCK_MOVEMENTS.CREATED_AT.desc())
                .limit(criteria.getSize())
                .offset((long) criteria.getPage() * criteria.getSize())
                .fetch(mapper::toStockMovementHistory);

        return PaginationResult.<StockMovementHistory>builder()
                .total(total)
                .items(items)
                .build();
    }
}
