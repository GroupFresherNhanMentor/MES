package fpt.qn.mes.inventory.infrastructure.persistence;

import static fpt.qn.mes.jooq.Tables.WAREHOUSE_LOCATIONS;

import java.util.UUID;

import org.jooq.DSLContext;
import org.springframework.stereotype.Component;

import fpt.qn.mes.inventory.application.port.out.WarehouseLocationQueryPort;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class WarehouseLocationQueryAdapter implements WarehouseLocationQueryPort {

    DSLContext ctx;

    @Override
    public boolean belongsToWarehouse(UUID locationId, UUID warehouseId) {
        return ctx.fetchExists(WAREHOUSE_LOCATIONS,
                WAREHOUSE_LOCATIONS.ID.eq(locationId).and(WAREHOUSE_LOCATIONS.WAREHOUSE_ID.eq(warehouseId)));
    }
}
