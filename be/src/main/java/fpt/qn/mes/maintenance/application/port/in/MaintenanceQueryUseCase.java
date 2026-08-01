package fpt.qn.mes.maintenance.application.port.in;

import java.util.List;
import java.util.UUID;
import fpt.qn.mes.common.dto.response.PageResponse;
import fpt.qn.mes.maintenance.application.dto.request.MaintenanceTicketSearchQuery;
import fpt.qn.mes.maintenance.application.dto.response.MachineDowntimeResponse;
import fpt.qn.mes.maintenance.application.dto.response.MaintenanceTicketResponse;
import fpt.qn.mes.maintenance.application.dto.response.MaintenanceMetadataResponse;
import fpt.qn.mes.maintenance.domain.entities.MachineDowntime;

public interface MaintenanceQueryUseCase {
    PageResponse<MaintenanceTicketResponse> getTickets(int page, int size);
    PageResponse<MaintenanceTicketResponse> searchTickets(MaintenanceTicketSearchQuery query, int page, int size);
    List<MachineDowntime> getAllDowntimes();
    List<MaintenanceMetadataResponse> getAllTicketStatuses();
    List<MaintenanceMetadataResponse> getAllTicketPriorities();
    List<MaintenanceMetadataResponse> getAllTicketTypes();
    MachineDowntimeResponse getDowntimeByTicketId(UUID ticketId);
}