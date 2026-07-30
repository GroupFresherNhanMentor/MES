package fpt.qn.mes.auth.presentation;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fpt.qn.mes.auth.application.dto.request.LoginRequest;
import fpt.qn.mes.auth.application.dto.request.RefreshRequest;
import fpt.qn.mes.auth.application.dto.response.TokenResponse;
import fpt.qn.mes.auth.application.port.in.AuthUseCase;
import fpt.qn.mes.common.dto.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthController {

    AuthUseCase authUseCase;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<TokenResponse>> login(@RequestBody @Valid LoginRequest request) {
        return ResponseEntity.ok(ApiResponse.success(authUseCase.login(request), "Login successful"));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenResponse>> refresh(@RequestBody @Valid RefreshRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                authUseCase.refresh(request.getRefreshToken()), "Token refreshed successfully"));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @RequestBody(required = false) RefreshRequest refreshRequest) {
        String accessToken = (authHeader != null && authHeader.startsWith("Bearer ")) ? authHeader.substring(7) : null;
        String refreshToken = refreshRequest != null ? refreshRequest.getRefreshToken() : null;
        authUseCase.logout(accessToken, refreshToken);
        return ResponseEntity.ok(ApiResponse.success(null, "Logout successful"));
    }
}


