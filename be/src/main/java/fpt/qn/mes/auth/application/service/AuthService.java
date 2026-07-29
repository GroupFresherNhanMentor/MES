package fpt.qn.mes.auth.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fpt.qn.mes.auth.application.dto.request.LoginRequest;
import fpt.qn.mes.auth.application.dto.response.TokenResponse;
import fpt.qn.mes.auth.application.exception.UnauthorizedException;
import fpt.qn.mes.auth.application.port.in.AuthUseCase;
import fpt.qn.mes.auth.application.port.in.ResolveAuthorizationUseCase;
import fpt.qn.mes.auth.application.port.out.CredentialQueryPort;
import fpt.qn.mes.auth.application.port.out.PasswordPort;
import fpt.qn.mes.auth.application.port.out.TokenPort;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthService implements AuthUseCase {

    static final String DUMMY_BCRYPT_HASH =
            "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy";

    TokenPort tokenPort;
    PasswordPort passwordPort;
    CredentialQueryPort credentialQueryPort;
    ResolveAuthorizationUseCase resolveAuthorizationUseCase;

    @Override
    @Transactional(readOnly = true)
    public TokenResponse login(LoginRequest request) {
        var account = credentialQueryPort.findByUsername(request.getUsername()).orElse(null);
        String encodedPassword = account == null ? DUMMY_BCRYPT_HASH : account.getPasswordHash();
        boolean passwordMatches = passwordPort.matches(request.getPassword(), encodedPassword);
        if (account == null || !account.isActive() || !passwordMatches) {
            throw new UnauthorizedException("Invalid username or password");
        }

        var snapshot = resolveAuthorizationUseCase.resolve(account.getId())
                .orElseThrow(() -> new UnauthorizedException("Invalid username or password"));
        String accessToken = tokenPort.generateAccessToken(account.getId(), account.getUsername());
        String refreshToken = tokenPort.generateRefreshToken(account.getId(), account.getUsername());
        return response(accessToken, refreshToken, snapshot);
    }

    @Override
    @Transactional(readOnly = true)
    public TokenResponse refresh(String refreshToken) {
        var claims = tokenPort.parseRefreshToken(refreshToken);
        var snapshot = resolveAuthorizationUseCase.resolve(claims.getUserId())
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));
        String newAccessToken = tokenPort.generateAccessToken(
                snapshot.getUserId(), snapshot.getUsername());
        String newRefreshToken = tokenPort.generateRefreshToken(
                snapshot.getUserId(), snapshot.getUsername());
        return response(newAccessToken, newRefreshToken, snapshot);
    }

    private TokenResponse response(
            String accessToken,
            String refreshToken,
            fpt.qn.mes.auth.application.security.AuthorizationSnapshot snapshot) {
        return TokenResponse.builder()
                .userId(snapshot.getUserId())
                .username(snapshot.getUsername())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }
}
