package fpt.qn.mes.master.line.application.dto.linestatus.create;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter @Setter @FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateLineStatusRequest {
    @NotBlank String name;
    String description;
}
