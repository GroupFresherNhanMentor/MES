package fpt.qn.mes.maintenance.application.dto.response;

import java.time.Instant;
import java.util.UUID;

import lombok.Builder;
import lombok.Getter;

@Getter @Builder
public class MaintenanceTicketResponse {
    UUID id; UUID machineId; UUID ticketTypeId; UUID priorityId;
    String description; UUID ticketStatusId; UUID createdBy; Instant createdAt;
}
