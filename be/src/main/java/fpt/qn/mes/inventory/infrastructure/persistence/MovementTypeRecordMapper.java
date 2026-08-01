package fpt.qn.mes.inventory.infrastructure.persistence;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import org.springframework.stereotype.Component;

import fpt.qn.mes.inventory.domain.entities.MovementType;
import fpt.qn.mes.jooq.tables.records.MovementTypesRecord;
import fpt.qn.mes.jooq.tables.records.UsersRecord;

@Component
public class MovementTypeRecordMapper {

    public MovementType toDomain(MovementTypesRecord r, UsersRecord creator, UsersRecord updater) {
        if (r == null || r.getId() == null) return null;
        return MovementType.builder()
                .id(r.getId())
                .name(r.getName())
                .description(r.getDescription())
                .createdAt(r.getCreatedAt() != null ? r.getCreatedAt().toInstant() : null)
                .createdBy(creator != null && creator.getId() != null
                        ? MovementType.UserRef.builder()
                                .id(creator.getId())
                                .fullName(creator.getFullName())
                                .username(creator.getUsername())
                                .build()
                        : null)
                .updatedAt(r.getUpdatedAt() != null ? r.getUpdatedAt().toInstant() : null)
                .updatedBy(updater != null && updater.getId() != null
                        ? MovementType.UserRef.builder()
                                .id(updater.getId())
                                .fullName(updater.getFullName())
                                .username(updater.getUsername())
                                .build()
                        : null)
                .build();
    }

    public MovementTypesRecord toRecord(MovementType mt) {
        MovementTypesRecord r = new MovementTypesRecord();
        r.setId(mt.getId());
        r.setName(mt.getName());
        r.setDescription(mt.getDescription());
        r.setCreatedAt(mt.getCreatedAt() != null ? OffsetDateTime.ofInstant(mt.getCreatedAt(), ZoneOffset.UTC) : null);
        r.setUpdatedAt(mt.getUpdatedAt() != null ? OffsetDateTime.ofInstant(mt.getUpdatedAt(), ZoneOffset.UTC) : null);
        r.setCreatedBy(mt.getCreatedBy() != null ? mt.getCreatedBy().getId() : null);
        r.setUpdatedBy(mt.getUpdatedBy() != null ? mt.getUpdatedBy().getId() : null);
        return r;
    }
}
