package fpt.qn.mes.maintenance.application.dto;

import java.time.Instant;
import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class CreateDowntimeRequest {
    @NotNull UUID machineId;
    Instant startTime; Instant endTime; Long totalDowntimeMinutes; String rootCause; String actionTaken;
}
