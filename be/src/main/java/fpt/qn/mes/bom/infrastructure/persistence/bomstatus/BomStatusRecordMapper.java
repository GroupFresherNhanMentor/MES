package fpt.qn.mes.bom.infrastructure.persistence.bomstatus;

import org.springframework.stereotype.Component;

import fpt.qn.mes.bom.domain.entities.BomStatus;
import fpt.qn.mes.jooq.tables.records.BomStatusesRecord;

@Component
public class BomStatusRecordMapper {

    public BomStatus toDomain(BomStatusesRecord r) {
        if (r == null || r.getId() == null) return null;
        return BomStatus.builder()
                .id(r.getId())
                .name(r.getName())
                .description(r.getDescription())
                .build();
    }

    public BomStatusesRecord toRecord(BomStatus s) {
        BomStatusesRecord r = new BomStatusesRecord();
        r.setId(s.getId());
        r.setName(s.getName());
        r.setDescription(s.getDescription());
        return r;
    }
}
