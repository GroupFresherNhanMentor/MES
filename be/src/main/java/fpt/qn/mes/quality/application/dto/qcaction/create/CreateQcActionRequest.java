package fpt.qn.mes.quality.application.dto.qcaction.create;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateQcActionRequest {
    @NotBlank String name;
    String description;
}
