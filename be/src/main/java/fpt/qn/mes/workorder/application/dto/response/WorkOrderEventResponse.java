package fpt.qn.mes.workorder.application.dto.response;

import java.time.Instant;
import java.util.UUID;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class WorkOrderEventResponse {
    UUID id;
    UUID workOrderId;
    UUID productionRunId;
    UUID eventTypeId;
    UUID operatorId;
    Instant eventTimestamp;
    String note;
}
