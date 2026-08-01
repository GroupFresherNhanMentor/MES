package fpt.qn.mes.master.line.application.dto.line.create;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter @Setter @FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateLineRequest {
    @NotBlank String code;
    @NotBlank String name;
}
