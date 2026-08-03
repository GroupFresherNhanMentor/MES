package fpt.qn.mes.workorder.application.dto.request;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateWorkOrderRequest {
    @NotBlank String code;
    @NotNull UUID finishedProductId;
    UUID bomId;
    @NotNull @Positive BigDecimal plannedQuantity;
    @NotNull
    Instant plannedStartDate;
    @NotNull
    Instant plannedEndDate;
    UUID priorityId;
    UUID workOrderStatusId;
}
