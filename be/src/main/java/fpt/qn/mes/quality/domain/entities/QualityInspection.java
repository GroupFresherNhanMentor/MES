package fpt.qn.mes.quality.domain.entities;

import java.math.BigDecimal;
import java.time.Instant;
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
        throw new UnsupportedOperationException("Not implemented");
    }
}
