package fpt.qn.mes.inventory.infrastructure.persistence;

import org.springframework.stereotype.Component;

import fpt.qn.mes.inventory.domain.entities.LotType;
import fpt.qn.mes.jooq.tables.records.LotTypesRecord;

@Component
public class LotTypeRecordMapper {

    public LotType toDomain(LotTypesRecord r) {
        if (r == null || r.getId() == null) return null;
        return LotType.builder()
                .id(r.getId())
                .name(r.getName())
                .description(r.getDescription())
                .build();
    }

    public LotTypesRecord toRecord(LotType lt) {
        LotTypesRecord r = new LotTypesRecord();
        r.setId(lt.getId());
        r.setName(lt.getName());
        r.setDescription(lt.getDescription());
        return r;
    }
}
