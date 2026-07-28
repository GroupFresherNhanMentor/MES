package fpt.qn.mes.quality.application.dto.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PassQcRequest {
    @NotNull
    @DecimalMin(value = "0.0001", message = "passedQuantity must be > 0")
    BigDecimal passedQuantity;

    String note;
}
