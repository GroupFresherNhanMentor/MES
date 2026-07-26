package fpt.qn.mes.master.machine.domain.entities;

import java.time.Instant;
import java.util.UUID;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter @Builder @FieldDefaults(level = AccessLevel.PRIVATE)
public class Machine {
    UUID id; UUID productionLineId; String code; String name; UUID machineStatusId;
    Instant createdAt; UUID createdBy; Instant updatedAt; UUID updatedBy;

    public static Machine create(UUID productionLineId, String code, String name, UUID machineStatusId, UUID createdBy) {
        throw new UnsupportedOperationException("Not implemented");
    }
}
