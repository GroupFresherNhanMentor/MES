package fpt.qn.mes.maintenance.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import fpt.qn.mes.maintenance.application.dto.request.MaintenanceTicketSearchQuery;
import fpt.qn.mes.common.domainQuery.PaginationResult;
import fpt.qn.mes.maintenance.application.dto.response.MaintenanceMetadataResponse;
import fpt.qn.mes.maintenance.domain.entities.MachineDowntime;
import fpt.qn.mes.maintenance.domain.entities.MaintenanceTicket;

public interface MaintenanceRepository {

    // =========================================================================
    // 1. LIFECYCLE MANAGEMENT: MAINTENANCE TICKET
    // =========================================================================
    Optional<MaintenanceTicket> findById(UUID id);
    MaintenanceTicket save(MaintenanceTicket ticket);
    void update(MaintenanceTicket ticket);

    // =========================================================================
    // 2. SEARCH & PAGINATION: MAINTENANCE TICKET
    // =========================================================================
    long countAll();
    List<MaintenanceTicket> findAllPaged(int offset, int limit);
    long countByCriteria(MaintenanceTicketSearchQuery query);
    List<MaintenanceTicket> findByCriteria(MaintenanceTicketSearchQuery query, int offset, int limit);

    // =========================================================================
    // 3. LIFECYCLE & PAGINATION: MACHINE DOWNTIME
    // =========================================================================
    void saveDowntime(MachineDowntime downtime);
    void updateDowntime(MachineDowntime downtime);
    List<MachineDowntime> findAllDowntimes();
    Optional<MachineDowntime> findActiveDowntimeByTicketId(UUID ticketId);
    Optional<MachineDowntime> findDowntimeByTicketId(UUID ticketId);

    // =========================================================================
    // 4. METADATA QUERIES (STATUS, PRIORITY, TYPE)
    // =========================================================================
    Optional<UUID> findStatusIdByName(String statusName);
    Optional<UUID> findStatusIdByTicketId(UUID ticketId);
    List<MaintenanceMetadataResponse> findAllTicketStatuses();
    List<MaintenanceMetadataResponse> findAllTicketPriorities();
    List<MaintenanceMetadataResponse> findAllTicketTypes();

    // =========================================================================
    // 5. EXTERNAL MODULE / SECURITY INTERACTION BOUNDS
    // =========================================================================
    boolean userHasRole(UUID userId, String roleName);
    Optional<UUID> findMachineStatusIdByName(String statusName);
    void updateMachineStatus(UUID machineId, UUID statusId);
}