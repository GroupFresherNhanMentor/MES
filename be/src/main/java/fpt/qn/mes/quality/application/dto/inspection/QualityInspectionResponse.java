package fpt.qn.mes.quality.application.dto.inspection;

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
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class QualityInspectionResponse {

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class WorkOrderInfo {
        UUID workOrderId;
        String workOrderCode;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class ProductInfo {
        UUID productId;
        String productCode;
        String productName;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class StockLotInfo {
        UUID lotId;
        String lotNumber;
        String lotType;
    }

    UUID id;
    WorkOrderInfo workOrder;
    ProductInfo product;
    StockLotInfo lot;
    BigDecimal quantity;
    String qcStatusName;
    Instant createdAt;
}
