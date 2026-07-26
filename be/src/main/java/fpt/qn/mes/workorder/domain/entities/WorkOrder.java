package fpt.qn.mes.workorder.domain.entities;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class WorkOrder {
    UUID id;
    String code;
    UUID finishedProductId;
    UUID bomId;
    BigDecimal plannedQuantity;
    Instant plannedStartDate;
    Instant plannedEndDate;
    UUID priorityId;
    UUID workOrderStatusId;
    UUID createdBy;
    Instant createdAt;

    public static WorkOrder create(String code, UUID finishedProductId, UUID bomId, BigDecimal plannedQuantity,
            Instant plannedStartDate, Instant plannedEndDate, UUID priorityId, UUID workOrderStatusId, UUID createdBy) {
        throw new UnsupportedOperationException("Not implemented");
    }
}
