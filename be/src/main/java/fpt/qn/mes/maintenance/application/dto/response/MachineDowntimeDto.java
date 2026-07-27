package fpt.qn.mes.maintenance.application.dto.response;

import java.time.Instant;
import java.util.UUID;

import lombok.Builder;
import lombok.Getter;

@Getter @Builder
public class MachineDowntimeDto {
    UUID id; UUID ticketId; UUID machineId;
    Instant startTime; Instant endTime; Long totalDowntimeMinutes; String rootCause; String actionTaken;
}
