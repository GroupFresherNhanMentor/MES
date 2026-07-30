package fpt.qn.mes.auth.application.service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fpt.qn.mes.auth.application.dto.request.ReplaceUserRolesRequest;
import fpt.qn.mes.auth.application.dto.response.RoleDto;
import fpt.qn.mes.auth.application.mapper.RoleDtoMapper;
import fpt.qn.mes.auth.application.port.in.UserRoleUseCase;
import fpt.qn.mes.auth.application.port.out.CredentialQueryPort;
import fpt.qn.mes.auth.domain.entities.Role;
import fpt.qn.mes.auth.domain.repository.UserRoleRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserRoleService implements UserRoleUseCase {

    CredentialQueryPort credentialQueryPort;
    UserRoleRepository userRoleRepository;
    RoleDtoMapper roleDtoMapper;
    AdministrativeAccessGuard administrativeAccessGuard;

    @Override
    @Transactional(readOnly = true)
    public List<RoleDto> getUserRoles(UUID userId) {
        requireUser(userId);
        return toDtos(userRoleRepository.findRolesByUserId(userId));
    }

    @Override
    @Transactional
    public List<RoleDto> replaceUserRoles(UUID userId, ReplaceUserRolesRequest request) {
        requireUser(userId);
        administrativeAccessGuard.lock();
        LinkedHashSet<UUID> unique = new LinkedHashSet<>(request.getRoleIds());
        List<UUID> roleIds = new ArrayList<>(unique);
        if (userRoleRepository.countExistingRoleIds(roleIds) != roleIds.size()) {
            throw new fpt.qn.mes.auth.application.exception.RoleNotFoundException(
                    "One or more roles were not found");
        }
        userRoleRepository.replaceRoles(userId, roleIds);
        administrativeAccessGuard.assertAdministrativeAccessRemains();
        return toDtos(userRoleRepository.findRolesByUserId(userId));
    }

    private fpt.qn.mes.auth.application.security.CredentialAccount requireUser(UUID userId) {
        return credentialQueryPort.findById(userId)
                .orElseThrow(() -> new fpt.qn.mes.auth.application.exception.UserReferenceNotFoundException(
                        "User not found"));
    }

    private List<RoleDto> toDtos(List<Role> roles) {
        List<RoleDto> result = new ArrayList<>();
        for (Role role : roles) {
            result.add(roleDtoMapper.toDto(role));
        }
        return result;
    }
}
