package fpt.qn.mes.role.application.port.in;

import java.util.List;
import java.util.UUID;

import fpt.qn.mes.role.application.dto.AssignPermissionsRequest;
import fpt.qn.mes.role.application.dto.CreateRoleRequest;
import fpt.qn.mes.role.application.dto.RoleDto;
import fpt.qn.mes.role.application.dto.UpdateRoleRequest;

public interface RoleUseCase {
    List<RoleDto> getRoles();
    RoleDto getRoleById(UUID id);
    RoleDto createRole(CreateRoleRequest request);
    RoleDto updateRole(UUID id, UpdateRoleRequest request);
    void deleteRole(UUID id);
    void assignPermissions(UUID id, AssignPermissionsRequest request);
}
