package fpt.qn.mes.auth.application.security;

import java.time.Instant;
import java.util.UUID;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@Builder
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TokenClaims {

    UUID userId;
    String username;
    UUID tokenId;
    String tokenType;
    Instant issuedAt;
    Instant expiresAt;
}
