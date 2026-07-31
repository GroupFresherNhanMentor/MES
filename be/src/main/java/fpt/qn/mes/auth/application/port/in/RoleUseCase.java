package fpt.qn.mes.auth.application.port.in;

import java.util.List;
import java.util.UUID;

import fpt.qn.mes.auth.application.dto.request.CreateRoleRequest;
import fpt.qn.mes.auth.application.dto.response.RoleResponse;
import fpt.qn.mes.auth.application.dto.request.UpdateRoleRequest;

public interface RoleUseCase {
    List<RoleResponse> getRoles();

    RoleResponse getRoleById(UUID id);

    RoleResponse createRole(CreateRoleRequest request);

    RoleResponse updateRole(UUID id, UpdateRoleRequest request);

    void deleteRole(UUID id);
}
