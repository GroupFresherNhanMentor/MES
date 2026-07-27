package fpt.qn.mes.quality.application.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import lombok.Builder;
import lombok.Getter;

@Getter @Builder
public class QualityInspectionDto {
    UUID id; UUID workOrderId; UUID productId; UUID lotId;
    BigDecimal quantity; UUID qcStatusId; Instant createdAt;
    List<QualityInspectionResultDto> results;
}
