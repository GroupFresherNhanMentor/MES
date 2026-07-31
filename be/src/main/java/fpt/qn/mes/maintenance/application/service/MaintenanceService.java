package fpt.qn.mes.maintenance.application.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fpt.qn.mes.common.dto.response.PageResponse;
import fpt.qn.mes.maintenance.application.dto.request.CreateDowntimeRequest;
import fpt.qn.mes.maintenance.application.dto.request.CreateMaintenanceTicketRequest;
import fpt.qn.mes.maintenance.application.dto.response.MachineDowntimeResponse;
import fpt.qn.mes.maintenance.application.dto.response.MaintenanceTicketResponse;
import fpt.qn.mes.maintenance.application.dto.request.UpdateMaintenanceTicketRequest;
import fpt.qn.mes.maintenance.application.mapper.MaintenanceDtoMapper;
import fpt.qn.mes.maintenance.application.port.in.MaintenanceUseCase;
import fpt.qn.mes.maintenance.domain.repository.MaintenanceRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MaintenanceService implements MaintenanceUseCase {

    MaintenanceRepository repository;
    MaintenanceDtoMapper mapper;

    @Override @Transactional(readOnly = true)
    public PageResponse<MaintenanceTicketResponse> getTickets(int page, int size) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override @Transactional(readOnly = true)
    public MaintenanceTicketResponse getTicketById(UUID id) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override @Transactional
    public MaintenanceTicketResponse createTicket(CreateMaintenanceTicketRequest req, UUID currentUserId) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override @Transactional
    public MaintenanceTicketResponse updateTicket(UUID id, UpdateMaintenanceTicketRequest req) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override @Transactional
    public void deleteTicket(UUID id) {}

    @Override @Transactional(readOnly = true)
    public PageResponse<MachineDowntimeResponse> getDowntimes(UUID ticketId, int page, int size) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override @Transactional
    public MachineDowntimeResponse addDowntime(UUID ticketId, CreateDowntimeRequest req) {
        throw new UnsupportedOperationException("Not implemented");
    }
}
