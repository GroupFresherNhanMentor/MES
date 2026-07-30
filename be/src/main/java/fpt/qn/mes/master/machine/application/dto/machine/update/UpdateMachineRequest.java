package fpt.qn.mes.master.machine.application.dto.machine.update;

import java.util.UUID;
import lombok.*;
import lombok.experimental.FieldDefaults;

import jakarta.validation.constraints.NotBlank;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateMachineRequest {
    
    UUID productionLineId;
    
    @NotBlank(message = "Name must not be blank")
    String name;
}
