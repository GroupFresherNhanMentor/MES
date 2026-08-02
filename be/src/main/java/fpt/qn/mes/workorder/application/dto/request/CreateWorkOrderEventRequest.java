package fpt.qn.mes.workorder.application.dto.request;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class CreateWorkOrderEventRequest {
    @NotNull UUID eventTypeId;
    UUID productionRunId;
    UUID operatorId;
    String note;
}
