package fpt.qn.mes.auth.application.port.in;

import fpt.qn.mes.auth.application.dto.LoginRequest;
import fpt.qn.mes.auth.application.dto.TokenResponse;

public interface AuthUseCase {

    TokenResponse login(LoginRequest request);

    TokenResponse refresh(String refreshToken);
}
