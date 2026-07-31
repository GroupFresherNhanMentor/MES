package fpt.qn.mes.role.application.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fpt.qn.mes.role.application.dto.request.AssignPermissionsRequest;
import fpt.qn.mes.role.application.dto.request.CreateRoleRequest;
import fpt.qn.mes.role.application.dto.response.RoleResponse;
import fpt.qn.mes.role.application.dto.request.UpdateRoleRequest;
import fpt.qn.mes.role.application.mapper.RoleDtoMapper;
import fpt.qn.mes.role.application.port.in.RoleUseCase;
import fpt.qn.mes.role.domain.repository.RoleRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RoleService implements RoleUseCase {

    RoleRepository roleRepository;
    RoleDtoMapper mapper;

    @Override @Transactional(readOnly = true)
    public List<RoleResponse> getRoles() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override @Transactional(readOnly = true)
    public RoleResponse getRoleById(UUID id) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override @Transactional
    public RoleResponse createRole(CreateRoleRequest request) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override @Transactional
    public RoleResponse updateRole(UUID id, UpdateRoleRequest request) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override @Transactional
    public void deleteRole(UUID id) {}

    @Override @Transactional
    public void assignPermissions(UUID id, AssignPermissionsRequest request) {}
}
