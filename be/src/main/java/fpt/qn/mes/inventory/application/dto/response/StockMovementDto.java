package fpt.qn.mes.inventory.application.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StockMovementDto {
    UUID id;
    MovementTypeSummaryDto movementType;
    ProductSummaryDto product;
    StockLotSummaryDto lot;
    WarehouseSummaryDto fromWarehouse;
    WarehouseSummaryDto toWarehouse;
    LocationSummaryDto fromLocation;
    LocationSummaryDto toLocation;
    BigDecimal quantity;
    StockStatusSummaryDto fromStatus;
    StockStatusSummaryDto toStatus;
    String referenceNo;
    String reason;
    UserSummaryDto createdBy;
    Instant createdAt;
}
