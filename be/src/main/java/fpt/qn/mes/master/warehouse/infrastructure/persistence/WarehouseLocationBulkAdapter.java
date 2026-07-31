package fpt.qn.mes.master.warehouse.infrastructure.persistence;

import static fpt.qn.mes.jooq.Tables.LOCATION_STATUSES;
import static fpt.qn.mes.jooq.Tables.WAREHOUSE_LOCATIONS;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import fpt.qn.mes.master.location.domain.constants.LocationStatusConstants;
import fpt.qn.mes.master.warehouse.application.port.out.WarehouseLocationPort;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Repository
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class WarehouseLocationBulkAdapter implements WarehouseLocationPort {

    DSLContext ctx;

    @Override
    public void deactivateAllByWarehouseId(UUID warehouseId, UUID updatedBy) {
        UUID inactiveStatusId = ctx.select(LOCATION_STATUSES.ID)
                .from(LOCATION_STATUSES)
                .where(LOCATION_STATUSES.NAME.eq(LocationStatusConstants.INACTIVE))
                .fetchOneInto(UUID.class);

        if (inactiveStatusId == null) return;

        ctx.update(WAREHOUSE_LOCATIONS)
                .set(WAREHOUSE_LOCATIONS.LOCATION_STATUS_ID, inactiveStatusId)
                .set(WAREHOUSE_LOCATIONS.UPDATED_AT, OffsetDateTime.now(ZoneOffset.UTC))
                .set(WAREHOUSE_LOCATIONS.UPDATED_BY, updatedBy)
                .where(WAREHOUSE_LOCATIONS.WAREHOUSE_ID.eq(warehouseId))
                .execute();
    }
}
