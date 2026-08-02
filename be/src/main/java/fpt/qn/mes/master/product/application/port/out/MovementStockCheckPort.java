package fpt.qn.mes.master.product.application.port.out;

import java.util.UUID;

public interface MovementStockCheckPort {
    boolean hasStockMovements(UUID productId);
}
