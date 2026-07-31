package fpt.qn.mes.bom.domain.entities;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import fpt.qn.mes.common.util.UuidV7;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Bom {
    UUID id;
    UUID finishedProductId;
    String finishedProductCode;
    String finishedProductName;
    Integer version;
    UUID bomStatusId;
    String bomStatusName;
    UUID createdBy;
    Instant createdAt;
    List<BomItem> items;

    public static Bom create(UUID finishedProductId, Integer version, UUID bomStatusId, UUID createdBy) {
        return Bom.builder()
                .id(UuidV7.generate())
                .finishedProductId(finishedProductId)
                .version(version)
                .bomStatusId(bomStatusId)
                .createdBy(createdBy)
                .createdAt(Instant.now())
                .items(new ArrayList<>())
                .build();
    }

    public void updateStatus(UUID newStatusId) {
        this.bomStatusId = newStatusId;
    }
}
