# Data Model & DTO Specs: Inventory Summary Report

## 1. DTO Specifications

### `InventorySummaryReportDto` (`fpt.qn.mes.report.application.dto.response.InventorySummaryReportDto`)

```java
package fpt.qn.mes.report.application.dto.response;

import java.math.BigDecimal;
import lombok.*;
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
```

## 2. Query Filters Matrix

| Parameter | Type | Required | Description |
|---|---|---|---|
| `warehouseId` | `UUID` | No | Filter by specific warehouse |
| `productTypeId` | `UUID` | No | Filter by product type |
| `productCode` | `String` | No | Partial string search on product code |
