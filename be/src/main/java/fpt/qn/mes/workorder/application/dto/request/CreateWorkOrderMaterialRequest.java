package fpt.qn.mes.workorder.application.dto.request;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class CreateWorkOrderMaterialRequest {
    @NotNull UUID materialProductId;
    @NotNull BigDecimal requiredQuantity;
}
