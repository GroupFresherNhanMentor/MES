package fpt.qn.mes.quality.application.port.out;

import java.math.BigDecimal;
import java.util.UUID;

public interface QcReleasePort {
    void releasePassedStock(UUID lotId, UUID productId, BigDecimal quantity, UUID workOrderId, UUID inspectorId);
}
