package fpt.qn.mes.auth.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import fpt.qn.mes.auth.application.dto.request.CreateRoleRequest;
import fpt.qn.mes.auth.application.dto.request.UpdateRoleRequest;
import fpt.qn.mes.auth.application.dto.response.RoleDto;
import fpt.qn.mes.auth.application.exception.RoleNotFoundException;
import fpt.qn.mes.auth.application.mapper.RoleDtoMapper;
import fpt.qn.mes.auth.domain.entities.Role;
import fpt.qn.mes.auth.domain.repository.RoleRepository;
import fpt.qn.mes.common.exception.ConflictException;

@ExtendWith(MockitoExtension.class)
class RoleServiceTest {

    @Mock RoleRepository roleRepository;
    @Mock RoleDtoMapper mapper;
    @Mock AdministrativeAccessGuard guard;

    RoleService service;

    @BeforeEach
    void setUp() {
        service = new RoleService(roleRepository, mapper, guard);
        org.mockito.Mockito.lenient().when(mapper.toDto(any(Role.class))).thenAnswer(invocation -> {
            Role role = invocation.getArgument(0);
            return RoleDto.builder()
                    .id(role.getId())
                    .name(role.getName())
                    .description(role.getDescription())
                    .build();
        });
    }

    @Test
    void createNormalizesTrimAndCase() {
        CreateRoleRequest request = new CreateRoleRequest();
        request.setName("  planner ");
        request.setDescription("Planning");
        when(roleRepository.existsByName("  planner ")).thenReturn(false);
        when(roleRepository.save(any(Role.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        RoleDto result = service.createRole(request);

        assertThat(result.getName()).isEqualTo("PLANNER");
        assertThat(result.getDescription()).isEqualTo("Planning");
    }

    @Test
    void duplicateNormalizedNameReturnsConflict() {
        CreateRoleRequest request = new CreateRoleRequest();
        request.setName(" admin ");
        when(roleRepository.existsByName(" admin ")).thenReturn(true);

        assertThatThrownBy(() -> service.createRole(request))
                .isInstanceOf(ConflictException.class);
        verify(roleRepository, never()).save(any());
    }

    @Test
    void adminRoleCannotBeRenamed() {
        UUID id = UUID.randomUUID();
        UpdateRoleRequest request = new UpdateRoleRequest();
        request.setName("SUPER_ADMIN");
        when(roleRepository.findById(id)).thenReturn(Optional.of(role(id, "ADMIN")));

        assertThatThrownBy(() -> service.updateRole(id, request))
                .isInstanceOf(ConflictException.class);
        verify(roleRepository, never()).update(any());
    }

    @Test
    void assignedAndSystemRolesCannotBeDeleted() {
        UUID adminId = UUID.randomUUID();
        when(roleRepository.findById(adminId)).thenReturn(Optional.of(role(adminId, "ADMIN")));
        assertThatThrownBy(() -> service.deleteRole(adminId))
                .isInstanceOf(ConflictException.class);

        UUID assignedId = UUID.randomUUID();
        when(roleRepository.findById(assignedId))
                .thenReturn(Optional.of(role(assignedId, "PLANNER")));
        when(roleRepository.isAssigned(assignedId)).thenReturn(true);
        assertThatThrownBy(() -> service.deleteRole(assignedId))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void customRoleIsReturnedWithoutDerivedAccessData() {
        UUID id = UUID.randomUUID();
        when(roleRepository.findById(id)).thenReturn(Optional.of(role(id, "CUSTOM")));

        assertThat(service.getRoleById(id).getName()).isEqualTo("CUSTOM");
    }

    @Test
    void updateAndUnassignedDeleteAreGuarded() {
        UUID id = UUID.randomUUID();
        Role current = role(id, "PLANNER");
        Role updated = role(id, "SCHEDULER");
        UpdateRoleRequest request = new UpdateRoleRequest();
        request.setName(" scheduler ");
        request.setDescription("Updated");
        when(roleRepository.findById(id)).thenReturn(Optional.of(current));
        when(roleRepository.existsByName("SCHEDULER")).thenReturn(false);
        when(roleRepository.update(any(Role.class))).thenReturn(updated);

        assertThat(service.updateRole(id, request).getName()).isEqualTo("SCHEDULER");

        when(roleRepository.isAssigned(id)).thenReturn(false);
        service.deleteRole(id);
        verify(roleRepository).deleteById(id);
        verify(guard).assertAdministrativeAccessRemains();
    }

    @Test
    void missingRoleReturnsNotFound() {
        UUID id = UUID.randomUUID();
        when(roleRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getRoleById(id))
                .isInstanceOf(RoleNotFoundException.class);
    }

    private Role role(UUID id, String name) {
        return Role.builder()
                .id(id)
                .name(name)
                .build();
    }
}
