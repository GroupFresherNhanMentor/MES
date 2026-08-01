package fpt.qn.mes.report.infrastructure.persistence;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.jooq.Record;
import org.springframework.stereotype.Component;
import fpt.qn.mes.report.domain.entities.DefectRate;
import fpt.qn.mes.report.domain.entities.InventorySummary;
import fpt.qn.mes.report.domain.entities.MachineDowntime;
import fpt.qn.mes.report.domain.entities.MaterialShortage;
import fpt.qn.mes.report.domain.entities.ProductionOutput;
import fpt.qn.mes.report.domain.entities.StockMovementHistory;

@Component
public class ReportRecordMapper {

    public InventorySummary toInventorySummary(Record r) {
        BigDecimal available = r.get("available_qty", BigDecimal.class);
        BigDecimal reserved = r.get("reserved_qty", BigDecimal.class);
        BigDecimal qi = r.get("qi_qty", BigDecimal.class);
        BigDecimal onHold = r.get("on_hold_qty", BigDecimal.class);
        BigDecimal scrapped = r.get("scrapped_qty", BigDecimal.class);

        available = available != null ? available : BigDecimal.ZERO;
        reserved = reserved != null ? reserved : BigDecimal.ZERO;
        qi = qi != null ? qi : BigDecimal.ZERO;
        onHold = onHold != null ? onHold : BigDecimal.ZERO;
        scrapped = scrapped != null ? scrapped : BigDecimal.ZERO;

        BigDecimal totalOnHand = available.add(reserved).add(qi).add(onHold).add(scrapped);

        return InventorySummary.builder()
                .productId(r.get("product_id", UUID.class))
                .productCode(r.get("product_code", String.class))
                .productName(r.get("product_name", String.class))
                .productType(r.get("product_type", String.class))
                .warehouseId(r.get("warehouse_id", UUID.class))
                .warehouseName(r.get("warehouse_name", String.class))
                .availableQuantity(available)
                .reservedQuantity(reserved)
                .qualityInspectionQuantity(qi)
                .onHoldQuantity(onHold)
                .scrappedQuantity(scrapped)
                .totalOnHand(totalOnHand)
                .build();
    }

    public MaterialShortage toMaterialShortage(Record r) {
        BigDecimal required = r.get("required_quantity", BigDecimal.class);
        BigDecimal reserved = r.get("reserved_quantity", BigDecimal.class);
        BigDecimal available = r.get("available_quantity", BigDecimal.class);

        required = required != null ? required : BigDecimal.ZERO;
        reserved = reserved != null ? reserved : BigDecimal.ZERO;
        available = available != null ? available : BigDecimal.ZERO;

        BigDecimal needed = required.subtract(reserved);
        BigDecimal shortage = needed.subtract(available);
        if (shortage.compareTo(BigDecimal.ZERO) < 0) {
            shortage = BigDecimal.ZERO;
        }

        return MaterialShortage.builder()
                .workOrderId(r.get("work_order_id", UUID.class))
                .workOrderCode(r.get("work_order_code", String.class))
                .materialProductId(r.get("material_product_id", UUID.class))
                .materialCode(r.get("material_code", String.class))
                .materialName(r.get("material_name", String.class))
                .requiredQuantity(required)
                .reservedQuantity(reserved)
                .availableQuantity(available)
                .shortageQuantity(shortage)
                .build();
    }

