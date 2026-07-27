package fpt.qn.mes.auth.infrastructure.security;

import java.time.Instant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
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
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
            .subject(username)
            .issuedAt(now)
            .expiresAt(now.plusMillis(accessTokenExpiration))
            .claim("role", role)
            .claim("tokenType", "access")
            .build();
        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

    @Override
    public String generateRefreshToken(String username) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
            .subject(username)
            .issuedAt(now)
            .expiresAt(now.plusMillis(refreshTokenExpiration))
            .claim("tokenType", "refresh")
            .build();
        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

    @Override
    public boolean isRefreshToken(String token) {
        return "refresh".equals(jwtDecoder.decode(token).getClaimAsString("tokenType"));
    }

    @Override
    public String extractUsername(String token) {
        return jwtDecoder.decode(token).getSubject();
    }
}
