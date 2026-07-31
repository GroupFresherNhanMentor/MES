package fpt.qn.mes.master.machine.application.dto.machine;

import java.time.Instant;
import java.util.UUID;

import fpt.qn.mes.master.machine.application.dto.machinestatus.MachineStatusResponse;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MachineResponse {

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductionLineInfo {
        private UUID id;
        private String code;
        private String name;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfo {
        private UUID id;
        private String fullName;
        private String username;
    }

    UUID id;
    ProductionLineInfo productionLine;
    String code;
    String name;
    MachineStatusResponse machineStatus;
    Instant createdAt;
    UserInfo createdBy;
    Instant updatedAt;
    UserInfo updatedBy;
}
