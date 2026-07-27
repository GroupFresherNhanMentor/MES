package fpt.qn.mes.master.machine.application.dto.request;

import java.util.UUID;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter @Setter @FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateMachineRequest {
    UUID productionLineId; @NotBlank String code; @NotBlank String name; @NotNull UUID machineStatusId;
}
