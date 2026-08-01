package fpt.qn.mes.maintenance.application.dto.request;

import lombok.Getter;
import lombok.Setter;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
public class MaintenanceTicketSearchQuery {
    UUID machineId;
    UUID ticketStatusId;
    UUID priorityId;
    UUID ticketTypeId;
    String description;
    Instant fromDate;
    Instant toDate;
}