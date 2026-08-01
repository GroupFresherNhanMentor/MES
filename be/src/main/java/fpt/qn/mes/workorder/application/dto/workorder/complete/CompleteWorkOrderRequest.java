package fpt.qn.mes.workorder.application.dto.workorder.complete;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CompleteWorkOrderRequest {
    @NotNull @DecimalMin("0.0") BigDecimal actualQuantity;
    @NotNull @DecimalMin("0.0") BigDecimal goodQuantity;
    @NotNull @DecimalMin("0.0") BigDecimal defectQuantity;
    @NotNull @DecimalMin("0.0") BigDecimal scrapQuantity;
    @NotNull UUID outputWarehouseId;
    @NotNull UUID outputLocationId;
    @Size(max = 500) String note;
}
