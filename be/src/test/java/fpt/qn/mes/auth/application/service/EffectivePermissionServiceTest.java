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

import fpt.qn.mes.auth.application.permission.RolePermissionPolicy;
import fpt.qn.mes.auth.application.port.out.AuthorizationSnapshotPort;
import fpt.qn.mes.auth.application.security.AuthorizationSnapshot;

@ExtendWith(MockitoExtension.class)
class EffectivePermissionServiceTest {

    @Mock AuthorizationSnapshotPort snapshotPort;

    EffectivePermissionService service;
    UUID userId;

    @BeforeEach
    void setUp() {
        service = new EffectivePermissionService(snapshotPort, new RolePermissionPolicy());
        userId = UUID.randomUUID();
    }

    @Test
    void derivesDistinctSortedPermissionsFromAllCurrentRoles() {
        AuthorizationSnapshot stored = snapshot(true, List.of("AUDITOR", "OPERATOR"));
        when(snapshotPort.load(userId)).thenReturn(Optional.of(stored));

        AuthorizationSnapshot resolved = service.resolve(userId).orElseThrow();

        assertThat(resolved.getRoles()).containsExactly("AUDITOR", "OPERATOR");
        assertThat(resolved.getPermissions())
                .contains("STOCK_BALANCE_READ", "WORK_ORDER_EVENT_ADD")
                .doesNotHaveDuplicates()
                .isSorted();
    }

    @Test
    void rejectsInactiveUser() {
        when(snapshotPort.load(userId))
                .thenReturn(Optional.of(snapshot(false, List.of("ADMIN"))));

        assertThat(service.resolve(userId)).isEmpty();
    }

    @Test
    void noRoleUserAuthenticatesWithEmptyAuthorities() {
        when(snapshotPort.load(userId))
                .thenReturn(Optional.of(snapshot(true, List.of())));

        assertThat(service.resolve(userId)).isPresent();
        assertThat(service.resolve(userId).orElseThrow().getPermissions()).isEmpty();
    }

    @Test
    void unknownCustomRoleHasNoImplicitPermissions() {
        when(snapshotPort.load(userId))
                .thenReturn(Optional.of(snapshot(true, List.of("CUSTOM_ROLE"))));

        assertThat(service.resolve(userId).orElseThrow().getPermissions()).isEmpty();
    }

    private AuthorizationSnapshot snapshot(boolean active, List<String> roles) {
        return AuthorizationSnapshot.builder()
                .userId(userId)
                .username("alice")
                .active(active)
                .roles(roles)
                .permissions(List.of("IGNORED_DATABASE_PERMISSION"))
                .build();
    }
}
