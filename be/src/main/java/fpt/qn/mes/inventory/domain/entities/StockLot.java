package fpt.qn.mes.inventory.domain.entities;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter @Builder @FieldDefaults(level = AccessLevel.PRIVATE)
public class StockLot {
    UUID id; String lotNumber; UUID productId; UUID lotTypeId; LocalDate expiryDate; Instant createdAt;

    public static StockLot create(String lotNumber, UUID productId, UUID lotTypeId, LocalDate expiryDate) {
        throw new UnsupportedOperationException("Not implemented");
    }
}
