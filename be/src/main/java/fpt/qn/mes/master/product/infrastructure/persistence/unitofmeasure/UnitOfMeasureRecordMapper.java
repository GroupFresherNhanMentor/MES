package fpt.qn.mes.master.product.infrastructure.persistence.unitofmeasure;

import java.time.ZoneOffset;

import org.springframework.stereotype.Component;

import fpt.qn.mes.jooq.tables.records.UnitsOfMeasureRecord;
import fpt.qn.mes.jooq.tables.records.UsersRecord;
import fpt.qn.mes.master.product.domain.entities.UnitOfMeasure;

@Component
public class UnitOfMeasureRecordMapper {

    public UnitOfMeasure toDomain(UnitsOfMeasureRecord r, UsersRecord creator, UsersRecord updater) {
        return UnitOfMeasure.builder()
            .id(r.getId())
            .name(r.getName())
            .description(r.getDescription())
            .createdBy(creator.getId() != null
                ? UnitOfMeasure.UserRef.builder().id(creator.getId()).fullName(creator.getFullName()).username(creator.getUsername()).build()
                : null)
            .updatedBy(updater.getId() != null
                ? UnitOfMeasure.UserRef.builder().id(updater.getId()).fullName(updater.getFullName()).username(updater.getUsername()).build()
                : null)
            .createdAt(r.getCreatedAt().toInstant())
            .updatedAt(r.getUpdatedAt() != null ? r.getUpdatedAt().toInstant() : null)
            .build();
    }

    public UnitsOfMeasureRecord toRecord(UnitOfMeasure unit) {
        UnitsOfMeasureRecord r = new UnitsOfMeasureRecord();
        r.setId(unit.getId());
        r.setName(unit.getName());
        r.setDescription(unit.getDescription());
        r.setCreatedBy(unit.getCreatedBy() != null ? unit.getCreatedBy().getId() : null);
        r.setUpdatedBy(unit.getUpdatedBy() != null ? unit.getUpdatedBy().getId() : null);
        r.setCreatedAt(unit.getCreatedAt().atOffset(ZoneOffset.UTC));
        r.setUpdatedAt(unit.getUpdatedAt() != null ? unit.getUpdatedAt().atOffset(ZoneOffset.UTC) : null);
        return r;
    }
}
