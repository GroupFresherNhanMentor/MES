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
public class QualityInspectionResultResponse {

    @Getter @Builder @NoArgsConstructor @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class Inspector {
        UUID userId;
        String username;
        String fullName;
    }

    UUID id;
    UUID inspectionId;
    Boolean isPass;
    BigDecimal quantity;
    String defectTypeName;
    String reason;
    String actionName;
    Inspector inspector;
    Instant inspectedAt;
    String note;
}
