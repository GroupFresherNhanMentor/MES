package fpt.qn.mes.auth.application.security;

import java.util.Collection;
import java.util.UUID;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AppUserPrincipal {

    UUID id;
    String username;
    boolean enabled;
    Collection<String> roles;
    Collection<String> permissions;
}
