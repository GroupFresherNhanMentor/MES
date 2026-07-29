package fpt.qn.mes.report.application.dto.response;

import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InventorySummaryReportDto {
    String productCode;
    String productName;
    String warehouseName;
    BigDecimal availableQuantity;
    BigDecimal reservedQuantity;
    BigDecimal qualityInspectionQuantity;
    BigDecimal onHoldQuantity;
    BigDecimal scrappedQuantity;
    BigDecimal totalOnHand;
}
