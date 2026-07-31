package fpt.qn.mes.auth.infrastructure.security;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import fpt.qn.mes.auth.application.security.AppUserPrincipal;

@Component
public class AppJwtAuthenticationConverter
        implements Converter<Jwt, UsernamePasswordAuthenticationToken> {

    @Override
    public UsernamePasswordAuthenticationToken convert(Jwt jwt) {
        UUID userId;
        try {
            userId = UUID.fromString(jwt.getSubject());
        } catch (RuntimeException ex) {
            throw invalidToken();
        }

        String tokenType = jwt.getClaimAsString("token_type");
        if (!"access".equals(tokenType)) {
            throw invalidToken();
        }

        String username = jwt.getClaimAsString("username");
        List<String> roles = jwt.getClaimAsStringList("roles");
        if (roles == null) {
            roles = List.of();
        }
        AppUserPrincipal principal = AppUserPrincipal.builder()
                .id(userId)
                .username(username != null ? username : "")
                .enabled(true)
                .roles(roles)
                .build();

        var authorities = new ArrayList<SimpleGrantedAuthority>();
        for (String role : roles) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
        }

        return new UsernamePasswordAuthenticationToken(principal, jwt, authorities);
    }

    private OAuth2AuthenticationException invalidToken() {
        return new OAuth2AuthenticationException(new OAuth2Error("invalid_token"), "Invalid access token");
    }
}