    public ProductionOutput toProductionOutput(Record r) {
        Date sqlDate = r.get("prod_date", Date.class);
        LocalDate date = sqlDate != null ? sqlDate.toLocalDate() : null;

        BigDecimal planned = r.get("planned_quantity", BigDecimal.class);
        BigDecimal actual = r.get("actual_quantity", BigDecimal.class);
        BigDecimal good = r.get("good_quantity", BigDecimal.class);
        BigDecimal defect = r.get("defect_quantity", BigDecimal.class);
        BigDecimal scrap = r.get("scrap_quantity", BigDecimal.class);

        planned = planned != null ? planned : BigDecimal.ZERO;
        actual = actual != null ? actual : BigDecimal.ZERO;
        good = good != null ? good : BigDecimal.ZERO;
        defect = defect != null ? defect : BigDecimal.ZERO;
        scrap = scrap != null ? scrap : BigDecimal.ZERO;

        BigDecimal completionRate = BigDecimal.ZERO;
        if (planned.compareTo(BigDecimal.ZERO) > 0) {
            completionRate = actual.multiply(BigDecimal.valueOf(100)).divide(planned, 2, RoundingMode.HALF_UP);
        }

        return ProductionOutput.builder()
                .date(date)
                .workOrderId(r.get("work_order_id", UUID.class))
                .workOrderCode(r.get("work_order_code", String.class))
                .productId(r.get("product_id", UUID.class))
                .productCode(r.get("product_code", String.class))
                .productName(r.get("product_name", String.class))
                .plannedQuantity(planned)
                .actualQuantity(actual)
                .goodQuantity(good)
                .defectQuantity(defect)
                .scrapQuantity(scrap)
                .completionRate(completionRate)
                .build();
    }

    public DefectRate toDefectRate(Record r) {
        BigDecimal inspected = r.get("total_inspected", BigDecimal.class);
        BigDecimal defect = r.get("defect_quantity", BigDecimal.class);
        BigDecimal scrap = r.get("scrap_quantity", BigDecimal.class);

        inspected = inspected != null ? inspected : BigDecimal.ZERO;
        defect = defect != null ? defect : BigDecimal.ZERO;
        scrap = scrap != null ? scrap : BigDecimal.ZERO;

        BigDecimal defectRate = BigDecimal.ZERO;
        if (inspected.compareTo(BigDecimal.ZERO) > 0) {
            defectRate = defect.multiply(BigDecimal.valueOf(100)).divide(inspected, 2, RoundingMode.HALF_UP);
        }

        String topDefectStr = r.get("top_defect_types", String.class);
        List<String> topDefectTypes = Collections.emptyList();
        if (topDefectStr != null && !topDefectStr.isBlank()) {
            topDefectTypes = Arrays.asList(topDefectStr.split(",\\s*"));
        }

        return DefectRate.builder()
                .productId(r.get("product_id", UUID.class))
                .productCode(r.get("product_code", String.class))
                .productName(r.get("product_name", String.class))
                .totalInspected(inspected)
                .defectQuantity(defect)
                .scrapQuantity(scrap)
                .defectRate(defectRate)
                .topDefectTypes(topDefectTypes)
                .build();
    }

    public MachineDowntime toMachineDowntime(Record r) {
        Long downtimeMinutes = r.get("total_downtime_minutes", Long.class);
        Long ticketCount = r.get("ticket_count", Long.class);

        return MachineDowntime.builder()
                .machineId(r.get("machine_id", UUID.class))
                .machineCode(r.get("machine_code", String.class))
                .machineName(r.get("machine_name", String.class))
                .totalDowntimeMinutes(downtimeMinutes != null ? downtimeMinutes : 0L)
                .maintenanceTicketCount(ticketCount != null ? ticketCount : 0L)
                .lastDowntimeReason(r.get("last_reason", String.class))
                .build();
    }

    public StockMovementHistory toStockMovementHistory(Record r) {
        OffsetDateTime odt = r.get("movement_time", OffsetDateTime.class);
        Instant movementTime = odt != null ? odt.toInstant() : null;

        UUID woId = r.get("work_order_id", UUID.class);
        String refType = woId != null ? "WORK_ORDER" : null;
        UUID refId = woId;

        return StockMovementHistory.builder()
                .movementId(r.get("movement_id", UUID.class))
                .movementTime(movementTime)
                .movementType(r.get("movement_type", String.class))
                .productId(r.get("product_id", UUID.class))
                .productCode(r.get("product_code", String.class))
                .productName(r.get("product_name", String.class))
                .lotNumber(r.get("lot_number", String.class))
                .fromWarehouse(r.get("from_warehouse", String.class))
                .fromLocation(r.get("from_location", String.class))
                .toWarehouse(r.get("to_warehouse", String.class))
                .toLocation(r.get("to_location", String.class))
                .quantity(r.get("quantity", BigDecimal.class))
                .referenceType(refType)
                .referenceId(refId)
                .createdBy(r.get("created_by_name", String.class))
                .reason(r.get("reason", String.class))
                .build();
    }
}
