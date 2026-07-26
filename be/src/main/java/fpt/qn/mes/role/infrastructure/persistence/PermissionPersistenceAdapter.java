package fpt.qn.mes.role.infrastructure.persistence;

import static fpt.qn.mes.jooq.Tables.PERMISSIONS;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import fpt.qn.mes.common.repository.BaseRepository;
import fpt.qn.mes.jooq.tables.records.PermissionsRecord;
import fpt.qn.mes.role.domain.entities.Permission;
import fpt.qn.mes.role.domain.repository.PermissionRepository;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@Repository
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PermissionPersistenceAdapter extends BaseRepository<PermissionsRecord> implements PermissionRepository {

    RoleRecordMapper mapper;

    public PermissionPersistenceAdapter(DSLContext ctx, RoleRecordMapper mapper) {
        super(ctx, PERMISSIONS); this.mapper = mapper;
    }

    @Override public List<Permission> findAll() { throw new UnsupportedOperationException("Not implemented"); }
    @Override public Optional<Permission> findById(UUID id) { throw new UnsupportedOperationException("Not implemented"); }
    @Override public Permission save(Permission permission) { throw new UnsupportedOperationException("Not implemented"); }
    @Override public Permission update(Permission permission) { throw new UnsupportedOperationException("Not implemented"); }
    @Override public void deleteById(UUID id) {}
    @Override public boolean existsByName(String name) { throw new UnsupportedOperationException("Not implemented"); }
}
