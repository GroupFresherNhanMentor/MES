package fpt.qn.mes.workorder.application.port.out;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface WorkOrderReservationPort {
    List<ReservationStock> findAvailableStock(UUID warehouseId, Collection<UUID> productIds, UUID availableStatusId);

    UUID findStockStatusId(String statusName);

    UUID findMovementTypeId(String movementTypeName);

    void applyReservation(UUID workOrderId, List<ReservationAllocation> allocations,
            Map<UUID, java.math.BigDecimal> reservedByProduct,
            UUID availableStatusId, UUID reservedStatusId, UUID reserveMovementTypeId, UUID actorId);
}
