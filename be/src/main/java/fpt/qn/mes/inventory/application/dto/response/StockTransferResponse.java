package fpt.qn.mes.inventory.application.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import fpt.qn.mes.inventory.application.dto.stockmovement.StockMovementResponse;
import fpt.qn.mes.inventory.application.dto.stockbalance.StockBalanceResponse;


@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StockTransferResponse {
    StockMovementResponse transferOutMovement;
    StockMovementResponse transferInMovement;
    StockBalanceResponse sourceBalance;
    StockBalanceResponse destinationBalance;
}
