package fpt.qn.mes.master.product.domain.entities;

import java.time.Instant;
import java.util.UUID;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Product {

    UUID id;
    String code;
    String name;
    UUID productTypeId;
    String productTypeName;
    UUID unitId;
    String unitName;
    UUID productStatusId;
    String productStatusName;
    Long version;
    Instant createdAt;
    UUID createdBy;
    Instant updatedAt;
    UUID updatedBy;

    public static Product create(String code, String name, UUID productTypeId, UUID unitId, UUID productStatusId, UUID createdBy) {
        return Product.builder()
                .id(UUID.randomUUID())
                .code(code)
                .name(name)
                .productTypeId(productTypeId)
                .unitId(unitId)
                .productStatusId(productStatusId)
                .version(0L)
                .createdBy(createdBy)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }
}
