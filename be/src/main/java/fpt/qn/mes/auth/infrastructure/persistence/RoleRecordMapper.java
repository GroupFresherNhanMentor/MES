package fpt.qn.mes.auth.infrastructure.persistence;

import org.springframework.stereotype.Component;

import fpt.qn.mes.jooq.tables.records.RolesRecord;
import fpt.qn.mes.auth.domain.entities.Role;

@Component
public class RoleRecordMapper {

    public Role toDomain(RolesRecord r) {
        return Role.builder()
                .id(r.getId())
                .name(r.getName())
                .description(r.getDescription())
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
