package fpt.qn.mes.workorder.application.port.out.dto;

import java.math.BigDecimal;
import java.util.UUID;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CompletionReservationAllocation {
    UUID materialProductId;
    UUID warehouseId;
    UUID locationId;
    UUID lotId;
    BigDecimal reservedQuantity;
    BigDecimal consumedQuantity;
    BigDecimal scrapQuantity;
}
