package fpt.qn.mes.maintenance.application.port.in;

import java.util.UUID;

import fpt.qn.mes.common.dto.response.PageResponse;
import fpt.qn.mes.maintenance.application.dto.request.*;
import fpt.qn.mes.maintenance.application.dto.response.*;

public interface MaintenanceUseCase {
    PageResponse<MaintenanceTicketResponse> getTickets(int page, int size);
    MaintenanceTicketResponse getTicketById(UUID id);
    MaintenanceTicketResponse createTicket(CreateMaintenanceTicketRequest request, UUID currentUserId);
    MaintenanceTicketResponse updateTicket(UUID id, UpdateMaintenanceTicketRequest request);
    void deleteTicket(UUID id);
    PageResponse<MachineDowntimeResponse> getDowntimes(UUID ticketId, int page, int size);
    MachineDowntimeResponse addDowntime(UUID ticketId, CreateDowntimeRequest request);
}
