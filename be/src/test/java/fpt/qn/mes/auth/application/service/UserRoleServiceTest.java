package fpt.qn.mes.auth.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import fpt.qn.mes.auth.application.dto.request.ReplaceUserRolesRequest;
import fpt.qn.mes.auth.application.dto.response.RoleResponse;
import fpt.qn.mes.auth.application.exception.RoleNotFoundException;
import fpt.qn.mes.auth.application.exception.UserReferenceNotFoundException;
import fpt.qn.mes.auth.application.mapper.RoleDtoMapper;
import fpt.qn.mes.auth.application.port.out.CredentialQueryPort;
import fpt.qn.mes.auth.application.security.CredentialAccount;
import fpt.qn.mes.auth.domain.entities.Role;
import fpt.qn.mes.auth.domain.repository.UserRoleRepository;

@ExtendWith(MockitoExtension.class)
class UserRoleServiceTest {

    @Mock CredentialQueryPort credentialQueryPort;
    @Mock UserRoleRepository userRoleRepository;
    @Mock RoleDtoMapper mapper;
    @Mock AdministrativeAccessGuard guard;

    UserRoleService service;
    CredentialAccount user;

    @BeforeEach
    void setUp() {
        service = new UserRoleService(
                credentialQueryPort,
                userRoleRepository,
                mapper,
                guard);
        user = CredentialAccount.builder()
                .id(UUID.randomUUID())
                .username("alice")
                .passwordHash("$2a$encoded")
                .active(true)
                .build();
        org.mockito.Mockito.lenient().when(mapper.toDto(any(Role.class)))
                .thenAnswer(invocation -> {
                    Role role = invocation.getArgument(0);
                    return RoleResponse.builder()
                            .id(role.getId())
                            .name(role.getName())
                            .build();
                });
    }

    @Test
    void replacementDeduplicatesIdsAndReturnsAssignedRoles() {
        UUID first = UUID.randomUUID();
        UUID second = UUID.randomUUID();
        ReplaceUserRolesRequest request = request(List.of(first, second, first));
        Role role = Role.builder()
                .id(first)
                .name("AUDITOR")
                .build();
        when(credentialQueryPort.findById(user.getId())).thenReturn(Optional.of(user));
        when(userRoleRepository.countExistingRoleIds(List.of(first, second))).thenReturn(2L);
        when(userRoleRepository.findRolesByUserId(user.getId())).thenReturn(List.of(role));

        List<RoleResponse> result = service.replaceUserRoles(user.getId(), request);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getName()).isEqualTo("AUDITOR");
        verify(guard).lock();
        verify(userRoleRepository).replaceRoles(user.getId(), List.of(first, second));
        verify(guard).assertAdministrativeAccessRemains();
    }

    @Test
    void emptySetCompletelyRemovesRoles() {
        ReplaceUserRolesRequest request = request(List.of());
        when(credentialQueryPort.findById(user.getId())).thenReturn(Optional.of(user));
        when(userRoleRepository.countExistingRoleIds(List.of())).thenReturn(0L);
        when(userRoleRepository.findRolesByUserId(user.getId())).thenReturn(List.of());

        assertThat(service.replaceUserRoles(user.getId(), request)).isEmpty();
        verify(userRoleRepository).replaceRoles(user.getId(), List.of());
    }

    @Test
    void missingRoleRejectsWholeMutation() {
        UUID existing = UUID.randomUUID();
        UUID missing = UUID.randomUUID();
        when(credentialQueryPort.findById(user.getId())).thenReturn(Optional.of(user));
        when(userRoleRepository.countExistingRoleIds(List.of(existing, missing))).thenReturn(1L);

        assertThatThrownBy(() -> service.replaceUserRoles(
                user.getId(), request(List.of(existing, missing))))
                .isInstanceOf(RoleNotFoundException.class);
        verify(userRoleRepository, never()).replaceRoles(
                org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
        verify(guard, never()).assertAdministrativeAccessRemains();
    }

    @Test
    void missingUserReturnsNotFoundBeforeMutationLock() {
        when(credentialQueryPort.findById(user.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.replaceUserRoles(
                user.getId(), request(List.of())))
                .isInstanceOf(UserReferenceNotFoundException.class);
        verify(guard, never()).lock();
    }


    private ReplaceUserRolesRequest request(List<UUID> roleIds) {
        ReplaceUserRolesRequest request = new ReplaceUserRolesRequest();
        request.setRoleIds(roleIds);
        return request;
    }
}
