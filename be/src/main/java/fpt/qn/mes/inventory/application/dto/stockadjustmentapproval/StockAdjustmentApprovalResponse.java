package fpt.qn.mes.inventory.application.dto.stockadjustmentapproval;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import fpt.qn.mes.inventory.application.dto.product.ProductResponse;
import fpt.qn.mes.inventory.application.dto.user.UserResponse;
import fpt.qn.mes.inventory.application.dto.warehouse.WarehouseLocationResponse;
import fpt.qn.mes.inventory.application.dto.warehouse.WarehouseResponse;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StockAdjustmentApprovalResponse {

    UUID id;
    ProductResponse product;
    WarehouseResponse warehouse;
    WarehouseLocationResponse location;
    UUID stockBalanceId;
    BigDecimal quantityAdjustment;
    String reason;
    String referenceNo;
    UserResponse creator;
    Instant createdAt;
}
