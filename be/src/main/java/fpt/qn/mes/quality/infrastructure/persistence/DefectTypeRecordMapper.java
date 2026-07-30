package fpt.qn.mes.quality.infrastructure.persistence;

import java.time.Instant;
import java.time.ZoneOffset;

import org.springframework.stereotype.Component;

import fpt.qn.mes.jooq.tables.records.DefectTypesRecord;
import fpt.qn.mes.quality.domain.entities.DefectType;

@Component
public class DefectTypeRecordMapper {

    public DefectType toDomain(DefectTypesRecord r) {
        return DefectType.builder()
            .id(r.getId())
            .name(r.getName())
            .description(r.getDescription())
            .createdBy(r.getCreatedBy())
            .updatedBy(r.getUpdatedBy())
            .createdAt(r.getCreatedAt() != null ? r.getCreatedAt().toInstant() : null)
            .updatedAt(r.getUpdatedAt() != null ? r.getUpdatedAt().toInstant() : null)
            .build();
    }

    public DefectTypesRecord toRecord(DefectType defectType) {
        DefectTypesRecord r = new DefectTypesRecord();
        r.setId(defectType.getId());
        r.setName(defectType.getName());
        r.setDescription(defectType.getDescription());
        r.setCreatedBy(defectType.getCreatedBy());
        r.setUpdatedBy(defectType.getUpdatedBy());
        Instant now = Instant.now();
        if (defectType.getCreatedAt() != null) {
            r.setCreatedAt(defectType.getCreatedAt().atOffset(ZoneOffset.UTC));
        } else {
            r.setCreatedAt(now.atOffset(ZoneOffset.UTC));
        }
        r.setUpdatedAt(now.atOffset(ZoneOffset.UTC));
        return r;
    }
}
