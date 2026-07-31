package fpt.qn.mes.auth.application.service;

import java.time.Duration;
import java.time.Instant;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fpt.qn.mes.auth.application.dto.request.LoginRequest;
import fpt.qn.mes.auth.application.dto.response.TokenResponse;
import fpt.qn.mes.auth.application.exception.UnauthorizedException;
import fpt.qn.mes.auth.application.port.in.AuthUseCase;
import fpt.qn.mes.auth.application.port.out.CredentialQueryPort;
import fpt.qn.mes.auth.application.port.out.PasswordPort;
import fpt.qn.mes.auth.application.port.out.TokenBlacklistPort;
import fpt.qn.mes.auth.application.port.out.TokenPort;
import fpt.qn.mes.auth.application.security.CredentialAccount;
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
    TokenBlacklistPort tokenBlacklistPort;

    @Override
    @Transactional(readOnly = true)
    public TokenResponse login(LoginRequest request) {
        var account = credentialQueryPort.findByUsername(request.getUsername()).orElse(null);
        String encodedPassword = account == null ? DUMMY_BCRYPT_HASH : account.getPasswordHash();
        boolean passwordMatches = passwordPort.matches(request.getPassword(), encodedPassword);
        if (account == null || !account.isActive() || !passwordMatches) {
            throw new UnauthorizedException("Invalid username or password");
        }
        return buildResponse(account);
    }

    @Override
    @Transactional(readOnly = true)
    public TokenResponse refresh(String refreshToken) {
        var claims = tokenPort.parseRefreshToken(refreshToken);
        if (claims.getTokenId() != null && tokenBlacklistPort.isBlacklisted(claims.getTokenId().toString())) {
            throw new UnauthorizedException("Invalid refresh token");
        }
        var account = credentialQueryPort.findById(claims.getUserId())
                .filter(CredentialAccount::isActive)
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));
        return buildResponse(account);
    }

    @Override
    public void logout(String accessToken, String refreshToken) {
        Instant now = Instant.now();
        if (accessToken != null && !accessToken.isBlank()) {
            try {
                var accessClaims = tokenPort.parseAccessToken(accessToken);
                if (accessClaims.getTokenId() != null && accessClaims.getExpiresAt() != null) {
                    Duration ttl = Duration.between(now, accessClaims.getExpiresAt());
                    if (!ttl.isNegative() && !ttl.isZero()) {
                        tokenBlacklistPort.blacklistToken(accessClaims.getTokenId().toString(), ttl);
                    }
                }
            } catch (Exception ignored) {
                // Token invalid or already expired
            }
        }
        if (refreshToken != null && !refreshToken.isBlank()) {
            try {
                var refreshClaims = tokenPort.parseRefreshToken(refreshToken);
                if (refreshClaims.getTokenId() != null && refreshClaims.getExpiresAt() != null) {
                    Duration ttl = Duration.between(now, refreshClaims.getExpiresAt());
                    if (!ttl.isNegative() && !ttl.isZero()) {
                        tokenBlacklistPort.blacklistToken(refreshClaims.getTokenId().toString(), ttl);
                    }
                }
            } catch (Exception ignored) {
                // Token invalid or already expired
            }
        }
    }

    private TokenResponse buildResponse(CredentialAccount account) {
        String accessToken = tokenPort.generateAccessToken(account.getId(), account.getUsername(), account.getRoles());
        String newRefreshToken = tokenPort.generateRefreshToken(account.getId(), account.getUsername());

        return TokenResponse.builder()
                .userId(account.getId())
                .username(account.getUsername())
                .accessToken(accessToken)
                .refreshToken(newRefreshToken)
                .build();
    }
}


