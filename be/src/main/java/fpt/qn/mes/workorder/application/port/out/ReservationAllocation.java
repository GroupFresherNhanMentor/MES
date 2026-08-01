package fpt.qn.mes.workorder.application.port.out;

import java.math.BigDecimal;
import java.util.UUID;

public record ReservationAllocation(
        UUID balanceId,
        UUID warehouseId,
        UUID locationId,
        UUID productId,
        UUID lotId,
        BigDecimal quantity) {
}
