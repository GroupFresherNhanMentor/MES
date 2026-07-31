package fpt.qn.mes.inventory.domain.entities;

import java.util.UUID;

import fpt.qn.mes.common.util.UuidV7;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LotType {
    UUID id;
    String name;
    String description;

    public static LotType create(String name, String description) {
        return LotType.builder()
                .id(UuidV7.generate())
                .name(name)
                .description(description)
                .build();
    }
}
