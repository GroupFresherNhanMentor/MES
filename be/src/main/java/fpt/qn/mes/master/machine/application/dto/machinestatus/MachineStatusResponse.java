package fpt.qn.mes.master.machine.application.dto.machinestatus;

import java.util.UUID;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MachineStatusResponse {
    UUID id;
    String name;
    String description;
}
