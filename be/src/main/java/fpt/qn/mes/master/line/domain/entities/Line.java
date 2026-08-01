package fpt.qn.mes.master.line.domain.entities;

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
public class Line {

    @Getter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class UserRef {
        private UUID id;
        private String fullName;
        private String username;
    }

    UUID id;
    String code;
    String name;
    LineStatus lineStatus;
    Instant createdAt;
    UserRef createdBy;
    Instant updatedAt;
    UserRef updatedBy;

    public static Line create(String code, String name, UUID lineStatusId, UUID createdBy) {
        return Line.builder()
                .id(UuidV7.generate())
                .code(code)
                .name(name)
                .lineStatus(LineStatus.builder().id(lineStatusId).build())
                .createdBy(UserRef.builder().id(createdBy).build())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    public static Line update(Line existing, String name, UUID lineStatusId, UUID updatedBy) {
        return Line.builder()
                .id(existing.id)
                .code(existing.code)
                .name(name != null ? name : existing.name)
                .lineStatus(lineStatusId != null ? LineStatus.builder().id(lineStatusId).build() : existing.lineStatus)
                .createdAt(existing.createdAt)
                .createdBy(existing.createdBy)
                .updatedAt(Instant.now())
                .updatedBy(UserRef.builder().id(updatedBy).build())
                .build();
    }

    public static Line activate(Line existing, UUID activeStatusId, UUID updatedBy) {
        return Line.builder()
                .id(existing.id)
                .code(existing.code)
                .name(existing.name)
                .lineStatus(LineStatus.builder().id(activeStatusId).build())
                .createdAt(existing.createdAt)
                .createdBy(existing.createdBy)
                .updatedAt(Instant.now())
                .updatedBy(UserRef.builder().id(updatedBy).build())
                .build();
    }

    public static Line deactivate(Line existing, UUID inactiveStatusId, UUID updatedBy) {
        return Line.builder()
                .id(existing.id)
                .code(existing.code)
                .name(existing.name)
                .lineStatus(LineStatus.builder().id(inactiveStatusId).build())
                .createdAt(existing.createdAt)
                .createdBy(existing.createdBy)
                .updatedAt(Instant.now())
                .updatedBy(UserRef.builder().id(updatedBy).build())
                .build();
    }
}
