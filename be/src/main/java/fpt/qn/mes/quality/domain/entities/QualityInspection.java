package fpt.qn.mes.quality.domain.entities;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class QualityInspection {
    UUID id;
    UUID workOrderId;
    UUID productId;
    UUID lotId;
    BigDecimal quantity;
    UUID qcStatusId;
    Instant createdAt;
    List<QualityInspectionResult> results;

    public static QualityInspection create(UUID workOrderId, UUID productId, UUID lotId, BigDecimal quantity, UUID qcStatusId) {
        return QualityInspection.builder()
            .id(UUID.randomUUID())
            .workOrderId(workOrderId)
            .productId(productId)
            .lotId(lotId)
            .quantity(quantity)
            .qcStatusId(qcStatusId)
            .createdAt(Instant.now())
            .results(new ArrayList<>())
            .build();
    }

    public BigDecimal getRemainingQuantity() {
        if (results == null || results.isEmpty()) {
            return quantity;
        }
        BigDecimal processed = results.stream()
            .map(r -> r.getQuantity())
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        return quantity.subtract(processed);
    }

    public boolean isFullyProcessed() {
        return getRemainingQuantity().compareTo(BigDecimal.ZERO) <= 0;
    }
}
