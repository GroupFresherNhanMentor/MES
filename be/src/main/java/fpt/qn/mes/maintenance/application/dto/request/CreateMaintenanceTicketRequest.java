package fpt.qn.mes.maintenance.application.dto.request;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateMaintenanceTicketRequest {

    @NotNull(message = "Machine ID không được để trống")
    UUID machineId;

    @NotNull(message = "Ticket Type ID không được để trống")
    UUID ticketTypeId;

    @NotNull(message = "Priority ID không được để trống")
    UUID priorityId;

    String description;
}