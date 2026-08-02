package fpt.qn.mes.bom.application.dto.bomstatus.create;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateBomStatusRequest {
    @NotBlank
    String name;
    String description;
}
