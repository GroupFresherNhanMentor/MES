package fpt.qn.mes.maintenance.application.service;

import java.time.Instant;
import java.util.UUID;

import fpt.qn.mes.auth.application.port.out.CurrentUserPort;
import fpt.qn.mes.maintenance.application.exception.BusinessException;
import fpt.qn.mes.maintenance.application.exception.MaintenanceTicketNotFoundException;
import fpt.qn.mes.maintenance.application.exception.ResourceNotFoundException;
import fpt.qn.mes.maintenance.domain.entities.MaintenanceTicket;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fpt.qn.mes.common.dto.response.PageResponse;
import fpt.qn.mes.maintenance.application.dto.request.CreateDowntimeRequest;
import fpt.qn.mes.maintenance.application.dto.request.CreateMaintenanceTicketRequest;
import fpt.qn.mes.maintenance.application.dto.response.MachineDowntimeDto;
import fpt.qn.mes.maintenance.application.dto.response.MaintenanceTicketDto;
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
    private final CurrentUserPort currentUserPort;
    MaintenanceDtoMapper mapper;

    @Override @Transactional(readOnly = true)
    public PageResponse<MaintenanceTicketDto> getTickets(int page, int size) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override @Transactional(readOnly = true)
    public MaintenanceTicketDto getTicketById(UUID id) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override @Transactional
    public MaintenanceTicketDto createTicket(CreateMaintenanceTicketRequest request) {
        // 1. Lấy thông tin User hiện tại từ Port (Đang dùng Mock hoặc Real JWT)
        UUID currentUserId = currentUserPort.getCurrentUserId();

        // 2. Lấy động Status ID "OPEN" cho Ticket
        UUID openStatusId = repository.findStatusIdByName("OPEN")
                .orElseThrow(() -> new ResourceNotFoundException("Status OPEN không tồn tại trên hệ thống"));

        // 3. Lấy động Status ID "DOWN" cho Machine
        UUID downStatusId = repository.findStatusIdByName("DOWN")
                .orElseThrow(() -> new ResourceNotFoundException("Status DOWN không tồn tại trên hệ thống"));

        // 4. Kiểm tra xem máy có tồn tại hay không trước khi xử lý
        // (Giả định bạn có hàm check này hoặc hàm update bên dưới sẽ check)

        // 5. Cập nhật trạng thái Machine sang DOWN luôn
        repository.updateMachineStatus(request.getMachineId(), downStatusId);

        // 6. Khởi tạo Domain Entity và Map data từ Request
        MaintenanceTicket ticket = MaintenanceTicket.builder()
                .id(UUID.randomUUID())
                .machineId(request.getMachineId())
                .ticketTypeId(request.getTicketTypeId())
                .priorityId(request.getPriorityId())
                .description(request.getDescription())
                .ticketStatusId(openStatusId)
                .createdBy(currentUserId)
                .createdAt(Instant.now())
                .build();

        // 7. Lưu Ticket mới vào DB
        repository.save(ticket);

        // 8. Map Entity sang DTO để trả về cho Controller
        return mapper.toDto(ticket);
    }

    @Override @Transactional
    public MaintenanceTicketDto updateTicket(UUID id, UpdateMaintenanceTicketRequest req) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override @Transactional
    public void deleteTicket(UUID id) {}

    @Override @Transactional(readOnly = true)
    public PageResponse<MachineDowntimeDto> getDowntimes(UUID ticketId, int page, int size) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override @Transactional
    public MachineDowntimeDto addDowntime(UUID ticketId, CreateDowntimeRequest req) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    @Transactional
    public void cancelTicket(UUID ticketId) {
        // 1. Lấy trạng thái hệ thống qua code
        UUID openStatusId = repository.findStatusIdByName("OPEN")
                .orElseThrow(() -> new BusinessException("Trạng thái OPEN không tồn tại"));
        UUID cancelledStatusId = repository.findStatusIdByName("CANCELLED")
                .orElseThrow(() -> new BusinessException("Trạng thái CANCELLED không tồn tại"));

        // 2. Tải toàn bộ Entity lên thay vì chỉ lấy ID trạng thái (Chuẩn DDD)
        MaintenanceTicket ticket = repository.findById(ticketId)
                .orElseThrow(() -> new MaintenanceTicketNotFoundException("Không tìm thấy ticket"));

        // 3. Ủy thác logic kiểm tra và thay đổi trạng thái cho Entity xử lý
        ticket.cancel(openStatusId, cancelledStatusId);

        // 4. Lưu lại Entity vào DB qua Persistence Adapter
        repository.saveStatus(ticket.getId(), ticket.getTicketStatusId());
    }
}
