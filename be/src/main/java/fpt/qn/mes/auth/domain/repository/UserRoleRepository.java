package fpt.qn.mes.auth.domain.repository;

import java.util.List;
import java.util.UUID;

import fpt.qn.mes.auth.domain.entities.Role;

public interface UserRoleRepository {

    List<Role> findRolesByUserId(UUID userId);

    long countExistingRoleIds(List<UUID> roleIds);

    void replaceRoles(UUID userId, List<UUID> roleIds);
}
