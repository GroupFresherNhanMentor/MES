package fpt.qn.mes.role.application.port.in;

import java.util.List;
import java.util.UUID;

import fpt.qn.mes.role.application.dto.request.AssignPermissionsRequest;
import fpt.qn.mes.role.application.dto.request.CreateRoleRequest;
import fpt.qn.mes.role.application.dto.response.RoleResponse;
import fpt.qn.mes.role.application.dto.request.UpdateRoleRequest;

public interface RoleUseCase {
    List<RoleResponse> getRoles();
    RoleResponse getRoleById(UUID id);
    RoleResponse createRole(CreateRoleRequest request);
    RoleResponse updateRole(UUID id, UpdateRoleRequest request);
    void deleteRole(UUID id);
    void assignPermissions(UUID id, AssignPermissionsRequest request);
}
