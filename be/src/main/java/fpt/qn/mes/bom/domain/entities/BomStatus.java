package fpt.qn.mes.bom.domain.entities;

import java.util.UUID;

import fpt.qn.mes.common.util.UuidV7;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BomStatus {
    UUID id;
    String name;
    String description;

    public static BomStatus create(String name, String description) {
        return BomStatus.builder()
                .id(UuidV7.generate())
                .name(name)
                .description(description)
                .build();
    }
}
