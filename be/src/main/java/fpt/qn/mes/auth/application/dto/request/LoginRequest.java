package fpt.qn.mes.auth.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LoginRequest {

    @NotBlank
    @Schema(
        description = "Tên đăng nhập của người dùng", 
        example = "admin", 
        defaultValue = "admin"
    )
    String username;

    @NotBlank
    @Schema(
        description = "Mật khẩu người dùng", 
        example = "Admin@1234", 
        defaultValue = "Admin@1234"
    )
    String password;
}