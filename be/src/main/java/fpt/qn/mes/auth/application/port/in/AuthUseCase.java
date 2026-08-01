package fpt.qn.mes.auth.application.port.in;

import fpt.qn.mes.auth.application.dto.request.LoginRequest;
import fpt.qn.mes.auth.application.dto.response.TokenResponse;

public interface AuthUseCase {

    TokenResponse login(LoginRequest request);

    TokenResponse refresh(String refreshToken);

    void logout(String accessToken, String refreshToken);
}
