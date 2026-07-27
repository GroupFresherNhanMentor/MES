package fpt.qn.mes.workorder.application.dto.request;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import lombok.Getter;

@Getter
public class UpdateWorkOrderRequest {
    String code;
    BigDecimal plannedQuantity;
    Instant plannedStartDate;
    Instant plannedEndDate;
    UUID priorityId;
    UUID workOrderStatusId;
}
