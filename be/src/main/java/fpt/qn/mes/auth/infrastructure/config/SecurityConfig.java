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
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;

import fpt.qn.mes.auth.infrastructure.security.AppJwtAuthenticationConverter;
import fpt.qn.mes.auth.infrastructure.security.JwtAccessDeniedHandler;
import fpt.qn.mes.auth.infrastructure.security.JwtAuthenticationEntryPoint;
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
    public SecurityFilterChain filterChain(HttpSecurity http,
            AppJwtAuthenticationConverter jwtConverter,
            JwtAuthenticationEntryPoint entryPoint,
            JwtAccessDeniedHandler accessDeniedHandler,
            Environment environment,
            @Qualifier("accessJwtDecoder") JwtDecoder accessJwtDecoder) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> {
                    auth.requestMatchers("/api/auth/login", "/api/auth/refresh", "/actuator/health/**")
                            .permitAll();
                    if (environment.acceptsProfiles(Profiles.of("dev", "test"))) {
                        auth.requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html")
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
                        .authenticationEntryPoint(entryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                )
                .build();
    }

    @Bean
    public JwtDecoder tokenDecoder() {
        NimbusJwtDecoder decoder = buildDecoder();
        decoder.setJwtValidator(baseValidator());
        return decoder;
    }

    @Bean
    public JwtDecoder accessJwtDecoder() {
        NimbusJwtDecoder decoder = buildDecoder();
        OAuth2TokenValidator<Jwt> typeValidator = jwt -> "access".equals(jwt.getClaimAsString("token_type"))
                ? OAuth2TokenValidatorResult.success()
                : OAuth2TokenValidatorResult.failure(
                        new OAuth2Error("invalid_token", "Access token required", null));
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(baseValidator(), typeValidator));
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
    public JwtEncoder jwtEncoder() {
        SecretKeySpec key = new SecretKeySpec(
                jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        JWKSource<SecurityContext> jwks = new ImmutableSecret<>(key);
        return new NimbusJwtEncoder(jwks);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
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
