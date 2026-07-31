package fpt.qn.mes.inventory.application.dto.stocklot;

import java.util.UUID;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StockLotSummaryResponse {
    UUID id;
    String lotNumber;
}
