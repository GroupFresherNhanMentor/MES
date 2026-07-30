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

    @Getter @Builder @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class InspectorRef {
        UUID userId;
        String username;
        String fullName;
    }

    UUID id;
    UUID inspectionId;
    Boolean isPass;
    BigDecimal quantity;
    DefectType defectType;
    String reason;
    QcAction action;
    InspectorRef inspector;
    Instant inspectedAt;
    String note;

    public static QualityInspectionResult create(UUID inspectionId, Boolean isPass, BigDecimal quantity,
            DefectType defectType, String reason, QcAction action, UUID inspectorId, String note) {
        return QualityInspectionResult.builder()
            .id(UUID.randomUUID())
            .inspectionId(inspectionId)
            .isPass(isPass)
            .quantity(quantity)
            .defectType(defectType)
            .reason(reason)
            .action(action)
            .inspector(InspectorRef.builder().userId(inspectorId).build())
            .inspectedAt(Instant.now())
            .note(note)
            .build();
    }
}
