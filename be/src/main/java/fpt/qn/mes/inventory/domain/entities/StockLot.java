package fpt.qn.mes.inventory.domain.entities;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

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
    UUID lotTypeId;
    LotType lotType;
    LocalDate expiryDate;
    Instant createdAt;

    public static StockLot create(String lotNumber, UUID productId, UUID lotTypeId, LocalDate expiryDate) {
        if (lotNumber == null || lotNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Lot number cannot be empty or null");
        }
        if (productId == null) {
            throw new IllegalArgumentException("Product ID cannot be null");
        }
        return StockLot.builder()
                .id(UUID.randomUUID())
                .lotNumber(lotNumber.trim())
                .productId(productId)
                .lotTypeId(lotTypeId)
                .expiryDate(expiryDate)
                .createdAt(Instant.now())
                .build();
    }
}
