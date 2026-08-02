package fpt.qn.mes.master.warehouse.domain.entities;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import fpt.qn.mes.common.util.UuidV7;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Getter @Builder @FieldDefaults(level = AccessLevel.PRIVATE)
public class Warehouse {

    @Getter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class UserRef {
        private UUID id;
        private String fullName;
        private String username;
    }

    @Getter @Builder @NoArgsConstructor @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class ManagerRef {
        UUID id;
        String fullName;
        String username;
        Instant assignedAt;
    }

    UUID id;
    String code;
    String name;
    String address;
    WarehouseStatus warehouseStatus;
    List<ManagerRef> managers;
    Instant createdAt;
    UserRef createdBy;
    Instant updatedAt;
    UserRef updatedBy;

    public static Warehouse create(String code, String name, String address, UUID warehouseStatusId, UUID createdBy) {
        return Warehouse.builder()
                .id(UuidV7.generate())
                .code(code)
                .name(name)
                .address(address)
                .warehouseStatus(WarehouseStatus.builder().id(warehouseStatusId).build())
                .createdBy(UserRef.builder().id(createdBy).build())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    public static Warehouse update(Warehouse existing, String name, String address, UUID warehouseStatusId, UUID updatedBy) {
        return Warehouse.builder()
                .id(existing.id)
                .code(existing.code)
                .name(name != null ? name : existing.name)
                .address(address != null ? address : existing.address)
                .warehouseStatus(warehouseStatusId != null ? WarehouseStatus.builder().id(warehouseStatusId).build() : existing.warehouseStatus)
                .createdAt(existing.createdAt)
                .createdBy(existing.createdBy)
                .updatedAt(Instant.now())
                .updatedBy(UserRef.builder().id(updatedBy).build())
                .build();
    }

    public static Warehouse activate(Warehouse existing, UUID activeStatusId, UUID updatedBy) {
        return Warehouse.builder()
                .id(existing.id)
                .code(existing.code)
                .name(existing.name)
                .address(existing.address)
                .warehouseStatus(WarehouseStatus.builder().id(activeStatusId).build())
                .createdAt(existing.createdAt)
                .createdBy(existing.createdBy)
                .updatedAt(Instant.now())
                .updatedBy(UserRef.builder().id(updatedBy).build())
                .build();
    }

    public static Warehouse deactivate(Warehouse existing, UUID inactiveStatusId, UUID updatedBy) {
        return Warehouse.builder()
                .id(existing.id)
                .code(existing.code)
                .name(existing.name)
                .address(existing.address)
                .warehouseStatus(WarehouseStatus.builder().id(inactiveStatusId).build())
                .createdAt(existing.createdAt)
                .createdBy(existing.createdBy)
                .updatedAt(Instant.now())
                .updatedBy(UserRef.builder().id(updatedBy).build())
                .build();
    }
}
