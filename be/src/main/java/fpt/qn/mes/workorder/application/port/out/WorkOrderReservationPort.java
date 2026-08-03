package fpt.qn.mes.workorder.application.port.out;

import fpt.qn.mes.workorder.application.port.out.dto.ReservationAllocation;
import fpt.qn.mes.workorder.application.port.out.dto.ReservationStock;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface WorkOrderReservationPort {
    List<ReservationStock> findAvailableStock(Collection<UUID> productIds, UUID availableStatusId);

    UUID findStockStatusId(String statusName);

    UUID findMovementTypeId(String movementTypeName);

    void applyReservation(UUID workOrderId, List<ReservationAllocation> allocations,
            Map<UUID, BigDecimal> reservedByProduct,
            UUID availableStatusId, UUID reservedStatusId, UUID reserveMovementTypeId, UUID actorId);

    /**
     * Releases only the outstanding net reservation and returns false when stock data is inconsistent.
     */
    boolean releaseReservation(UUID workOrderId, UUID availableStatusId, UUID reservedStatusId,
            UUID releaseMovementTypeId, UUID actorId);
}
