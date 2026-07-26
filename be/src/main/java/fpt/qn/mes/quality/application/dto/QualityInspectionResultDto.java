package fpt.qn.mes.quality.application.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import lombok.Builder;
import lombok.Getter;

@Getter @Builder
public class QualityInspectionResultDto {
    UUID id; UUID inspectionId; Boolean isPass; BigDecimal quantity;
    UUID defectTypeId; String reason; String action; UUID inspectorId; Instant inspectedAt; String note;
}
