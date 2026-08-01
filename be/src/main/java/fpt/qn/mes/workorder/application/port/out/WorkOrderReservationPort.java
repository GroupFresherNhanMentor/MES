package fpt.qn.mes.workorder.application.port.out;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Outbound port interface for Work Order inventory reservation operations.
 * Interacts with persistence layer to query available stock across all active warehouses
 * and apply FIFO reservations.
 */
public interface WorkOrderReservationPort {

    /**
     * Finds available stock for specified products across all ACTIVE warehouses in pure FIFO date order.
     *
     * @param productIds Collection of required material product IDs
     * @param availableStatusId ID of AVAILABLE stock status
     * @return List of ReservationStock DTOs ordered by FIFO lot creation date
     */
    List<ReservationStock> findAvailableStock(Collection<UUID> productIds, UUID availableStatusId);

    /**
     * Finds stock status ID by name.
     */
    UUID findStockStatusId(String statusName);

    /**
     * Finds movement type ID by name.
     */
    UUID findMovementTypeId(String movementTypeName);

    /**
     * Applies material reservations, updates stock balances, updates work order material reserved quantities,
     * and records detailed stock movements.
     */
    void applyReservation(UUID workOrderId, List<ReservationAllocation> allocations,
            Map<UUID, java.math.BigDecimal> reservedByProduct,
            UUID availableStatusId, UUID reservedStatusId, UUID reserveMovementTypeId, UUID actorId);
}
