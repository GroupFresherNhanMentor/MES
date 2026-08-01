package fpt.qn.mes.inventory.infrastructure.persistence;

import static fpt.qn.mes.jooq.Tables.WAREHOUSES;

import java.util.UUID;

import org.jooq.DSLContext;
import org.springframework.stereotype.Component;

import fpt.qn.mes.inventory.application.port.out.WarehouseCheckPort;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class WarehouseCheckAdapter implements WarehouseCheckPort {

    DSLContext ctx;

    @Override
    public boolean existsById(UUID warehouseId) {
        return ctx.fetchExists(WAREHOUSES, WAREHOUSES.ID.eq(warehouseId));
    }
}
