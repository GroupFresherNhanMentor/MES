package fpt.qn.mes.master.warehouse.infrastructure.persistence.warehouse;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import org.springframework.stereotype.Component;

import fpt.qn.mes.jooq.tables.records.WarehouseStatusesRecord;
import fpt.qn.mes.jooq.tables.records.WarehousesRecord;
import fpt.qn.mes.jooq.tables.records.UsersRecord;
import fpt.qn.mes.master.warehouse.domain.entities.Warehouse;
import fpt.qn.mes.master.warehouse.domain.entities.WarehouseStatus;

@Component
public class WarehouseRecordMapper {

    public Warehouse toDomain(WarehousesRecord r, WarehouseStatusesRecord status, UsersRecord creator, UsersRecord updater) {
        return toDomain(r, status, creator, updater, null);
    }

    public Warehouse toDomain(WarehousesRecord r, WarehouseStatusesRecord status, UsersRecord creator, UsersRecord updater, List<Warehouse.ManagerRef> managers) {
        if (r == null || r.getId() == null) return null;

        return Warehouse.builder()
                .id(r.getId())
                .code(r.getCode())
                .name(r.getName())
                .address(r.getAddress())
                .warehouseStatus(status.getId() != null ? WarehouseStatus.builder()
                        .id(status.getId())
                        .name(status.getName())
                        .description(status.getDescription())
                        .build() : null)
                .managers(managers)
                .createdAt(r.getCreatedAt().toInstant())
                .updatedAt(r.getUpdatedAt() != null ? r.getUpdatedAt().toInstant() : null)
                .createdBy(creator.getId() != null
                        ? Warehouse.UserRef.builder()
                            .id(creator.getId())
                            .fullName(creator.getFullName())
                            .username(creator.getUsername())
                            .build()
                        : null)
                .updatedBy(updater.getId() != null
                        ? Warehouse.UserRef.builder()
                            .id(updater.getId())
                            .fullName(updater.getFullName())
                            .username(updater.getUsername())
                            .build()
                        : null)
                .build();
    }

    public WarehousesRecord toRecord(Warehouse w) {
        WarehousesRecord r = new WarehousesRecord();
        r.setId(w.getId());
        r.setCode(w.getCode());
        r.setName(w.getName());
        r.setAddress(w.getAddress());
        r.setWarehouseStatusId(w.getWarehouseStatus() != null ? w.getWarehouseStatus().getId() : null);
        r.setCreatedAt(OffsetDateTime.ofInstant(w.getCreatedAt(), ZoneOffset.UTC));
        r.setUpdatedAt(w.getUpdatedAt() != null ? OffsetDateTime.ofInstant(w.getUpdatedAt(), ZoneOffset.UTC) : null);
        r.setCreatedBy(w.getCreatedBy() != null ? w.getCreatedBy().getId() : null);
        r.setUpdatedBy(w.getUpdatedBy() != null ? w.getUpdatedBy().getId() : null);
        return r;
    }
}
