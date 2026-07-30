package fpt.qn.mes.inventory.application.dto.stockmovement;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import fpt.qn.mes.inventory.application.dto.movementtype.MovementTypeResponse;
import fpt.qn.mes.inventory.application.dto.product.ProductResponse;
import fpt.qn.mes.inventory.application.dto.stocklot.StockLotSummaryResponse;
import fpt.qn.mes.inventory.application.dto.stockstatus.StockStatusResponse;
import fpt.qn.mes.inventory.application.dto.user.UserResponse;
import fpt.qn.mes.inventory.application.dto.warehouse.WarehouseLocationResponse;
import fpt.qn.mes.inventory.application.dto.warehouse.WarehouseResponse;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StockMovementResponse {
    UUID id;
    MovementTypeResponse movementType;
    ProductResponse product;
    StockLotSummaryResponse lot;
    WarehouseResponse fromWarehouse;
    WarehouseResponse toWarehouse;
    WarehouseLocationResponse fromLocation;
    WarehouseLocationResponse toLocation;
    BigDecimal quantity;
    StockStatusResponse fromStatus;
    StockStatusResponse toStatus;
    String referenceNo;
    String reason;
    UserResponse createdBy;
    Instant createdAt;
}
