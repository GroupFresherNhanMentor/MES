package fpt.qn.mes.master.product.domain.entities;

import java.time.Instant;
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
public class Product {

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserRef {
        private UUID id;
        private String fullName;
        private String username;
    }

    UUID id;
    String code;
    String name;
    ProductType productType;
    UnitOfMeasure unit;
    ProductStatus productStatus;
    String version;
    Instant createdAt;
    UserRef createdBy;
    Instant updatedAt;
    UserRef updatedBy;

    public static Product create(String code, String name, String version, UUID productTypeId, UUID unitId, UUID productStatusId, UUID createdBy) {
        return Product.builder()
                .id(UuidV7.generate())
                .code(code)
                .name(name)
                .productType(ProductType.builder().id(productTypeId).build())
                .unit(UnitOfMeasure.builder().id(unitId).build())
                .productStatus(ProductStatus.builder().id(productStatusId).build())
                .version(version)
                .createdBy(UserRef.builder().id(createdBy).build())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    public static Product changeStatus(Product existing, UUID productStatusId, UUID updatedBy) {
        return Product.builder()
                .id(existing.id)
                .code(existing.code)
                .name(existing.name)
                .productType(existing.productType)
                .unit(existing.unit)
                .productStatus(ProductStatus.builder().id(productStatusId).build())
                .version(existing.version)
                .createdAt(existing.createdAt)
                .createdBy(existing.createdBy)
                .updatedAt(Instant.now())
                .updatedBy(UserRef.builder().id(updatedBy).build())
                .build();
    }

    public static Product update(Product existing, String name, UUID productTypeId, UUID unitId, UUID updatedBy) {
        return Product.builder()
                .id(existing.id)
                .code(existing.code)
                .name(name != null ? name : existing.name)
                .productType(productTypeId != null ? ProductType.builder().id(productTypeId).build() : existing.productType)
                .unit(unitId != null ? UnitOfMeasure.builder().id(unitId).build() : existing.unit)
                .productStatus(existing.productStatus)
                .version(existing.version)
                .createdAt(existing.createdAt)
                .createdBy(existing.createdBy)
                .updatedAt(Instant.now())
                .updatedBy(UserRef.builder().id(updatedBy).build())
                .build();
    }
}
