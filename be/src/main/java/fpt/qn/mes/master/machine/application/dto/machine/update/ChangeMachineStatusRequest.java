package fpt.qn.mes.master.machine.application.dto.machine.update;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter @Setter @FieldDefaults(level = AccessLevel.PRIVATE)
public class ChangeMachineStatusRequest {
    @NotNull UUID statusId;
}
