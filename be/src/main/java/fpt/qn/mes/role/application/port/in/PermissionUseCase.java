package fpt.qn.mes.role.application.port.in;

import java.util.List;
import java.util.UUID;

import fpt.qn.mes.role.application.dto.request.CreatePermissionRequest;
import fpt.qn.mes.role.application.dto.response.PermissionResponse;

public interface PermissionUseCase {
    List<PermissionResponse> getPermissions();
    PermissionResponse getPermissionById(UUID id);
    PermissionResponse createPermission(CreatePermissionRequest request);
    void deletePermission(UUID id);
}
