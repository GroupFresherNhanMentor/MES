package fpt.qn.mes.user.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateUserRequest {

    @NotBlank
    @Size(min = 3, max = 100)
    String username;

    @NotBlank
    @Size(min = 8, max = 255)
    String password;

    @Size(max = 255)
    String fullName;
}
