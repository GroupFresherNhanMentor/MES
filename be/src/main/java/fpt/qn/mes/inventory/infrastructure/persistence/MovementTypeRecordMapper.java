package fpt.qn.mes.inventory.infrastructure.persistence;

import org.springframework.stereotype.Component;

import fpt.qn.mes.inventory.domain.entities.MovementType;
import fpt.qn.mes.jooq.tables.records.MovementTypesRecord;

@Component
public class MovementTypeRecordMapper {

    public MovementType toDomain(MovementTypesRecord r) {
        if (r == null || r.getId() == null) return null;
        return MovementType.builder()
                .id(r.getId())
                .name(r.getName())
                .description(r.getDescription())
                .build();
    }

    public MovementTypesRecord toRecord(MovementType mt) {
        MovementTypesRecord r = new MovementTypesRecord();
        r.setId(mt.getId());
        r.setName(mt.getName());
        r.setDescription(mt.getDescription());
        return r;
    }
}
