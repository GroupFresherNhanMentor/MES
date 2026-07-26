package fpt.qn.mes.auth.infrastructure.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.stereotype.Component;

import fpt.qn.mes.auth.application.port.out.TokenPort;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class JwtTokenProvider implements TokenPort {

    @Value("${app.jwt.access-token-expiration:900000}")
    long accessTokenExpiration;

    @Value("${app.jwt.refresh-token-expiration:604800000}")
    long refreshTokenExpiration;

    final JwtEncoder jwtEncoder;
    final JwtDecoder jwtDecoder;

    @Override
    public String generateAccessToken(String username, String role) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public String generateRefreshToken(String username) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public boolean isRefreshToken(String token) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public String extractUsername(String token) {
        throw new UnsupportedOperationException("Not implemented");
    }
}
