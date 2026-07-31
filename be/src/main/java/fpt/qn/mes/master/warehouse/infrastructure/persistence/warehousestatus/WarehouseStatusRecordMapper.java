package fpt.qn.mes.master.warehouse.infrastructure.persistence.warehousestatus;

import org.springframework.stereotype.Component;

import fpt.qn.mes.jooq.tables.records.WarehouseStatusesRecord;
import fpt.qn.mes.master.warehouse.domain.entities.WarehouseStatus;

@Component
public class WarehouseStatusRecordMapper {

    public WarehouseStatus toDomain(WarehouseStatusesRecord r) {
        if (r == null || r.getId() == null) return null;
        return WarehouseStatus.builder()
                .id(r.getId())
                .name(r.getName())
                .description(r.getDescription())
                .build();
    }

    public WarehouseStatusesRecord toRecord(WarehouseStatus s) {
        WarehouseStatusesRecord r = new WarehouseStatusesRecord();
        r.setId(s.getId());
        r.setName(s.getName());
        r.setDescription(s.getDescription());
        return r;
    }
}
