package fpt.qn.mes.quality.application.dto.request;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class CreateInspectionResultRequest {
    @NotNull Boolean isPass; @NotNull BigDecimal quantity;
    UUID defectTypeId; String reason; String action; UUID inspectorId; String note;
}
