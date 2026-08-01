package fpt.qn.mes.inventory.application.port.out;

import java.util.UUID;

public interface WarehouseLocationQueryPort {
    boolean belongsToWarehouse(UUID locationId, UUID warehouseId);
}
