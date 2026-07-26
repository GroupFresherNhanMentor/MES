package fpt.qn.mes.role.infrastructure.persistence;

import java.util.List;

import org.springframework.stereotype.Component;

import fpt.qn.mes.jooq.tables.records.PermissionsRecord;
import fpt.qn.mes.jooq.tables.records.RolesRecord;
import fpt.qn.mes.role.domain.entities.Permission;
import fpt.qn.mes.role.domain.entities.Role;

@Component
public class RoleRecordMapper {

    public Role toDomain(RolesRecord r) {
        return null;
    }

    public Role toDomain(RolesRecord r, List<String> permissionNames) {
        return null;
    }

    public RolesRecord toRecord(Role role) {
        return null;
    }

    public Permission toDomain(PermissionsRecord r) {
        return null;
    }

    public PermissionsRecord toRecord(Permission p) {
        return null;
    }
}
