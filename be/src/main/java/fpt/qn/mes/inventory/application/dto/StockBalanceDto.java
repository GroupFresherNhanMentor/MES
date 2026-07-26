package fpt.qn.mes.inventory.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter @Builder @NoArgsConstructor @AllArgsConstructor @FieldDefaults(level = AccessLevel.PRIVATE)
public class StockBalanceDto {
    UUID id; UUID warehouseId; UUID locationId; UUID productId; UUID lotId;
    UUID stockStatusId; BigDecimal quantity; Long version;
}
