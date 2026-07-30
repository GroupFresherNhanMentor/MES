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
    UUID id;
    String lotNumber;
    UUID productId;
    LotType lotType;
    LocalDate expiryDate;
    Instant createdAt;

    public static StockLot create(String lotNumber, UUID productId, UUID lotTypeId, LocalDate expiryDate) {
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
                .productId(productId)
                .lotType(lotType)
                .expiryDate(expiryDate)
                .createdAt(Instant.now())
                .build();
    }

    public UUID getLotTypeId() {
        return lotType != null ? lotType.getId() : null;
    }
}
