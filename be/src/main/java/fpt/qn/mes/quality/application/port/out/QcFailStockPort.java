package fpt.qn.mes.quality.application.port.out;

import java.math.BigDecimal;
import java.util.UUID;

public interface QcFailStockPort {
    void scrapStock(UUID lotId, UUID productId, BigDecimal quantity, UUID workOrderId, UUID inspectorId);
    void holdStock(UUID lotId, UUID productId, BigDecimal quantity, UUID workOrderId, UUID inspectorId);
}
