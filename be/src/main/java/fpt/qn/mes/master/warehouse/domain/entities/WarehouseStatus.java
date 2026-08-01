package fpt.qn.mes.master.warehouse.domain.entities;

import java.util.UUID;
import fpt.qn.mes.common.util.UuidV7;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class WarehouseStatus {
    UUID id;
    String name;
    String description;

    public static WarehouseStatus create(String name, String description) {
        return WarehouseStatus.builder()
            .id(UuidV7.generate())
            .name(name)
            .description(description)
            .build();
    }
}
