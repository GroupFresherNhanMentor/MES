package fpt.qn.mes.inventory.domain.entities;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter @Builder @FieldDefaults(level = AccessLevel.PRIVATE)
public class StockMovement {
    UUID id; UUID movementTypeId; UUID productId; UUID lotId; UUID warehouseId; UUID locationId;
    BigDecimal quantity; UUID fromStatusId; UUID toStatusId;
    String referenceType; UUID referenceId; String reason; UUID createdBy; Instant createdAt;

    public static StockMovement create(UUID movementTypeId, UUID productId, UUID lotId, UUID warehouseId,
                                        UUID locationId, BigDecimal quantity, UUID fromStatusId, UUID toStatusId,
                                        String referenceType, UUID referenceId, String reason, UUID createdBy) {
        throw new UnsupportedOperationException("Not implemented");
    }
}
