package fpt.qn.mes.maintenance.application.dto;

import java.time.Instant;
import java.util.UUID;

import lombok.Builder;
import lombok.Getter;

@Getter @Builder
public class MaintenanceTicketDto {
    UUID id; UUID machineId; UUID ticketTypeId; UUID priorityId;
    String description; UUID ticketStatusId; UUID createdBy; Instant createdAt;
}
