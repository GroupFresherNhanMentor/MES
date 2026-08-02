package fpt.qn.mes.report.application.dto.inventorysummary.search;

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
public class InventorySummarySearchRequest extends BaseSearchRequest {
    UUID warehouseId;
    String productType;
    String productCode;
}
