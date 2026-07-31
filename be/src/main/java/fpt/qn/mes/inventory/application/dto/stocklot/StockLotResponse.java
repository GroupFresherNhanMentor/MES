package fpt.qn.mes.inventory.application.dto.stocklot;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StockLotResponse {
    UUID id;
    String lotNumber;
    UUID productId;
    UUID lotTypeId;
    LocalDate expiryDate;
    Instant createdAt;
}
