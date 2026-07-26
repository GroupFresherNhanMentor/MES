package fpt.qn.mes.bom.domain.entities;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter @Builder @FieldDefaults(level = AccessLevel.PRIVATE)
public class Bom {
    UUID id; UUID finishedProductId; Integer version; UUID bomStatusId;
    UUID createdBy; Instant createdAt; List<BomItem> items;

    public static Bom create(UUID finishedProductId, Integer version, UUID bomStatusId, UUID createdBy) {
        throw new UnsupportedOperationException("Not implemented");
    }
}
