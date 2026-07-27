package fpt.qn.mes.maintenance.application.dto.request;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class CreateMaintenanceTicketRequest {
    @NotNull UUID machineId; UUID ticketTypeId; UUID priorityId;
    String description; UUID ticketStatusId;
}
