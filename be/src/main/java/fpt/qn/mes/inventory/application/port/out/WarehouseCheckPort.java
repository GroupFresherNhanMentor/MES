package fpt.qn.mes.inventory.application.port.out;

import java.util.UUID;

public interface WarehouseCheckPort {
    boolean existsById(UUID warehouseId);
}
