package fpt.qn.mes.master.location.domain.entities;

import java.time.Instant;
import java.util.UUID;

import fpt.qn.mes.common.util.UuidV7;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Getter @Builder @FieldDefaults(level = AccessLevel.PRIVATE)
public class WarehouseLocation {

    @Getter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class WarehouseRef {
        private UUID id;
        private String code;
        private String name;
        private String address;
    }

    @Getter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class UserRef {
        private UUID id;
        private String fullName;
        private String username;
    }

    UUID id;
    WarehouseRef warehouse;
    String code;
    String name;
    LocationStatus locationStatus;
    Instant createdAt;
    UserRef createdBy;
    Instant updatedAt;
    UserRef updatedBy;

    public static WarehouseLocation create(UUID warehouseId, String code, String name, UUID locationStatusId, UUID createdBy) {
        return WarehouseLocation.builder()
                .id(UuidV7.generate())
                .warehouse(WarehouseRef.builder().id(warehouseId).build())
                .code(code)
                .name(name)
                .locationStatus(LocationStatus.builder().id(locationStatusId).build())
                .createdBy(UserRef.builder().id(createdBy).build())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    public static WarehouseLocation update(WarehouseLocation existing, String name, UUID locationStatusId, UUID updatedBy) {
        return WarehouseLocation.builder()
                .id(existing.id)
                .warehouse(existing.warehouse)
                .code(existing.code)
                .name(name != null ? name : existing.name)
                .locationStatus(locationStatusId != null ? LocationStatus.builder().id(locationStatusId).build() : existing.locationStatus)
                .createdAt(existing.createdAt)
                .createdBy(existing.createdBy)
                .updatedAt(Instant.now())
                .updatedBy(UserRef.builder().id(updatedBy).build())
                .build();
    }

    public static WarehouseLocation activate(WarehouseLocation existing, UUID activeStatusId, UUID updatedBy) {
        return WarehouseLocation.builder()
                .id(existing.id)
                .warehouse(existing.warehouse)
                .code(existing.code)
                .name(existing.name)
                .locationStatus(LocationStatus.builder().id(activeStatusId).build())
                .createdAt(existing.createdAt)
                .createdBy(existing.createdBy)
                .updatedAt(Instant.now())
                .updatedBy(UserRef.builder().id(updatedBy).build())
                .build();
    }

    public static WarehouseLocation deactivate(WarehouseLocation existing, UUID inactiveStatusId, UUID updatedBy) {
        return WarehouseLocation.builder()
                .id(existing.id)
                .warehouse(existing.warehouse)
                .code(existing.code)
                .name(existing.name)
                .locationStatus(LocationStatus.builder().id(inactiveStatusId).build())
                .createdAt(existing.createdAt)
                .createdBy(existing.createdBy)
                .updatedAt(Instant.now())
                .updatedBy(UserRef.builder().id(updatedBy).build())
                .build();
    }
}
