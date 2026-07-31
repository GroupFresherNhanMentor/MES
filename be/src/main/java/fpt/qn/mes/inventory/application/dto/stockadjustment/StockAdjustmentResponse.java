package fpt.qn.mes.inventory.application.dto.stockadjustment;

import java.util.UUID;

import fpt.qn.mes.inventory.application.dto.stockmovement.StockMovementResponse;
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
public class StockAdjustmentResponse {

    boolean requiresApproval;
    UUID approvalId;
    StockMovementResponse movement;
    String message;
}
