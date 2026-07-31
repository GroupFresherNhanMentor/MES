package fpt.qn.mes.inventory.infrastructure.persistence;

import org.springframework.stereotype.Component;

import fpt.qn.mes.inventory.domain.entities.StockStatus;
import fpt.qn.mes.jooq.tables.records.StockStatusesRecord;

@Component
public class StockStatusRecordMapper {

    public StockStatus toDomain(StockStatusesRecord r) {
        if (r == null || r.getId() == null) return null;
        return StockStatus.builder()
                .id(r.getId())
                .name(r.getName())
                .description(r.getDescription())
                .build();
    }

    public StockStatusesRecord toRecord(StockStatus ss) {
        StockStatusesRecord r = new StockStatusesRecord();
        r.setId(ss.getId());
        r.setName(ss.getName());
        r.setDescription(ss.getDescription());
        return r;
    }
}
