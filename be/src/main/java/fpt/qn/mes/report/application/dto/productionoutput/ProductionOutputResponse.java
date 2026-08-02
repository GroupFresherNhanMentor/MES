package fpt.qn.mes.report.application.dto.productionoutput;

import java.math.BigDecimal;
import java.time.LocalDate;
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
public class ProductionOutputResponse {
    LocalDate date;
    UUID workOrderId;
    String workOrderCode;
    UUID productId;
    String productCode;
    String productName;
    BigDecimal plannedQuantity;
    BigDecimal actualQuantity;
    BigDecimal goodQuantity;
    BigDecimal defectQuantity;
    BigDecimal scrapQuantity;
    BigDecimal completionRate;
}
