package fpt.qn.mes.quality.application.dto.qcstatus.create;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateQcStatusRequest {
    @NotBlank String name;
    String description;
}
