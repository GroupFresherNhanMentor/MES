package fpt.qn.mes.workorder.application.dto.request;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateWorkOrderRequest {
    @NotBlank String code;
    @NotNull UUID finishedProductId;
    UUID bomId;
    @NotNull BigDecimal plannedQuantity;
    Instant plannedStartDate;
    Instant plannedEndDate;
    UUID priorityId;
    UUID workOrderStatusId;
}
