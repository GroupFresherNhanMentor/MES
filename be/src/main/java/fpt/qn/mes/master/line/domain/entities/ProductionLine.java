package fpt.qn.mes.master.line.domain.entities;

import java.time.Instant;
import java.util.UUID;

import fpt.qn.mes.common.util.UuidV7;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter @Builder @FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductionLine {
    UUID id; String code; String name; UUID lineStatusId;
    Instant createdAt; UUID createdBy; Instant updatedAt; UUID updatedBy;

    public static ProductionLine create(String code, String name, UUID lineStatusId, UUID createdBy) {
        return ProductionLine.builder()
                .id(UuidV7.generate())
                .code(code)
                .name(name)
                .lineStatusId(lineStatusId)
                .createdBy(createdBy)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }
}
