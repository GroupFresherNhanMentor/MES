package fpt.qn.mes.maintenance.application.service;

import java.util.List;
import java.util.UUID;

import fpt.qn.mes.common.dto.response.PageResponse;
import fpt.qn.mes.maintenance.application.dto.request.MaintenanceTicketSearchQuery;
import fpt.qn.mes.maintenance.application.dto.response.MachineDowntimeResponse;
import fpt.qn.mes.maintenance.application.dto.response.MaintenanceEngineerResponse;
import fpt.qn.mes.maintenance.application.dto.response.MaintenanceMetadataResponse;
import fpt.qn.mes.maintenance.application.dto.response.MaintenanceTicketResponse;
import fpt.qn.mes.maintenance.application.exception.ResourceNotFoundException;
import fpt.qn.mes.maintenance.application.mapper.MaintenanceDtoMapper;
import fpt.qn.mes.maintenance.application.port.in.MaintenanceQueryUseCase;
import fpt.qn.mes.maintenance.domain.entities.MachineDowntime;
import fpt.qn.mes.maintenance.domain.entities.MaintenanceTicket;
import fpt.qn.mes.maintenance.domain.repository.MaintenanceRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional(readOnly = true)
public class MaintenanceQueryService implements MaintenanceQueryUseCase {

    MaintenanceRepository repository;
    MaintenanceDtoMapper mapper;

    @Override
    public PageResponse<MaintenanceTicketResponse> getTickets(int page, int size) {
        int offset = (page - 1) * size;
        List<MaintenanceTicket> entities = repository.findAllPaged(offset, size);
        long totalElements = repository.countAll();
        int totalPages = size > 0 ? (int) Math.ceil((double) totalElements / size) : 0;

        List<MaintenanceTicketResponse> dtos = entities.stream()
                .map(mapper::toDto)
                .toList();

        return PageResponse.<MaintenanceTicketResponse>builder()
                .items(dtos)
                .pageNumber(page)
                .pageSize(size)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .build();
    }

    @Override
    public PageResponse<MaintenanceTicketResponse> searchTickets(MaintenanceTicketSearchQuery query, int page, int size) {
        int offset = (page - 1) * size;
        List<MaintenanceTicket> entities = repository.findByCriteria(query, offset, size);
        long totalElements = repository.countByCriteria(query);
        int totalPages = size > 0 ? (int) Math.ceil((double) totalElements / size) : 0;

        List<MaintenanceTicketResponse> dtos = entities.stream()
                .map(mapper::toDto)
                .toList();

        return PageResponse.<MaintenanceTicketResponse>builder()
                .items(dtos)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .pageNumber(page)
                .pageSize(size)
                .build();
    }

    @Override
    public List<MachineDowntime> getAllDowntimes() {
        return repository.findAllDowntimes();
    }

    @Override
    public List<MaintenanceMetadataResponse> getAllTicketStatuses() {
        return repository.findAllTicketStatuses();
    }

    @Override
    public List<MaintenanceMetadataResponse> getAllTicketPriorities() {
        return repository.findAllTicketPriorities();
    }

    @Override
    public List<MaintenanceMetadataResponse> getAllTicketTypes() {
        return repository.findAllTicketTypes();
    }

    @Override
    public List<MaintenanceEngineerResponse> getMaintenanceEngineers() {
        return repository.findMaintenanceEngineers();
    }

    @Override
    public MachineDowntimeResponse getDowntimeByTicketId(UUID ticketId) {
        // Sửa thành findDowntimeByTicketId để lấy ra bản ghi bất kể trạng thái đóng/mở
        MachineDowntime downtime = repository.findDowntimeByTicketId(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bản ghi downtime nào cho ticket ID: " + ticketId));

        return MachineDowntimeResponse.builder()
                .id(downtime.getId())
                .ticketId(downtime.getTicketId())
                .machineId(downtime.getMachineId())
                .startTime(downtime.getStartTime())
                .endTime(downtime.getEndTime())
                .totalDowntimeMinutes(downtime.getTotalDowntimeMinutes())
                .rootCause(downtime.getRootCause())
                .actionTaken(downtime.getActionTaken())
                .build();
    }
}