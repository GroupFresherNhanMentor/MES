package fpt.qn.mes.workorder.application.port.out;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ReservationStock(
        UUID balanceId,
        UUID warehouseId,
        UUID locationId,
        UUID productId,
        UUID lotId,
        BigDecimal quantity,
        Instant lotCreatedAt) {
}
