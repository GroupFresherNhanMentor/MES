package fpt.qn.mes.workorder.application.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
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
