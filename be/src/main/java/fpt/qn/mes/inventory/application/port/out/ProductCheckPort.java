package fpt.qn.mes.inventory.application.port.out;

import java.util.UUID;

public interface ProductCheckPort {
    boolean existsById(UUID productId);
}
