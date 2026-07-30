package fpt.qn.mes.auth.application.service;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fpt.qn.mes.auth.application.port.in.ResolveAuthorizationUseCase;
import fpt.qn.mes.auth.application.port.out.AuthorizationSnapshotPort;
import fpt.qn.mes.auth.application.security.AuthorizationSnapshot;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthorizationService implements ResolveAuthorizationUseCase {

    AuthorizationSnapshotPort authorizationSnapshotPort;

    @Override
    @Transactional(readOnly = true)
    public Optional<AuthorizationSnapshot> resolve(UUID userId) {
        return authorizationSnapshotPort.load(userId)
                .filter(snapshot -> snapshot.isActive());
    }
}
