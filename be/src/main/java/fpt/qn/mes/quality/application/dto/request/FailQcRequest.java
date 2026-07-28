package fpt.qn.mes.quality.application.dto.request;

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
public class FailQcRequest {
    @NotNull
    @DecimalMin(value = "0.0001", message = "failedQuantity must be > 0")
    BigDecimal failedQuantity;

    @NotNull
    UUID actionId;

    @NotNull
    UUID defectTypeId;

    @NotNull
    String reason;

    String note;
}
