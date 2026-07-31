package fpt.qn.mes.auth.infrastructure.config;

import java.nio.charset.StandardCharsets;
import java.util.List;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import fpt.qn.mes.auth.infrastructure.security.AppJwtAuthenticationConverter;
import fpt.qn.mes.idempotency.application.service.IdempotencyService;
import fpt.qn.mes.idempotency.infrastructure.filter.IdempotencyFilter;

import fpt.qn.mes.auth.application.port.out.TokenBlacklistPort;
import fpt.qn.mes.common.dto.response.ApiResponse;

import fpt.qn.mes.common.exception.ErrorCode;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SecurityConfig {

    @Value("${app.cors.allowed-origins}")
    List<String> allowedOrigins;

    final JwtSecurityProperties jwtProperties;

    public SecurityConfig(JwtSecurityProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http,
            AppJwtAuthenticationConverter jwtConverter,
            IdempotencyService idempotencyService, 
            Environment environment,
            ObjectMapper objectMapper,
            @Qualifier("accessJwtDecoder") JwtDecoder accessJwtDecoder) throws Exception {
        IdempotencyFilter idempotencyFilter = new IdempotencyFilter(idempotencyService, objectMapper);

        return http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth -> {
                    auth.requestMatchers(
                            "/api/auth/login",
                            "/api/auth/refresh",
                            "/actuator/health/**",
                            "/v3/api-docs/**",
                            "/swagger-ui/**",
                            "/swagger-ui.html")
                            .permitAll();
                    if (environment.acceptsProfiles(Profiles.of("dev"))) {
                        auth.requestMatchers(
                                "/api/stock**",
                                "/api/stock**/**",
                                "/api/quality-inspections/**")
                                .permitAll();
                    }
                    auth.requestMatchers("/actuator/**").hasRole("ADMIN");
                    auth.requestMatchers("/api/**").authenticated();
                    auth.anyRequest().denyAll();
                })
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .decoder(accessJwtDecoder)
                                .jwtAuthenticationConverter(jwtConverter)
                        )
                        .authenticationEntryPoint((req, res, ex) -> writeError(res, HttpStatus.UNAUTHORIZED, ErrorCode.UNAUTHORIZED, "Authentication required", objectMapper))
                        .accessDeniedHandler((req, res, ex) -> writeError(res, HttpStatus.FORBIDDEN, ErrorCode.FORBIDDEN, "Access denied", objectMapper))
                )
                .addFilterAfter(idempotencyFilter, BearerTokenAuthenticationFilter.class)
                .build();
    }

    private void writeError(HttpServletResponse response, HttpStatus status, ErrorCode errorCode, String message, ObjectMapper objectMapper) throws java.io.IOException {
        if (response.isCommitted()) return;
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setStatus(status.value());
        objectMapper.writeValue(response.getOutputStream(), ApiResponse.error(errorCode, message));
    }


    @Bean
    JwtDecoder tokenDecoder() {
        NimbusJwtDecoder decoder = buildDecoder();
        decoder.setJwtValidator(baseValidator());
        return decoder;
    }

    @Bean
    JwtDecoder accessJwtDecoder(TokenBlacklistPort tokenBlacklistPort) {


        NimbusJwtDecoder decoder = buildDecoder();
        OAuth2TokenValidator<Jwt> typeValidator = jwt -> "access".equals(jwt.getClaimAsString("token_type"))
                ? OAuth2TokenValidatorResult.success()
                : OAuth2TokenValidatorResult.failure(
                        new OAuth2Error("invalid_token", "Access token required", null));
        OAuth2TokenValidator<Jwt> blacklistValidator = jwt -> (jwt.getId() != null && tokenBlacklistPort.isBlacklisted(jwt.getId()))
                ? OAuth2TokenValidatorResult.failure(
                        new OAuth2Error("invalid_token", "Token has been revoked", null))
                : OAuth2TokenValidatorResult.success();
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(baseValidator(), typeValidator, blacklistValidator));
        return decoder;
    }

    private NimbusJwtDecoder buildDecoder() {
        SecretKeySpec key = new SecretKeySpec(
                jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        return NimbusJwtDecoder.withSecretKey(key).build();
    }

    private OAuth2TokenValidator<Jwt> baseValidator() {
        OAuth2TokenValidator<Jwt> audienceValidator =
                jwt -> jwt.getAudience().contains(jwtProperties.getAudience())
                ? OAuth2TokenValidatorResult.success()
                : OAuth2TokenValidatorResult.failure(
                        new OAuth2Error("invalid_token", "Invalid audience", null));
        return new DelegatingOAuth2TokenValidator<>(
                JwtValidators.createDefaultWithIssuer(jwtProperties.getIssuer()),
                audienceValidator);
    }

    @Bean
    JwtEncoder jwtEncoder() {
        SecretKeySpec key = new SecretKeySpec(
                jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        JWKSource<SecurityContext> jwks = new ImmutableSecret<>(key);
        return new NimbusJwtEncoder(jwks);
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(allowedOrigins);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
