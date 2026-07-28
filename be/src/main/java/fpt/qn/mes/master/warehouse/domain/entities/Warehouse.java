package fpt.qn.mes.master.warehouse.domain.entities;

import java.time.Instant;
import java.util.UUID;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter @Builder @FieldDefaults(level = AccessLevel.PRIVATE)
public class Warehouse {
    UUID id; String code; String name; String address;
    UUID warehouseStatusId; Instant createdAt; UUID createdBy; Instant updatedAt; UUID updatedBy;

    public static Warehouse create(String code, String name, String address, UUID warehouseStatusId, UUID createdBy) {
        return Warehouse.builder()
                .id(UUID.randomUUID())
                .code(code)
                .name(name)
                .address(address)
                .warehouseStatusId(warehouseStatusId)
                .createdBy(createdBy)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }
}
