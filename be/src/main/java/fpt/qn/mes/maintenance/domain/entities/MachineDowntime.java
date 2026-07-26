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
public class MachineDowntime {
    UUID id;
    UUID ticketId;
    UUID machineId;
    Instant startTime;
    Instant endTime;
    Long totalDowntimeMinutes;
    String rootCause;
    String actionTaken;

    public static MachineDowntime create(UUID ticketId, UUID machineId, Instant startTime, Instant endTime,
            Long totalDowntimeMinutes, String rootCause, String actionTaken) {
        throw new UnsupportedOperationException("Not implemented");
    }
}
