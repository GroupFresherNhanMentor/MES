package fpt.qn.mes.auth.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import fpt.qn.mes.auth.domain.entities.Role;

public interface RoleRepository {
    List<Role> findAll();
    Optional<Role> findById(UUID id);
    Role save(Role role);
    Role update(Role role);
    void deleteById(UUID id);
    boolean existsByName(String name);
    boolean isAssigned(UUID id);
}
