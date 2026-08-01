package fpt.qn.mes.inventory.domain.entities;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import fpt.qn.mes.common.exception.DomainException;
import fpt.qn.mes.common.util.UuidV7;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StockLot {

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class ProductRef {
        UUID id;
        String code;
        String name;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class UserRef {
        UUID id;
        String fullName;
        String username;
    }

    UUID id;
    String lotNumber;
    ProductRef product;
    LotType lotType;
    LocalDate expiryDate;
    Instant createdAt;
    UserRef createdBy;
    Instant updatedAt;
    UserRef updatedBy;

    public static StockLot create(String lotNumber, UUID productId, UUID lotTypeId, LocalDate expiryDate, UUID createdBy) {
        if (lotNumber == null || lotNumber.trim().isEmpty()) {
            throw new DomainException("Lot number cannot be empty or null");
        }
        if (productId == null) {
            throw new DomainException("Product ID cannot be null");
        }
        LotType lotType = lotTypeId != null ? LotType.builder().id(lotTypeId).build() : null;
        return StockLot.builder()
                .id(UuidV7.generate())
                .lotNumber(lotNumber.trim())
                .product(ProductRef.builder().id(productId).build())
                .lotType(lotType)
                .expiryDate(expiryDate)
                .createdAt(Instant.now())
                .createdBy(createdBy != null ? UserRef.builder().id(createdBy).build() : null)
                .build();
    }

    public UUID getProductId() {
        return product != null ? product.getId() : null;
    }

    public UUID getLotTypeId() {
        return lotType != null ? lotType.getId() : null;
    }
}
