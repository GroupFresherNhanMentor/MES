package fpt.qn.mes.maintenance.application.port.in;

import java.util.UUID;

import fpt.qn.mes.common.dto.response.PageResponse;
import fpt.qn.mes.maintenance.application.dto.request.*;
import fpt.qn.mes.maintenance.application.dto.response.*;

public interface MaintenanceUseCase {
    PageResponse<MaintenanceTicketDto> getTickets(int page, int size);
    MaintenanceTicketDto getTicketById(UUID id);
    MaintenanceTicketDto createTicket(CreateMaintenanceTicketRequest request, UUID currentUserId);
    MaintenanceTicketDto updateTicket(UUID id, UpdateMaintenanceTicketRequest request);
    void deleteTicket(UUID id);
    PageResponse<MachineDowntimeDto> getDowntimes(UUID ticketId, int page, int size);
    MachineDowntimeDto addDowntime(UUID ticketId, CreateDowntimeRequest request);
}
