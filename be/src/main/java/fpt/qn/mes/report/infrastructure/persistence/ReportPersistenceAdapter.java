package fpt.qn.mes.report.infrastructure.persistence;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;

import fpt.qn.mes.report.application.dto.response.InventorySummaryReportDto;
import fpt.qn.mes.report.domain.repository.ReportRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import static fpt.qn.mes.jooq.Tables.PRODUCTS;
import static fpt.qn.mes.jooq.Tables.STOCK_BALANCES;
import static fpt.qn.mes.jooq.Tables.STOCK_STATUSES;
import static fpt.qn.mes.jooq.Tables.WAREHOUSES;

@Repository
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ReportPersistenceAdapter implements ReportRepository {

    DSLContext dslCtx;

    @Override
    public List<InventorySummaryReportDto> getInventorySummary(UUID warehouseId, UUID productTypeId, String productCode) {
        Condition condition = DSL.trueCondition();
        if (warehouseId != null) {
            condition = condition.and(STOCK_BALANCES.WAREHOUSE_ID.eq(warehouseId));
        }
        if (productTypeId != null) {
            condition = condition.and(PRODUCTS.PRODUCT_TYPE_ID.eq(productTypeId));
        }
        if (productCode != null && !productCode.isBlank()) {
            condition = condition.and(PRODUCTS.CODE.containsIgnoreCase(productCode));
        }

        var availableQty = DSL.sum(DSL.when(STOCK_STATUSES.NAME.eq("AVAILABLE"), STOCK_BALANCES.QUANTITY).otherwise(BigDecimal.ZERO));
        var reservedQty = DSL.sum(DSL.when(STOCK_STATUSES.NAME.eq("RESERVED"), STOCK_BALANCES.QUANTITY).otherwise(BigDecimal.ZERO));
        var qiQty = DSL.sum(DSL.when(STOCK_STATUSES.NAME.eq("QUALITY_INSPECTION"), STOCK_BALANCES.QUANTITY).otherwise(BigDecimal.ZERO));
        var onHoldQty = DSL.sum(DSL.when(STOCK_STATUSES.NAME.eq("ON_HOLD"), STOCK_BALANCES.QUANTITY).otherwise(BigDecimal.ZERO));
        var scrappedQty = DSL.sum(DSL.when(STOCK_STATUSES.NAME.eq("SCRAPPED"), STOCK_BALANCES.QUANTITY).otherwise(BigDecimal.ZERO));
        var totalOnHand = DSL.sum(STOCK_BALANCES.QUANTITY);

        return dslCtx.select(
                        PRODUCTS.CODE,
                        PRODUCTS.NAME,
                        WAREHOUSES.NAME,
                        availableQty.as("available_quantity"),
                        reservedQty.as("reserved_quantity"),
                        qiQty.as("quality_inspection_quantity"),
                        onHoldQty.as("on_hold_quantity"),
                        scrappedQty.as("scrapped_quantity"),
                        totalOnHand.as("total_on_hand")
                )
                .from(STOCK_BALANCES)
                .join(PRODUCTS).on(STOCK_BALANCES.PRODUCT_ID.eq(PRODUCTS.ID))
                .join(WAREHOUSES).on(STOCK_BALANCES.WAREHOUSE_ID.eq(WAREHOUSES.ID))
                .join(STOCK_STATUSES).on(STOCK_BALANCES.STOCK_STATUS_ID.eq(STOCK_STATUSES.ID))
                .where(condition)
                .groupBy(PRODUCTS.CODE, PRODUCTS.NAME, WAREHOUSES.NAME)
                .fetch(r -> InventorySummaryReportDto.builder()
                        .productCode(r.get(PRODUCTS.CODE))
                        .productName(r.get(PRODUCTS.NAME))
                        .warehouseName(r.get(WAREHOUSES.NAME))
                        .availableQuantity(r.get("available_quantity", BigDecimal.class) != null ? r.get("available_quantity", BigDecimal.class) : BigDecimal.ZERO)
                        .reservedQuantity(r.get("reserved_quantity", BigDecimal.class) != null ? r.get("reserved_quantity", BigDecimal.class) : BigDecimal.ZERO)
                        .qualityInspectionQuantity(r.get("quality_inspection_quantity", BigDecimal.class) != null ? r.get("quality_inspection_quantity", BigDecimal.class) : BigDecimal.ZERO)
                        .onHoldQuantity(r.get("on_hold_quantity", BigDecimal.class) != null ? r.get("on_hold_quantity", BigDecimal.class) : BigDecimal.ZERO)
                        .scrappedQuantity(r.get("scrapped_quantity", BigDecimal.class) != null ? r.get("scrapped_quantity", BigDecimal.class) : BigDecimal.ZERO)
                        .totalOnHand(r.get("total_on_hand", BigDecimal.class) != null ? r.get("total_on_hand", BigDecimal.class) : BigDecimal.ZERO)
                        .build()
                );
    }
}
