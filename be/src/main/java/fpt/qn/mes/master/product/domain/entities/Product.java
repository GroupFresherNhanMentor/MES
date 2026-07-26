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
    String unit;
    UUID productStatusId;
    Long version;
    Instant createdAt;
    UUID createdBy;
    Instant updatedAt;
    UUID updatedBy;

    public static Product create(String code, String name, UUID productTypeId, String unit, UUID productStatusId, UUID createdBy) {
        throw new UnsupportedOperationException("Not implemented");
    }
}
