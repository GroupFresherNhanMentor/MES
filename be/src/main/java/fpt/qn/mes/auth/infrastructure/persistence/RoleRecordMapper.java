package fpt.qn.mes.auth.infrastructure.persistence;

import java.util.List;

import org.springframework.stereotype.Component;

import fpt.qn.mes.jooq.tables.records.RolesRecord;
import fpt.qn.mes.auth.domain.entities.Role;

@Component
public class RoleRecordMapper {

    public Role toDomain(RolesRecord r) {
        return toDomain(r, List.of());
    }

    public Role toDomain(RolesRecord r, List<String> permissionNames) {
        return Role.builder()
                .id(r.getId())
                .name(r.getName())
                .description(r.getDescription())
                .permissionNames(permissionNames == null ? List.of() : List.copyOf(permissionNames))
                .build();
    }

    public RolesRecord toRecord(Role role) {
        RolesRecord record = new RolesRecord();
        record.setId(role.getId());
        record.setName(role.getName());
        record.setDescription(role.getDescription());
        return record;
    }

}
