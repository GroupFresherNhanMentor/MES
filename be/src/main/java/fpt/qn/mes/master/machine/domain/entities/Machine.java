package fpt.qn.mes.master.machine.domain.entities;

import java.time.Instant;
import java.util.UUID;

import fpt.qn.mes.common.util.UuidV7;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Machine {

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductionLineRef {
        private UUID id;
        private String code;
        private String name;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserRef {
        private UUID id;
        private String fullName;
        private String username;
    }

    UUID id;
    ProductionLineRef productionLine;
    String code;
    String name;
    MachineStatus machineStatus;
    Instant createdAt;
    UserRef createdBy;
    Instant updatedAt;
    UserRef updatedBy;

    public static Machine create(UUID productionLineId, String code, String name, UUID machineStatusId, UUID createdBy) {
        return Machine.builder()
                .id(UuidV7.generate())
                .productionLine(ProductionLineRef.builder().id(productionLineId).build())
                .code(code)
                .name(name)
                .machineStatus(MachineStatus.builder().id(machineStatusId).build())
                .createdBy(UserRef.builder().id(createdBy).build())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    public static Machine update(Machine existing, String name, UUID updatedBy) {
        return Machine.builder()
                .id(existing.getId())
                .productionLine(existing.getProductionLine())
                .code(existing.getCode())
                .name(name != null ? name : existing.getName())
                .machineStatus(existing.getMachineStatus())
                .createdAt(existing.getCreatedAt())
                .createdBy(existing.getCreatedBy())
                .updatedAt(Instant.now())
                .updatedBy(UserRef.builder().id(updatedBy).build())
                .build();
    }

    public static Machine changeStatus(Machine existing, UUID newStatusId, UUID updatedBy) {
        return Machine.builder()
                .id(existing.getId())
                .productionLine(existing.getProductionLine())
                .code(existing.getCode())
                .name(existing.getName())
                .machineStatus(MachineStatus.builder().id(newStatusId).build())
                .createdAt(existing.getCreatedAt())
                .createdBy(existing.getCreatedBy())
                .updatedAt(Instant.now())
                .updatedBy(UserRef.builder().id(updatedBy).build())
                .build();
    }
}
