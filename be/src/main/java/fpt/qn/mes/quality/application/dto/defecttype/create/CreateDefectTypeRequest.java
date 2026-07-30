package fpt.qn.mes.quality.application.dto.defecttype.create;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateDefectTypeRequest {
    @NotBlank String name;
    String description;
}
