package fpt.qn.mes.workorder.application.dto.request;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class CreateWorkOrderEventRequest {
    @NotNull UUID eventTypeId;
    UUID machineId;
    UUID productionLineId;
    UUID operatorId;
    BigDecimal actualQuantity;
    BigDecimal goodQuantity;
    BigDecimal defectQuantity;
    BigDecimal scrapQuantity;
    String note;
}
