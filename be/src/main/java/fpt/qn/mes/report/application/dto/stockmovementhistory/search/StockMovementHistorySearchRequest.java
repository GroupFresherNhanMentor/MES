package fpt.qn.mes.report.application.dto.stockmovementhistory.search;

import java.time.LocalDate;
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
public class StockMovementHistorySearchRequest extends BaseSearchRequest {
    UUID productId;
    UUID warehouseId;
    UUID movementTypeId;
    LocalDate fromDate;
    LocalDate toDate;
    String referenceType;
    UUID referenceId;
}
