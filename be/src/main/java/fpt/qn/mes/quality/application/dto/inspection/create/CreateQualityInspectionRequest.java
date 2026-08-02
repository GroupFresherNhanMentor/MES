package fpt.qn.mes.quality.application.dto.inspection.create;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateQualityInspectionRequest {
    @NotNull UUID workOrderId;
    @NotNull UUID productId;
    @NotNull UUID lotId;
    @NotNull @DecimalMin("0.0") BigDecimal quantity;
    @NotNull UUID qcStatusId;
}
