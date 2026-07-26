package fpt.qn.mes.maintenance.application.dto;

import java.time.Instant;
import java.util.UUID;

import lombok.Builder;
import lombok.Getter;

@Getter @Builder
public class MachineDowntimeDto {
    UUID id; UUID ticketId; UUID machineId;
    Instant startTime; Instant endTime; Long totalDowntimeMinutes; String rootCause; String actionTaken;
}
