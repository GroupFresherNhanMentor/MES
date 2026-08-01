package fpt.qn.mes.bom.domain.entities;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
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
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Bom {

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
    UUID finishedProductId;
    String finishedProductCode;
    String finishedProductName;
    Integer version;
    BomStatus bomStatus;
    UserRef createdBy;
    Instant createdAt;
    List<BomItem> items;

    public static Bom create(UUID finishedProductId, Integer version, UUID bomStatusId, UUID createdBy) {
        return Bom.builder()
                .id(UuidV7.generate())
                .finishedProductId(finishedProductId)
                .version(version)
                .bomStatus(BomStatus.builder().id(bomStatusId).build())
                .createdBy(UserRef.builder().id(createdBy).build())
                .createdAt(Instant.now())
                .items(new ArrayList<>())
                .build();
    }

    public static Bom changeStatus(Bom existing, UUID newStatusId) {
        return Bom.builder()
                .id(existing.id)
                .finishedProductId(existing.finishedProductId)
                .finishedProductCode(existing.finishedProductCode)
                .finishedProductName(existing.finishedProductName)
                .version(existing.version)
                .bomStatus(BomStatus.builder().id(newStatusId).build())
                .createdBy(existing.createdBy)
                .createdAt(existing.createdAt)
                .items(existing.items)
                .build();
    }
}
