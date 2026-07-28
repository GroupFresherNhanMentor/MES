package fpt.qn.mes.maintenance.domain.entities;

import java.time.Instant;
import java.util.UUID;

import fpt.qn.mes.maintenance.application.exception.BusinessException;
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
    UUID assignedEngineerId;
    UUID createdBy;
    Instant createdAt;

    public static MaintenanceTicket create(
            UUID machineId,
            UUID ticketTypeId,
            UUID priorityId,
            String description,
            UUID statusId,
            UUID createdBy) {

        return MaintenanceTicket.builder()
                .id(UUID.randomUUID())
                .machineId(machineId)
                .ticketTypeId(ticketTypeId)
                .priorityId(priorityId)
                .description(description)
                .ticketStatusId(statusId)
                .assignedEngineerId(null)
                .createdBy(createdBy)
                .createdAt(Instant.now())
                .build();
    }

    public void cancel(UUID openStatusId, UUID cancelledStatusId) {
        if (!this.ticketStatusId.equals(openStatusId)) {
            throw new BusinessException("Chỉ được phép hủy các ticket đang ở trạng thái OPEN.");
        }
        this.ticketStatusId = cancelledStatusId;
    }
}