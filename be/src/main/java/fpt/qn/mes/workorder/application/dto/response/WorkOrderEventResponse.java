package fpt.qn.mes.workorder.application.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class WorkOrderEventResponse {
    UUID id;
    UUID workOrderId;
    UUID eventTypeId;
    UUID machineId;
    UUID productionLineId;
    UUID operatorId;
    BigDecimal actualQuantity;
    BigDecimal goodQuantity;
    BigDecimal defectQuantity;
    BigDecimal scrapQuantity;
    Instant eventTimestamp;
    String note;
}
