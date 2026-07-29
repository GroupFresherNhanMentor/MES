package fpt.qn.mes.auth.application.port.in;

import java.util.Optional;
import java.util.UUID;

import fpt.qn.mes.auth.application.security.AuthorizationSnapshot;

public interface ResolveAuthorizationUseCase {

    Optional<AuthorizationSnapshot> resolve(UUID userId);
}
