package fpt.qn.mes.maintenance.application.dto;

import java.util.UUID;

import lombok.Getter;

@Getter
public class UpdateMaintenanceTicketRequest {
    String description; UUID ticketStatusId; UUID priorityId;
}
