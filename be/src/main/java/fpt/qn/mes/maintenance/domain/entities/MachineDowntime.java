package fpt.qn.mes.maintenance.domain.entities;

import java.time.Instant;
import java.util.UUID;

import fpt.qn.mes.common.exception.DomainException;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MachineDowntime {
    UUID id;
    UUID ticketId;
    UUID machineId;
    Instant startTime;
    Instant endTime;
    Long totalDowntimeMinutes;
    String rootCause;
    String actionTaken;

    public static MachineDowntime create(UUID ticketId, UUID machineId, Instant startTime) {
        return MachineDowntime.builder()
                .id(UUID.randomUUID())
                .ticketId(ticketId)
                .machineId(machineId)
                .startTime(startTime)
                .endTime(null)
                .totalDowntimeMinutes(null)
                .rootCause(null)
                .actionTaken(null)
                .build();
    }

    public void endDowntime(Instant endTime, String rootCause, String actionTaken) {
        if (this.startTime == null) {
            throw new DomainException("Downtime cannot be ended because start time is missing.");
        }
        if (endTime.isBefore(this.startTime)) {
            throw new DomainException("End time cannot be before start time.");
        }
        if (actionTaken == null || actionTaken.isBlank()) {
            throw new DomainException("Action taken is required to close the ticket.");
        }

        this.endTime = endTime;
        this.rootCause = rootCause;
        this.actionTaken = actionTaken;

        // Tính toán số phút chênh lệch giữa startTime và endTime
        this.totalDowntimeMinutes = java.time.Duration.between(this.startTime, endTime).toMinutes();
    }
}
