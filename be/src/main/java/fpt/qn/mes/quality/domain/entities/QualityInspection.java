package fpt.qn.mes.quality.domain.entities;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import fpt.qn.mes.common.util.UuidV7;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class QualityInspection {

    @Getter @Builder @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class WorkOrderRef {
        UUID id;
        String code;
    }

    @Getter @Builder @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class ProductRef {
        UUID id;
        String code;
        String name;
    }

    @Getter @Builder @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class StockLotRef {
        UUID id;
        String lotNumber;
        String lotType;
    }

    UUID id;
    WorkOrderRef workOrder;
    ProductRef product;
    StockLotRef lot;
    BigDecimal quantity;
    QcStatus qcStatus;
    Instant createdAt;

    public static QualityInspection create(UUID workOrderId, UUID productId, UUID lotId,
            BigDecimal quantity, QcStatus qcStatus) {
        return QualityInspection.builder()
            .id(UuidV7.generate())
            .workOrder(WorkOrderRef.builder().id(workOrderId).build())
            .product(ProductRef.builder().id(productId).build())
            .lot(StockLotRef.builder().id(lotId).build())
            .quantity(quantity)
            .qcStatus(qcStatus)
            .createdAt(Instant.now())
            .build();
    }
}
