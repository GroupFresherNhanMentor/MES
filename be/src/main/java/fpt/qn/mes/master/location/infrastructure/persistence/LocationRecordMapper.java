package fpt.qn.mes.master.location.infrastructure.persistence;

import org.springframework.stereotype.Component;

import fpt.qn.mes.jooq.tables.records.WarehouseLocationsRecord;
import fpt.qn.mes.master.location.domain.entities.WarehouseLocation;

@Component
public class LocationRecordMapper {

    public WarehouseLocation toDomain(WarehouseLocationsRecord r) {
        return null;
    }

    public WarehouseLocationsRecord toRecord(WarehouseLocation l) {
        return null;
    }
}
