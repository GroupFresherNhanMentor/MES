package fpt.qn.mes.workorder.application.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class WorkOrderMaterialDto {
    UUID id;
    UUID workOrderId;
    UUID materialProductId;
    BigDecimal requiredQuantity;
    BigDecimal reservedQuantity;
    BigDecimal consumedQuantity;
}
