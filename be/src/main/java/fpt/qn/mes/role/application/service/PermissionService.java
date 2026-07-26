package fpt.qn.mes.role.application.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fpt.qn.mes.role.application.dto.CreatePermissionRequest;
import fpt.qn.mes.role.application.dto.PermissionDto;
import fpt.qn.mes.role.application.mapper.RoleDtoMapper;
import fpt.qn.mes.role.application.port.in.PermissionUseCase;
import fpt.qn.mes.role.domain.repository.PermissionRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PermissionService implements PermissionUseCase {

    PermissionRepository permissionRepository;
    RoleDtoMapper mapper;

    @Override @Transactional(readOnly = true)
    public List<PermissionDto> getPermissions() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override @Transactional(readOnly = true)
    public PermissionDto getPermissionById(UUID id) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override @Transactional
    public PermissionDto createPermission(CreatePermissionRequest request) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override @Transactional
    public void deletePermission(UUID id) {}
}
