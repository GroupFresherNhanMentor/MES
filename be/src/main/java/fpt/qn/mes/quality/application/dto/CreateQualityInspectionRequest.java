package fpt.qn.mes.quality.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class CreateQualityInspectionRequest {
    UUID workOrderId; @NotNull UUID productId; UUID lotId;
    @NotNull BigDecimal quantity; UUID qcStatusId;
}
