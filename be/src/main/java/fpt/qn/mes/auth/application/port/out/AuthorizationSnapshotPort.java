package fpt.qn.mes.auth.application.port.out;

import java.util.Optional;
import java.util.UUID;

import fpt.qn.mes.auth.application.security.AuthorizationSnapshot;

public interface AuthorizationSnapshotPort {

    Optional<AuthorizationSnapshot> load(UUID userId);
}
