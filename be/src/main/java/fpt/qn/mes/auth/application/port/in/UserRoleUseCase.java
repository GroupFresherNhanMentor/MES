package fpt.qn.mes.auth.application.port.in;

import java.util.List;
import java.util.UUID;

import fpt.qn.mes.auth.application.dto.request.ReplaceUserRolesRequest;
import fpt.qn.mes.auth.application.dto.response.RoleDto;

public interface UserRoleUseCase {

    List<RoleDto> getUserRoles(UUID userId);

    List<RoleDto> replaceUserRoles(UUID userId, ReplaceUserRolesRequest request);
}
