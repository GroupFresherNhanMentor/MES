package fpt.qn.mes.auth.infrastructure.security;

import static fpt.qn.mes.jooq.Tables.ROLES;
import static fpt.qn.mes.jooq.Tables.USERS;
import static fpt.qn.mes.jooq.Tables.USER_ROLES;

import java.util.List;

import org.jooq.DSLContext;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import fpt.qn.mes.auth.application.security.AppUserPrincipal;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AppJwtAuthenticationConverter
        implements Converter<Jwt, UsernamePasswordAuthenticationToken> {

    DSLContext ctx;

    @Override
    public UsernamePasswordAuthenticationToken convert(Jwt jwt) {
        var rows = ctx.select(USERS.ID, USERS.USERNAME, USERS.ACTIVE, ROLES.NAME)
            .from(USERS)
            .leftJoin(USER_ROLES).on(USER_ROLES.USER_ID.eq(USERS.ID))
            .leftJoin(ROLES).on(ROLES.ID.eq(USER_ROLES.ROLE_ID))
            .where(USERS.USERNAME.eq(jwt.getSubject()))
            .fetch();

        if (rows.isEmpty()) throw new UsernameNotFoundException("User not found: " + jwt.getSubject());

        var first = rows.getFirst();
        List<String> roles = rows.stream()
            .filter(r -> r.get(ROLES.NAME) != null)
            .map(r -> r.get(ROLES.NAME))
            .toList();

        AppUserPrincipal principal = AppUserPrincipal.builder()
            .id(first.get(USERS.ID))
            .username(first.get(USERS.USERNAME))
            .enabled(Boolean.TRUE.equals(first.get(USERS.ACTIVE)))
            .roles(roles)
            .build();

        var authorities = roles.stream()
            .map(r -> new SimpleGrantedAuthority("ROLE_" + r))
            .toList();

        return new UsernamePasswordAuthenticationToken(principal, jwt, authorities);
    }
}
