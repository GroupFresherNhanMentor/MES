package fpt.qn.mes.inventory.application.port.out;

import java.util.UUID;

public interface WarehouseLocationCheckPort {
    boolean existsById(UUID locationId);
}
