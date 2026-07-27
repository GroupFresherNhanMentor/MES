package fpt.qn.mes.master.machine.application.dto.request;

import java.util.UUID;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter @Setter @FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateMachineRequest {
    UUID productionLineId; String name; UUID machineStatusId;
}
