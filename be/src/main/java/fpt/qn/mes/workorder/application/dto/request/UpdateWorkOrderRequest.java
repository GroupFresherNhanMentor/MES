package fpt.qn.mes.workorder.application.dto.request;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateWorkOrderRequest {
    String code;
    BigDecimal plannedQuantity;
    Instant plannedStartDate;
    Instant plannedEndDate;
    UUID priorityId;
    UUID workOrderStatusId;
}
