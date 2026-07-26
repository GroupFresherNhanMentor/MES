package fpt.qn.mes.inventory.domain.entities;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter @Builder @FieldDefaults(level = AccessLevel.PRIVATE)
public class StockBalance {
    UUID id; UUID warehouseId; UUID locationId; UUID productId; UUID lotId; UUID stockStatusId;
    BigDecimal quantity; Long version; Instant createdAt; Instant updatedAt;
}
