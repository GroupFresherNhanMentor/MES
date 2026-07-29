package fpt.qn.mes.master.location.domain.entities;

import java.time.Instant;
import java.util.UUID;

import fpt.qn.mes.common.util.UuidV7;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter @Builder @FieldDefaults(level = AccessLevel.PRIVATE)
public class WarehouseLocation {
    UUID id; UUID warehouseId; String code; String name;
    UUID locationStatusId; Instant createdAt; UUID createdBy; Instant updatedAt; UUID updatedBy;

    public static WarehouseLocation create(UUID warehouseId, String code, String name, UUID locationStatusId, UUID createdBy) {
        return WarehouseLocation.builder()
                .id(UuidV7.generate())
                .warehouseId(warehouseId)
                .code(code)
                .name(name)
                .locationStatusId(locationStatusId)
                .createdBy(createdBy)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }
}
