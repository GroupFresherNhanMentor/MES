package fpt.qn.mes.auth.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Validated
@Configuration
@ConfigurationProperties(prefix = "app.jwt")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class JwtSecurityProperties {

    @NotBlank
    @Size(min = 32)
    String secret;

    @NotBlank
    String issuer = "factoryflow";

    @NotBlank
    String audience = "factoryflow-api";

    @Positive
    long accessTokenExpiration = 900_000;

    @Positive
    long refreshTokenExpiration = 604_800_000;
}

