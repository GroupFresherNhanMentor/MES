package fpt.qn.mes.maintenance.domain.entities;

import java.time.Instant;
import java.util.UUID;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MaintenanceTicket {
    UUID id;
    UUID machineId;
    UUID ticketTypeId;
    UUID priorityId;
    String description;
    UUID ticketStatusId;
    UUID createdBy;
    Instant createdAt;

    public static MaintenanceTicket create(UUID machineId, UUID ticketTypeId, UUID priorityId, String description,
            UUID ticketStatusId, UUID createdBy) {
        throw new UnsupportedOperationException("Not implemented");
    }
}
