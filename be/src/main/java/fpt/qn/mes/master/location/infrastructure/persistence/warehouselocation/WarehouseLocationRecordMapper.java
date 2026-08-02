package fpt.qn.mes.master.location.infrastructure.persistence.warehouselocation;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import org.springframework.stereotype.Component;

import fpt.qn.mes.jooq.tables.records.LocationStatusesRecord;
import fpt.qn.mes.jooq.tables.records.WarehouseLocationsRecord;
import fpt.qn.mes.jooq.tables.records.WarehousesRecord;
import fpt.qn.mes.jooq.tables.records.UsersRecord;
import fpt.qn.mes.master.location.domain.entities.WarehouseLocation;
import fpt.qn.mes.master.location.domain.entities.LocationStatus;

@Component
public class WarehouseLocationRecordMapper {

    public WarehouseLocation toDomain(WarehouseLocationsRecord r, WarehousesRecord warehouse, LocationStatusesRecord status, UsersRecord creator, UsersRecord updater) {
        if (r == null || r.getId() == null) return null;

        return WarehouseLocation.builder()
                .id(r.getId())
                .warehouse(warehouse.getId() != null
                        ? WarehouseLocation.WarehouseRef.builder()
                                .id(warehouse.getId())
                                .code(warehouse.getCode())
                                .name(warehouse.getName())
                                .address(warehouse.getAddress())
                                .build()
                        : null)
                .code(r.getCode())
                .name(r.getName())
                .locationStatus(status.getId() != null ? LocationStatus.builder()
                        .id(status.getId())
                        .name(status.getName())
                        .description(status.getDescription())
                        .build() : null)
                .createdAt(r.getCreatedAt().toInstant())
                .updatedAt(r.getUpdatedAt() != null ? r.getUpdatedAt().toInstant() : null)
                .createdBy(creator.getId() != null
                        ? WarehouseLocation.UserRef.builder()
                            .id(creator.getId())
                            .fullName(creator.getFullName())
                            .username(creator.getUsername())
                            .build()
                        : null)
                .updatedBy(updater.getId() != null
                        ? WarehouseLocation.UserRef.builder()
                            .id(updater.getId())
                            .fullName(updater.getFullName())
                            .username(updater.getUsername())
                            .build()
                        : null)
                .build();
    }

    public WarehouseLocationsRecord toRecord(WarehouseLocation loc) {
        WarehouseLocationsRecord r = new WarehouseLocationsRecord();
        r.setId(loc.getId());
        r.setWarehouseId(loc.getWarehouse() != null ? loc.getWarehouse().getId() : null);
        r.setCode(loc.getCode());
        r.setName(loc.getName());
        r.setLocationStatusId(loc.getLocationStatus() != null ? loc.getLocationStatus().getId() : null);
        r.setCreatedAt(OffsetDateTime.ofInstant(loc.getCreatedAt(), ZoneOffset.UTC));
        r.setUpdatedAt(loc.getUpdatedAt() != null ? OffsetDateTime.ofInstant(loc.getUpdatedAt(), ZoneOffset.UTC) : null);
        r.setCreatedBy(loc.getCreatedBy() != null ? loc.getCreatedBy().getId() : null);
        r.setUpdatedBy(loc.getUpdatedBy() != null ? loc.getUpdatedBy().getId() : null);
        return r;
    }
}
