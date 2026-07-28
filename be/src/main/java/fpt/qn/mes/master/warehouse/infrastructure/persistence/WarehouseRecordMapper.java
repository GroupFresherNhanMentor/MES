package fpt.qn.mes.master.warehouse.infrastructure.persistence;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import org.springframework.stereotype.Component;

import fpt.qn.mes.jooq.tables.records.WarehousesRecord;
import fpt.qn.mes.master.warehouse.domain.entities.Warehouse;

@Component
public class WarehouseRecordMapper {

    public Warehouse toDomain(WarehousesRecord r) {
        if (r == null) return null;
        return Warehouse.builder()
                .id(r.getId()).code(r.getCode()).name(r.getName()).address(r.getAddress())
                .warehouseStatusId(r.getWarehouseStatusId())
                .createdAt(r.getCreatedAt().toInstant()).createdBy(r.getCreatedBy())
                .updatedAt(r.getUpdatedAt().toInstant()).updatedBy(r.getUpdatedBy())
                .build();
    }

    public WarehousesRecord toRecord(Warehouse w) {
        WarehousesRecord r = new WarehousesRecord();
        r.setId(w.getId()); r.setCode(w.getCode()); r.setName(w.getName()); r.setAddress(w.getAddress());
        r.setWarehouseStatusId(w.getWarehouseStatusId());
        r.setCreatedAt(OffsetDateTime.ofInstant(w.getCreatedAt(), ZoneOffset.UTC));
        r.setCreatedBy(w.getCreatedBy());
        r.setUpdatedAt(OffsetDateTime.ofInstant(w.getUpdatedAt(), ZoneOffset.UTC));
        r.setUpdatedBy(w.getUpdatedBy());
        return r;
    }
}
