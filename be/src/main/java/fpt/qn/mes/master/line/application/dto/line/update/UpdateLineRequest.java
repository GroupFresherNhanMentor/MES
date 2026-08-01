package fpt.qn.mes.master.line.application.dto.line.update;

import java.util.UUID;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter @Setter @FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateLineRequest {
    @NotBlank String name;
    UUID lineStatusId;
}
