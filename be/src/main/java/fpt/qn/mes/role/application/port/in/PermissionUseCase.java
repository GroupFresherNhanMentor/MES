package fpt.qn.mes.role.application.port.in;

import java.util.List;
import java.util.UUID;

import fpt.qn.mes.role.application.dto.CreatePermissionRequest;
import fpt.qn.mes.role.application.dto.PermissionDto;

public interface PermissionUseCase {
    List<PermissionDto> getPermissions();
    PermissionDto getPermissionById(UUID id);
    PermissionDto createPermission(CreatePermissionRequest request);
    void deletePermission(UUID id);
}
