package fpt.qn.mes.master.machine.application.dto;

import java.time.Instant;
import java.util.UUID;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter @Builder @NoArgsConstructor @AllArgsConstructor @FieldDefaults(level = AccessLevel.PRIVATE)
public class MachineDto {
    UUID id; UUID productionLineId; String code; String name; UUID machineStatusId;
    Instant createdAt; UUID createdBy; Instant updatedAt; UUID updatedBy;
}
