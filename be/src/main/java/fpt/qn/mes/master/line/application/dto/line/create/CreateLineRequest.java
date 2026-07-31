package fpt.qn.mes.master.line.application.dto.line.create;

import java.util.UUID;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter @Setter @FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateLineRequest {
    @NotBlank String code;
    @NotBlank String name;
    @NotNull UUID lineStatusId;
}
