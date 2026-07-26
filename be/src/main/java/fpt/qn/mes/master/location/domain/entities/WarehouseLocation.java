package fpt.qn.mes.master.location.domain.entities;

import java.time.Instant;
import java.util.UUID;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter @Builder @FieldDefaults(level = AccessLevel.PRIVATE)
public class WarehouseLocation {
    UUID id; UUID warehouseId; String code; String name;
    UUID locationStatusId; Instant createdAt; UUID createdBy; Instant updatedAt; UUID updatedBy;

    public static WarehouseLocation create(UUID warehouseId, String code, String name, UUID locationStatusId, UUID createdBy) {
        throw new UnsupportedOperationException("Not implemented");
    }
}
