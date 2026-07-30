package fpt.qn.mes.master.product.infrastructure.persistence;

import java.time.ZoneOffset;

import org.springframework.stereotype.Component;

import fpt.qn.mes.jooq.tables.records.UnitsOfMeasureRecord;
import fpt.qn.mes.master.product.domain.entities.UnitOfMeasure;

@Component
public class UnitOfMeasureRecordMapper {

    public UnitOfMeasure toDomain(UnitsOfMeasureRecord r) {
        return UnitOfMeasure.builder()
            .id(r.getId())
            .name(r.getName())
            .description(r.getDescription())
            .createdBy(r.getCreatedBy())
            .updatedBy(r.getUpdatedBy())
            .createdAt(r.getCreatedAt() != null ? r.getCreatedAt().toInstant() : null)
            .updatedAt(r.getUpdatedAt() != null ? r.getUpdatedAt().toInstant() : null)
            .build();
    }

    public UnitsOfMeasureRecord toRecord(UnitOfMeasure unit) {
        UnitsOfMeasureRecord r = new UnitsOfMeasureRecord();
        r.setId(unit.getId());
        r.setName(unit.getName());
        r.setDescription(unit.getDescription());
        r.setCreatedBy(unit.getCreatedBy());
        r.setUpdatedBy(unit.getUpdatedBy());
        if (unit.getCreatedAt() != null) r.setCreatedAt(unit.getCreatedAt().atOffset(ZoneOffset.UTC));
        if (unit.getUpdatedAt() != null) r.setUpdatedAt(unit.getUpdatedAt().atOffset(ZoneOffset.UTC));
        return r;
    }
}
