package fpt.qn.mes.inventory.domain.entities;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StockAdjustmentApproval {

    UUID id;
    UUID productId;
    UUID warehouseId;
    UUID locationId;
    UUID stockBalanceId;
    BigDecimal quantityAdjustment;
    String reason;
    String referenceNo;
    UUID createdBy;
    Instant createdAt;

    public static StockAdjustmentApproval create(
            UUID productId,
            UUID warehouseId,
            UUID locationId,
            UUID stockBalanceId,
            BigDecimal quantityAdjustment,
            String reason,
            String referenceNo,
            UUID createdBy) {
        return StockAdjustmentApproval.builder()
                .productId(productId)
                .warehouseId(warehouseId)
                .locationId(locationId)
                .stockBalanceId(stockBalanceId)
                .quantityAdjustment(quantityAdjustment)
                .reason(reason)
                .referenceNo(referenceNo)
                .createdBy(createdBy)
                .createdAt(Instant.now())
                .build();
    }
}
