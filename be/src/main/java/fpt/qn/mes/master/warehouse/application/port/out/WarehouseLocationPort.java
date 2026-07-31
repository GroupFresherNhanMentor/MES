package fpt.qn.mes.master.warehouse.application.port.out;

import java.util.UUID;

public interface WarehouseLocationPort {
    void deactivateAllByWarehouseId(UUID warehouseId, UUID updatedBy);
}
