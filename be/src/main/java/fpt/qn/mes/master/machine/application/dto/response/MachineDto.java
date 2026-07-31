package fpt.qn.mes.master.machine.application.dto.response;

import java.time.Instant;
import java.util.UUID;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor @FieldDefaults(level = AccessLevel.PRIVATE)
public class MachineDto {
    UUID id; UUID productionLineId; String productionLineName; String code; String name; UUID machineStatusId; String machineStatusName;
    Instant createdAt; UUID createdBy; Instant updatedAt; UUID updatedBy;
}
