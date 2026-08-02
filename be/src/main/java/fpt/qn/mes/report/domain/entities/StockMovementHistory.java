package fpt.qn.mes.report.domain.entities;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StockMovementHistory {
    UUID movementId;
    Instant movementTime;
    String movementType;
    UUID productId;
    String productCode;
    String productName;
    String lotNumber;
    String fromWarehouse;
    String fromLocation;
    String toWarehouse;
    String toLocation;
    BigDecimal quantity;
    String referenceType;
    UUID referenceId;
    String createdBy;
    String reason;
}
