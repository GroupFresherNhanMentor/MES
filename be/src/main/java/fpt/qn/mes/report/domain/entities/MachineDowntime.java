package fpt.qn.mes.report.domain.entities;

import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MachineDowntime {
    UUID machineId;
    String machineCode;
    String machineName;
    Long totalDowntimeMinutes;
    Long maintenanceTicketCount;
    String lastDowntimeReason;
}
