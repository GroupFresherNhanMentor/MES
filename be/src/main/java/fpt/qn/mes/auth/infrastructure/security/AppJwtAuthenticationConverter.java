package fpt.qn.mes.auth.infrastructure.security;

import java.util.ArrayList;
import java.util.UUID;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import fpt.qn.mes.auth.application.port.out.CredentialQueryPort;
import fpt.qn.mes.auth.application.security.AppUserPrincipal;
import fpt.qn.mes.auth.application.security.CredentialAccount;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AppJwtAuthenticationConverter
        implements Converter<Jwt, UsernamePasswordAuthenticationToken> {

    CredentialQueryPort credentialQueryPort;

    @Override
    public UsernamePasswordAuthenticationToken convert(Jwt jwt) {
        UUID userId;
        try {
            userId = UUID.fromString(jwt.getSubject());
        } catch (RuntimeException ex) {
            throw invalidToken();
        }

        CredentialAccount account = credentialQueryPort.findById(userId)
                .filter(CredentialAccount::isActive)
                .orElseThrow(this::invalidToken);

        AppUserPrincipal principal = AppUserPrincipal.builder()
                .id(account.getId())
                .username(account.getUsername())
                .enabled(account.isActive())
                .roles(account.getRoles())
                .build();

        var authorities = new ArrayList<SimpleGrantedAuthority>();
        for (String role : account.getRoles()) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
        }

        return new UsernamePasswordAuthenticationToken(principal, jwt, authorities);
    }

    private OAuth2AuthenticationException invalidToken() {
        return new OAuth2AuthenticationException(new OAuth2Error("invalid_token"), "Invalid access token");
    }
}

