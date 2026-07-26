package fpt.qn.mes.master.warehouse.infrastructure.persistence;

import org.springframework.stereotype.Component;

import fpt.qn.mes.jooq.tables.records.WarehousesRecord;
import fpt.qn.mes.master.warehouse.domain.entities.Warehouse;

@Component
public class WarehouseRecordMapper {

    public Warehouse toDomain(WarehousesRecord r) {
        return null;
    }

    public WarehousesRecord toRecord(Warehouse w) {
        return null;
    }
}
