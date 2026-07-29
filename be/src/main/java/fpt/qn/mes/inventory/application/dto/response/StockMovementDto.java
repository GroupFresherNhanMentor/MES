package fpt.qn.mes.inventory.application.dto.response;

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
    String referenceNo;
    String reason;
    UUID createdBy;
    Instant createdAt;
}
