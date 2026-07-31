package fpt.qn.mes.inventory.domain.entities;

import java.time.Instant;
import java.util.UUID;

import fpt.qn.mes.common.util.UuidV7;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MovementType {

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class UserRef {
        UUID id;
        String fullName;
        String username;
    }

    UUID id;
    String name;
    String description;
    Instant createdAt;
    UserRef createdBy;
    Instant updatedAt;
    UserRef updatedBy;

    public static MovementType create(String name, String description, UUID createdBy) {
        return MovementType.builder()
                .id(UuidV7.generate())
                .name(name)
                .description(description)
                .createdAt(Instant.now())
                .createdBy(UserRef.builder().id(createdBy).build())
                .updatedAt(Instant.now())
                .build();
    }

    public static MovementType update(MovementType existing, String name, String description, UUID updatedBy) {
        return MovementType.builder()
                .id(existing.id)
                .name(name != null ? name : existing.name)
                .description(description != null ? description : existing.description)
                .createdAt(existing.createdAt)
                .createdBy(existing.createdBy)
                .updatedAt(Instant.now())
                .updatedBy(UserRef.builder().id(updatedBy).build())
                .build();
    }
}
