package fpt.qn.mes.auth.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;

import javax.crypto.spec.SecretKeySpec;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.nimbusds.jose.jwk.source.ImmutableSecret;

import fpt.qn.mes.auth.application.exception.InvalidTokenException;
import fpt.qn.mes.auth.infrastructure.config.JwtSecurityProperties;

class JwtTokenProviderTest {

    static final Instant NOW = Instant.now().truncatedTo(java.time.temporal.ChronoUnit.SECONDS);

    JwtTokenProvider provider;
    NimbusJwtDecoder decoder;

    @BeforeEach
    void setUp() {
        byte[] secret = "test-secret-key-must-be-at-least-32-chars-long-for-hmac256"
                .getBytes(StandardCharsets.UTF_8);
        SecretKeySpec key = new SecretKeySpec(secret, "HmacSHA256");
        decoder = NimbusJwtDecoder.withSecretKey(key).build();
        JwtSecurityProperties properties = new JwtSecurityProperties();
        properties.setSecret(new String(secret, StandardCharsets.UTF_8));
        properties.setIssuer("factoryflow-test");
        properties.setAudience("factoryflow-api-test");
        properties.setAccessTokenExpiration(900_000L);
        properties.setRefreshTokenExpiration(604_800_000L);
        provider = new JwtTokenProvider(
                new NimbusJwtEncoder(new ImmutableSecret<>(key)),
                decoder,
                Clock.fixed(NOW, ZoneOffset.UTC),
                properties);
    }

    @Test
    void issuesPurposeSpecificHs256TokensWithRequiredClaimsAndLifetimes() {
        UUID userId = UUID.randomUUID();

        String access = provider.generateAccessToken(userId, "alice", java.util.List.of("ADMIN"));
        String refresh = provider.generateRefreshToken(userId, "alice");

        var accessJwt = decoder.decode(access);
        var refreshJwt = decoder.decode(refresh);
        assertThat(accessJwt.getHeaders().get("alg")).isEqualTo("HS256");
        assertThat(accessJwt.getClaimAsString("iss")).isEqualTo("factoryflow-test");
        assertThat(accessJwt.getAudience()).containsExactly("factoryflow-api-test");
        assertThat(accessJwt.getSubject()).isEqualTo(userId.toString());
        assertThat(accessJwt.getId()).isNotBlank();
        assertThat(accessJwt.hasClaim("sid")).isFalse();
        assertThat(accessJwt.getClaimAsString("token_type")).isEqualTo("access");
        assertThat(accessJwt.getClaimAsStringList("roles")).containsExactly("ADMIN");
        assertThat(accessJwt.getExpiresAt()).isEqualTo(NOW.plusSeconds(900));
        assertThat(refreshJwt.getId()).isNotBlank().isNotEqualTo(accessJwt.getId());
        assertThat(refreshJwt.getClaimAsString("token_type")).isEqualTo("refresh");
        assertThat(refreshJwt.getExpiresAt()).isEqualTo(NOW.plusSeconds(604_800));
        assertThat(provider.parseRefreshToken(refresh).getTokenId())
                .isEqualTo(UUID.fromString(refreshJwt.getId()));
    }

    @Test
    void accessTokenCannotBeUsedAsRefreshToken() {
        String access = provider.generateAccessToken(UUID.randomUUID(), "alice", java.util.List.of());
        assertThatThrownBy(() -> provider.parseRefreshToken(access))
                .isInstanceOf(InvalidTokenException.class);
    }


    @Test
    void remainsCompatibleWithExistingBcryptHashes() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(10);
        String legacy2aHash = encoder.encode("legacy-password");
        assertThat(legacy2aHash).startsWith("$2a$");
        assertThat(new BCryptPasswordEncoder().matches(
                "legacy-password", legacy2aHash)).isTrue();
    }
}
