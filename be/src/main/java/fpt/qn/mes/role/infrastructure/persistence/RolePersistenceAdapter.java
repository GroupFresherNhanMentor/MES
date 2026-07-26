package fpt.qn.mes.role.infrastructure.persistence;

import static fpt.qn.mes.jooq.Tables.ROLES;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import fpt.qn.mes.common.repository.BaseRepository;
import fpt.qn.mes.jooq.tables.records.RolesRecord;
import fpt.qn.mes.role.domain.entities.Role;
import fpt.qn.mes.role.domain.repository.RoleRepository;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@Repository
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RolePersistenceAdapter extends BaseRepository<RolesRecord> implements RoleRepository {

    RoleRecordMapper mapper;

    public RolePersistenceAdapter(DSLContext ctx, RoleRecordMapper mapper) {
        super(ctx, ROLES); this.mapper = mapper;
    }

    @Override public List<Role> findAll() { throw new UnsupportedOperationException("Not implemented"); }
    @Override public Optional<Role> findById(UUID id) { throw new UnsupportedOperationException("Not implemented"); }
    @Override public Role save(Role role) { throw new UnsupportedOperationException("Not implemented"); }
    @Override public Role update(Role role) { throw new UnsupportedOperationException("Not implemented"); }
    @Override public void deleteById(UUID id) {}
    @Override public boolean existsByName(String name) { throw new UnsupportedOperationException("Not implemented"); }
    @Override public void assignPermissions(UUID roleId, List<UUID> permissionIds) {}
}
