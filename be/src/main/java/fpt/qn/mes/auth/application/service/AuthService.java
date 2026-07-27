package fpt.qn.mes.auth.application.service;

import org.springframework.stereotype.Service;

import fpt.qn.mes.auth.application.dto.request.LoginRequest;
import fpt.qn.mes.auth.application.dto.response.TokenResponse;
import fpt.qn.mes.auth.application.port.in.AuthUseCase;
import fpt.qn.mes.auth.application.port.out.PasswordPort;
import fpt.qn.mes.auth.application.port.out.TokenPort;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthService implements AuthUseCase {

    TokenPort tokenPort;
    PasswordPort passwordPort;

    @Override
    public TokenResponse login(LoginRequest request) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public TokenResponse refresh(String refreshToken) {
        throw new UnsupportedOperationException("Not implemented");
    }
}
