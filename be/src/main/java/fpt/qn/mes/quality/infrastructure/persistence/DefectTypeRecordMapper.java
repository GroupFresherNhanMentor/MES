package fpt.qn.mes.quality.infrastructure.persistence;

import org.springframework.stereotype.Component;

import fpt.qn.mes.jooq.tables.records.DefectTypesRecord;
import fpt.qn.mes.quality.domain.entities.DefectType;

@Component
public class DefectTypeRecordMapper {

    public DefectType toDomain(DefectTypesRecord r) {
        if (r == null) {
            return null;
        }
        return DefectType.builder()
            .id(r.getId())
            .name(r.getName())
            .description(r.getDescription())
            .build();
    }

    public DefectTypesRecord toRecord(DefectType defectType) {
        if (defectType == null) {
            return null;
        }
        DefectTypesRecord r = new DefectTypesRecord();
        r.setId(defectType.getId());
        r.setName(defectType.getName());
        r.setDescription(defectType.getDescription());
        return r;
    }
}
