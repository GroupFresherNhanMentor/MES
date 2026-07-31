package fpt.qn.mes.inventory.domain.entities;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Getter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StockAdjustmentApproval {

    @Getter @Builder @NoArgsConstructor @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class ProductRef { UUID id; String code; String name; }

    @Getter @Builder @NoArgsConstructor @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class WarehouseRef { UUID id; String code; String name; }

    @Getter @Builder @NoArgsConstructor @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class WarehouseLocationRef { UUID id; String code; String name; }

    @Getter @Builder @NoArgsConstructor @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class UserRef { UUID id; String username; String fullName; }

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

    ProductRef product;
    WarehouseRef warehouse;
    WarehouseLocationRef location;
    UserRef createdByUser;

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
