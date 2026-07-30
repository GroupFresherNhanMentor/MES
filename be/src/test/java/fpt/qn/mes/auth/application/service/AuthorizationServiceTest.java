package fpt.qn.mes.auth.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import fpt.qn.mes.auth.application.port.out.AuthorizationSnapshotPort;
import fpt.qn.mes.auth.application.security.AuthorizationSnapshot;

@ExtendWith(MockitoExtension.class)
class AuthorizationServiceTest {

    @Mock AuthorizationSnapshotPort snapshotPort;

    AuthorizationService service;
    UUID userId;

    @BeforeEach
    void setUp() {
        service = new AuthorizationService(snapshotPort);
        userId = UUID.randomUUID();
    }

    @Test
    void returnsCurrentRolesFromSnapshot() {
        AuthorizationSnapshot stored = snapshot(true, List.of("AUDITOR", "OPERATOR"));
        when(snapshotPort.load(userId)).thenReturn(Optional.of(stored));

        AuthorizationSnapshot resolved = service.resolve(userId).orElseThrow();

        assertThat(resolved.getRoles()).containsExactly("AUDITOR", "OPERATOR");
    }

    @Test
    void rejectsInactiveUser() {
        when(snapshotPort.load(userId))
                .thenReturn(Optional.of(snapshot(false, List.of("ADMIN"))));

        assertThat(service.resolve(userId)).isEmpty();
    }

    @Test
    void noRoleUserStillResolvesAsAuthenticatedIdentity() {
        when(snapshotPort.load(userId))
                .thenReturn(Optional.of(snapshot(true, List.of())));

        assertThat(service.resolve(userId)).isPresent();
        assertThat(service.resolve(userId).orElseThrow().getRoles()).isEmpty();
    }

    @Test
    void customRoleIsPreservedForRoleChecks() {
        when(snapshotPort.load(userId))
                .thenReturn(Optional.of(snapshot(true, List.of("CUSTOM_ROLE"))));

        assertThat(service.resolve(userId).orElseThrow().getRoles())
                .containsExactly("CUSTOM_ROLE");
    }

    private AuthorizationSnapshot snapshot(boolean active, List<String> roles) {
        return AuthorizationSnapshot.builder()
                .userId(userId)
                .username("alice")
                .active(active)
                .roles(roles)
                .build();
    }
}
