package fpt.qn.mes.master.location.infrastructure.persistence;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import org.springframework.stereotype.Component;

import fpt.qn.mes.jooq.tables.records.WarehouseLocationsRecord;
import fpt.qn.mes.master.location.domain.entities.WarehouseLocation;

@Component
public class LocationRecordMapper {

    public WarehouseLocation toDomain(WarehouseLocationsRecord r) {
        if (r == null) return null;
        return WarehouseLocation.builder()
                .id(r.getId()).warehouseId(r.getWarehouseId())
                .code(r.getCode()).name(r.getName())
                .locationStatusId(r.getLocationStatusId())
                .createdAt(r.getCreatedAt().toInstant()).createdBy(r.getCreatedBy())
                .updatedAt(r.getUpdatedAt().toInstant()).updatedBy(r.getUpdatedBy())
                .build();
    }

    public WarehouseLocationsRecord toRecord(WarehouseLocation l) {
        WarehouseLocationsRecord r = new WarehouseLocationsRecord();
        r.setId(l.getId()); r.setWarehouseId(l.getWarehouseId());
        r.setCode(l.getCode()); r.setName(l.getName());
        r.setLocationStatusId(l.getLocationStatusId());
        r.setCreatedAt(OffsetDateTime.ofInstant(l.getCreatedAt(), ZoneOffset.UTC));
        r.setCreatedBy(l.getCreatedBy());
        r.setUpdatedAt(OffsetDateTime.ofInstant(l.getUpdatedAt(), ZoneOffset.UTC));
        r.setUpdatedBy(l.getUpdatedBy());
        return r;
    }
}
