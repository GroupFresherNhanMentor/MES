package fpt.qn.mes.maintenance.domain.repository;

import java.util.Optional;
import java.util.UUID;

import fpt.qn.mes.common.domainQuery.PaginationResult;
import fpt.qn.mes.maintenance.domain.entities.MachineDowntime;
import fpt.qn.mes.maintenance.domain.entities.MaintenanceTicket;

public interface MaintenanceRepository {
    Optional<MaintenanceTicket> findById(UUID id);
    MaintenanceTicket save(MaintenanceTicket ticket);
    MaintenanceTicket update(MaintenanceTicket ticket);
    void deleteById(UUID id);
    PaginationResult<MaintenanceTicket> findAll(int page, int size);

    MachineDowntime saveDowntime(MachineDowntime downtime);
    PaginationResult<MachineDowntime> findDowntimesByTicketId(UUID ticketId, int page, int size);
}
