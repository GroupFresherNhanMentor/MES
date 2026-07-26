package fpt.qn.mes.role.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import fpt.qn.mes.role.domain.entities.Permission;

public interface PermissionRepository {
    List<Permission> findAll();
    Optional<Permission> findById(UUID id);
    Permission save(Permission permission);
    Permission update(Permission permission);
    void deleteById(UUID id);
    boolean existsByName(String name);
}
