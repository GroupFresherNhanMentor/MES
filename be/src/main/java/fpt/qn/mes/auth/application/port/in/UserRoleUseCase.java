package fpt.qn.mes.auth.application.port.in;

import java.util.List;
import java.util.UUID;

import fpt.qn.mes.auth.application.dto.request.ReplaceUserRolesRequest;
import fpt.qn.mes.auth.application.dto.response.RoleResponse;

public interface UserRoleUseCase {

    List<RoleResponse> getUserRoles(UUID userId);

    List<RoleResponse> replaceUserRoles(UUID userId, ReplaceUserRolesRequest request);
}
