package fpt.qn.mes.auth.application.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fpt.qn.mes.auth.application.dto.request.CreateRoleRequest;
import fpt.qn.mes.auth.application.dto.request.UpdateRoleRequest;
import fpt.qn.mes.auth.application.dto.response.RoleResponse;
import fpt.qn.mes.auth.application.exception.RoleNotFoundException;
import fpt.qn.mes.auth.application.mapper.RoleDtoMapper;
import fpt.qn.mes.auth.application.port.in.RoleUseCase;
import fpt.qn.mes.auth.domain.entities.Role;
import fpt.qn.mes.auth.domain.repository.RoleRepository;
import fpt.qn.mes.auth.application.exception.RoleConflictException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RoleService implements RoleUseCase {

    RoleRepository roleRepository;
    RoleDtoMapper mapper;
    AdministrativeAccessGuard administrativeAccessGuard;

    @Override
    @Transactional(readOnly = true)
    public List<RoleResponse> getRoles() {
        List<RoleResponse> result = new java.util.ArrayList<>();
        for (Role role : roleRepository.findAll()) {
            result.add(toDto(role));
        }
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public RoleResponse getRoleById(UUID id) {
        return toDto(roleRepository.findById(id)
                .orElseThrow(() -> new RoleNotFoundException("Role not found")));
    }

    @Override
    @Transactional
    public RoleResponse createRole(CreateRoleRequest request) {
        if (roleRepository.existsByName(request.getName())) {
            throw new RoleConflictException("Role name already exists");
        }
        return toDto(roleRepository.save(Role.create(
                request.getName(), request.getDescription())));
    }

    @Override
    @Transactional
    public RoleResponse updateRole(UUID id, UpdateRoleRequest request) {
        administrativeAccessGuard.lock();
        Role current = roleRepository.findById(id)
                .orElseThrow(() -> new RoleNotFoundException("Role not found"));
        String normalized = request.getName() == null
                ? current.getName()
                : Role.normalizeName(request.getName());
        if ("ADMIN".equals(current.getName()) && !"ADMIN".equals(normalized)) {
            throw new RoleConflictException("System role ADMIN cannot be renamed");
        }
        if (!current.getName().equals(normalized) && roleRepository.existsByName(normalized)) {
            throw new RoleConflictException("Role name already exists");
        }
        return toDto(roleRepository.update(
                current.update(request.getName(), request.getDescription())));
    }

    @Override
    @Transactional
    public void deleteRole(UUID id) {
        administrativeAccessGuard.lock();
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RoleNotFoundException("Role not found"));
        if ("ADMIN".equals(role.getName()) || roleRepository.isAssigned(id)) {
            throw new RoleConflictException("Assigned or system role cannot be deleted");
        }
        roleRepository.deleteById(id);
        administrativeAccessGuard.assertAdministrativeAccessRemains();
    }

    private RoleResponse toDto(Role role) {
        return mapper.toDto(role);
    }
}
