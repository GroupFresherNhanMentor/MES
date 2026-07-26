package fpt.qn.mes.inventory.application.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StockMovementDto {
    UUID id;
    UUID movementTypeId;
    UUID productId;
    UUID lotId;
    UUID warehouseId;
    UUID locationId;
    BigDecimal quantity;
    UUID fromStatusId;
    UUID toStatusId;
    String referenceType;
    UUID referenceId;
    String reason;
    UUID createdBy;
    Instant createdAt;
}
