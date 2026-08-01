package fpt.qn.mes.auth.application.port.out;

import java.util.List;

import java.util.UUID;

import fpt.qn.mes.auth.application.security.TokenClaims;

public interface TokenPort {

    String generateAccessToken(UUID userId, String username, List<String> roles);

    String generateRefreshToken(UUID userId, String username);


    TokenClaims parseRefreshToken(String token);

    TokenClaims parseAccessToken(String token);
}
