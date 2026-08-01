package fpt.qn.mes.report.application.dto.inventorysummary;

import java.math.BigDecimal;
import java.util.UUID;
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
public class InventorySummaryResponse {
    UUID productId;
    String productCode;
    String productName;
    String productType;
    UUID warehouseId;
    String warehouseName;
    BigDecimal availableQuantity;
    BigDecimal reservedQuantity;
    BigDecimal qualityInspectionQuantity;
    BigDecimal onHoldQuantity;
    BigDecimal scrappedQuantity;
    BigDecimal totalOnHand;
}
