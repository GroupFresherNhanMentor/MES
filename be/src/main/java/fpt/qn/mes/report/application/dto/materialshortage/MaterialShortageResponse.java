package fpt.qn.mes.report.application.dto.materialshortage;

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
public class MaterialShortageResponse {
    UUID workOrderId;
    String workOrderCode;
    UUID materialProductId;
    String materialCode;
    String materialName;
    BigDecimal requiredQuantity;
    BigDecimal reservedQuantity;
    BigDecimal availableQuantity;
    BigDecimal shortageQuantity;
}
