package fpt.qn.mes.role.application.port.in;

import java.util.List;
import java.util.UUID;

import fpt.qn.mes.role.application.dto.request.AssignPermissionsRequest;
import fpt.qn.mes.role.application.dto.request.CreateRoleRequest;
import fpt.qn.mes.role.application.dto.response.RoleDto;
import fpt.qn.mes.role.application.dto.request.UpdateRoleRequest;

public interface RoleUseCase {
    List<RoleDto> getRoles();
    RoleDto getRoleById(UUID id);
    RoleDto createRole(CreateRoleRequest request);
    RoleDto updateRole(UUID id, UpdateRoleRequest request);
    void deleteRole(UUID id);
    void assignPermissions(UUID id, AssignPermissionsRequest request);
}
