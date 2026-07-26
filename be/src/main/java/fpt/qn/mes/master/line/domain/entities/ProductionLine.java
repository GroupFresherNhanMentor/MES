package fpt.qn.mes.master.line.domain.entities;

import java.time.Instant;
import java.util.UUID;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter @Builder @FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductionLine {
    UUID id; String code; String name; UUID lineStatusId;
    Instant createdAt; UUID createdBy; Instant updatedAt; UUID updatedBy;

    public static ProductionLine create(String code, String name, UUID lineStatusId, UUID createdBy) {
        throw new UnsupportedOperationException("Not implemented");
    }
}
