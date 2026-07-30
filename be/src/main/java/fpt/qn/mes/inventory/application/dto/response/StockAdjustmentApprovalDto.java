package fpt.qn.mes.inventory.application.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

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
public class StockAdjustmentApprovalDto {

    UUID id;
    ProductSummaryDto product;
    WarehouseSummaryDto warehouse;
    WarehouseLocationSummaryDto location;
    UUID stockBalanceId;
    BigDecimal quantityAdjustment;
    String reason;
    String referenceNo;
    UserSummaryDto creator;
    Instant createdAt;
}
