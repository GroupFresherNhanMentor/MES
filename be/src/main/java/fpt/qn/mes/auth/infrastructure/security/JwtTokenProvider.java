package fpt.qn.mes.auth.infrastructure.security;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.stereotype.Component;

import fpt.qn.mes.auth.application.exception.InvalidTokenException;
import fpt.qn.mes.auth.application.port.out.TokenPort;
import fpt.qn.mes.auth.application.security.TokenClaims;
import fpt.qn.mes.auth.infrastructure.config.JwtSecurityProperties;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE)
public class JwtTokenProvider implements TokenPort {

    final JwtEncoder jwtEncoder;
    final JwtDecoder jwtDecoder;
    final Clock clock;
    final JwtSecurityProperties properties;

    public JwtTokenProvider(
            JwtEncoder jwtEncoder,
            @Qualifier("tokenDecoder") JwtDecoder jwtDecoder,
            Clock clock,
            JwtSecurityProperties properties) {
        this.jwtEncoder = jwtEncoder;
        this.jwtDecoder = jwtDecoder;
        this.clock = clock;
        this.properties = properties;
    }

    @Override
    public String generateAccessToken(UUID userId, String username) {
        Instant now = clock.instant();
        JwtClaimsSet claims = JwtClaimsSet.builder()
            .issuer(properties.getIssuer())
            .audience(java.util.List.of(properties.getAudience()))
            .subject(userId.toString())
            .issuedAt(now)
            .expiresAt(now.plusMillis(properties.getAccessTokenExpiration()))
            .id(UUID.randomUUID().toString())
            .claim("username", username)
            .claim("token_type", "access")
            .build();
        return encode(claims);
    }

    @Override
    public String generateRefreshToken(UUID userId, String username) {
        Instant now = clock.instant();
        JwtClaimsSet claims = JwtClaimsSet.builder()
            .issuer(properties.getIssuer())
            .audience(java.util.List.of(properties.getAudience()))
            .subject(userId.toString())
            .issuedAt(now)
            .expiresAt(now.plusMillis(properties.getRefreshTokenExpiration()))
            .id(UUID.randomUUID().toString())
            .claim("username", username)
            .claim("token_type", "refresh")
            .build();
        return encode(claims);
    }

    @Override
    public TokenClaims parseRefreshToken(String token) {
        try {
            var jwt = jwtDecoder.decode(token);
            String tokenType = jwt.getClaimAsString("token_type");
            if (!"refresh".equals(tokenType)) {
                throw new InvalidTokenException("Invalid refresh token");
            }
            return TokenClaims.builder()
                    .userId(UUID.fromString(jwt.getSubject()))
                    .username(jwt.getClaimAsString("username"))
                    .tokenId(UUID.fromString(jwt.getId()))
                    .tokenType(tokenType)
                    .issuedAt(jwt.getIssuedAt())
                    .expiresAt(jwt.getExpiresAt())
                    .build();
        } catch (JwtException | IllegalArgumentException | NullPointerException ex) {
            throw new InvalidTokenException("Invalid refresh token");
        }
    }

    private String encode(JwtClaimsSet claims) {
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }
}
