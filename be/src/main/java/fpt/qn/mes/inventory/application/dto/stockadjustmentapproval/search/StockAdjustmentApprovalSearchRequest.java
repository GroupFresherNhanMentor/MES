package fpt.qn.mes.inventory.application.dto.stockadjustmentapproval.search;

import java.util.UUID;

import fpt.qn.mes.common.dto.request.BaseSearchRequest;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StockAdjustmentApprovalSearchRequest extends BaseSearchRequest {

    UUID productId;
    UUID warehouseId;
    UUID locationId;
    UUID stockBalanceId;
    UUID createdBy;
}
