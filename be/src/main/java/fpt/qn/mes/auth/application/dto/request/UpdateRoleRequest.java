package fpt.qn.mes.auth.application.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateRoleRequest {
    @Size(min = 1, max = 50)
    String name;

    @Size(max = 255)
    String description;
}
