package fpt.qn.mes.quality.domain.entities;

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
public class QualityInspectionResult {
    UUID id;
    UUID inspectionId;
    Boolean isPass;
    BigDecimal quantity;
    UUID defectTypeId;
    String reason;
    UUID actionId;
    UUID inspectorId;
    Instant inspectedAt;
    String note;

    public static QualityInspectionResult create(UUID inspectionId, Boolean isPass, BigDecimal quantity,
            UUID defectTypeId, String reason, UUID actionId, UUID inspectorId, String note) {
        return QualityInspectionResult.builder()
            .id(UUID.randomUUID())
            .inspectionId(inspectionId)
            .isPass(isPass)
            .quantity(quantity)
            .defectTypeId(defectTypeId)
            .reason(reason)
            .actionId(actionId)
            .inspectorId(inspectorId)
            .inspectedAt(Instant.now())
            .note(note)
            .build();
    }
}
