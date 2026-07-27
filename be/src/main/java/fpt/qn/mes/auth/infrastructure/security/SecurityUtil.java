package fpt.qn.mes.auth.infrastructure.security;

import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import fpt.qn.mes.auth.application.security.AppUserPrincipal;
import fpt.qn.mes.auth.application.exception.UnauthorizedException;
import fpt.qn.mes.auth.application.port.out.CurrentUserPort;

@Component
public class SecurityUtil implements CurrentUserPort {

    @Override
    public AppUserPrincipal getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof AppUserPrincipal principal)) {
            throw new UnauthorizedException("Not authenticated");
        }
        return principal;
    }

    @Override
    public UUID getCurrentUserId() {
        return getCurrentUser().getId();
    }

    @Override
    public String getCurrentUsername() {
        return getCurrentUser().getUsername();
    }
}
