package fpt.qn.mes.auth.application.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import fpt.qn.mes.auth.application.dto.LoginRequest;
import fpt.qn.mes.auth.application.dto.TokenResponse;
import fpt.qn.mes.auth.application.port.in.AuthUseCase;
import fpt.qn.mes.auth.application.port.out.TokenPort;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthService implements AuthUseCase {

    AuthenticationManager authenticationManager;
    UserDetailsService userDetailsService;
    TokenPort tokenPort;

    @Override
    public TokenResponse login(LoginRequest request) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public TokenResponse refresh(String refreshToken) {
        throw new UnsupportedOperationException("Not implemented");
    }
}
