package fpt.qn.mes.master.location.application.dto.locationstatus.create;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter @Setter @FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateLocationStatusRequest {
    @NotBlank String name;
    String description;
}
