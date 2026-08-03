package fpt.qn.mes.workorder.application.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReservedMaterialAllocationResponse {
    UUID materialProductId;
    UUID lotId;
    UUID warehouseId;
    UUID locationId;
    BigDecimal reservedQuantity;
}
