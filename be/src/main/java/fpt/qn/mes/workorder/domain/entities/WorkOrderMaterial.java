package fpt.qn.mes.workorder.domain.entities;

import java.math.BigDecimal;
import java.util.UUID;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class WorkOrderMaterial {
    UUID id;
    UUID workOrderId;
    UUID materialProductId;
    BigDecimal requiredQuantity;
    BigDecimal reservedQuantity;
    BigDecimal consumedQuantity;

    public static WorkOrderMaterial create(UUID workOrderId, UUID materialProductId, BigDecimal requiredQuantity) {
        throw new UnsupportedOperationException("Not implemented");
    }
}
