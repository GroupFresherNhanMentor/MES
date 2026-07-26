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
public class WorkOrderEvent {
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

    public static WorkOrderEvent create(UUID workOrderId, UUID eventTypeId, UUID machineId, UUID productionLineId,
            UUID operatorId, BigDecimal actualQuantity, BigDecimal goodQuantity, BigDecimal defectQuantity,
            BigDecimal scrapQuantity, String note) {
        throw new UnsupportedOperationException("Not implemented");
    }
}
