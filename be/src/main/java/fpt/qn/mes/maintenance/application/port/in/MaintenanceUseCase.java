package fpt.qn.mes.maintenance.application.port.in;

import java.util.UUID;
import fpt.qn.mes.maintenance.application.dto.request.CreateMaintenanceTicketRequest;
import fpt.qn.mes.maintenance.application.dto.request.StartMaintenanceTicketRequest;
import fpt.qn.mes.maintenance.application.dto.request.CloseMaintenanceTicketRequest;
import fpt.qn.mes.maintenance.application.dto.response.MaintenanceTicketResponse;

public interface MaintenanceUseCase {
    MaintenanceTicketResponse createTicket(CreateMaintenanceTicketRequest request);
    MaintenanceTicketResponse startTicket(UUID ticketId, StartMaintenanceTicketRequest request);
    void cancelTicket(UUID ticketId);
    void resolveTicket(UUID ticketId);
    void closeTicket(CloseMaintenanceTicketRequest request);
}