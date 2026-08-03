package fpt.qn.mes.workorder.domain.entities;

import java.time.Instant;
import java.util.UUID;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class WorkOrderEvent {
    UUID id;
    UUID workOrderId;
    UUID productionRunId;
    UUID eventTypeId;
    UUID operatorId;
    Instant eventTimestamp;
    String note;

    public static WorkOrderEvent create(UUID workOrderId, UUID productionRunId, UUID eventTypeId, UUID operatorId,
            String note) {
        throw new UnsupportedOperationException("Not implemented");
    }
}
