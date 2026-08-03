package fpt.qn.mes.maintenance.presentation;

import java.util.List;
import java.util.UUID;

import fpt.qn.mes.common.dto.response.ApiResponse;
import fpt.qn.mes.maintenance.application.dto.response.MachineDowntimeResponse;
import fpt.qn.mes.maintenance.application.dto.response.MaintenanceEngineerResponse;
import fpt.qn.mes.maintenance.application.dto.response.MaintenanceTicketResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import fpt.qn.mes.common.dto.response.PageResponse;
import fpt.qn.mes.maintenance.application.dto.request.MaintenanceTicketSearchQuery;
import fpt.qn.mes.maintenance.application.dto.response.MaintenanceMetadataResponse;
import fpt.qn.mes.maintenance.application.port.in.MaintenanceQueryUseCase;
import fpt.qn.mes.maintenance.domain.entities.MachineDowntime;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/api/maintenance")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Maintenance Queries", description = "Các API tra cứu danh sách, tìm kiếm và lấy dữ liệu cấu hình")
public class MaintenanceQueryController {

    MaintenanceQueryUseCase maintenanceQueryUseCase;

    @GetMapping
    @Operation(summary = "Lấy danh sách ticket phân trang")
    @PreAuthorize("hasAnyRole('MAINTENANCE_ENGINEER', 'OPERATOR', 'PLANNER', 'FACTORY_MANAGER', 'ADMIN')")
    public ResponseEntity<PageResponse<MaintenanceTicketResponse>> getTickets(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResponse<MaintenanceTicketResponse> response = maintenanceQueryUseCase.getTickets(page, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    @Operation(summary = "Tìm kiếm ticket theo bộ lọc động")
    @PreAuthorize("hasAnyRole('MAINTENANCE_ENGINEER', 'OPERATOR', 'PLANNER', 'FACTORY_MANAGER', 'ADMIN')")
    public ResponseEntity<PageResponse<MaintenanceTicketResponse>> searchTickets(
            MaintenanceTicketSearchQuery query,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResponse<MaintenanceTicketResponse> response = maintenanceQueryUseCase.searchTickets(query, page, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/downtimes")
    @Operation(summary = "Lấy lịch sử downtime của toàn bộ máy móc")
    @PreAuthorize("hasAnyRole('MAINTENANCE_ENGINEER', 'OPERATOR', 'PLANNER', 'FACTORY_MANAGER', 'ADMIN')")
    public ResponseEntity<List<MachineDowntime>> getAllDowntimes() {
        return ResponseEntity.ok(maintenanceQueryUseCase.getAllDowntimes());
    }

    @GetMapping("/ticket-statuses")
    @Operation(summary = "Lấy danh mục các trạng thái của ticket bảo trì")
    @PreAuthorize("hasAnyRole('MAINTENANCE_ENGINEER', 'OPERATOR', 'PLANNER', 'FACTORY_MANAGER', 'ADMIN')")
    public ResponseEntity<List<MaintenanceMetadataResponse>> getAllTicketStatuses() {
        return ResponseEntity.ok(maintenanceQueryUseCase.getAllTicketStatuses());
    }

    @GetMapping("/ticket-priorities")
    @Operation(summary = "Lấy danh mục mức độ ưu tiên")
    @PreAuthorize("hasAnyRole('MAINTENANCE_ENGINEER', 'OPERATOR', 'PLANNER', 'FACTORY_MANAGER', 'ADMIN')")
    public ResponseEntity<List<MaintenanceMetadataResponse>> getAllTicketPriorities() {
        return ResponseEntity.ok(maintenanceQueryUseCase.getAllTicketPriorities());
    }

    @GetMapping("/ticket-types")
    @Operation(summary = "Lấy danh mục các loại hình bảo trì")
    @PreAuthorize("hasAnyRole('MAINTENANCE_ENGINEER', 'OPERATOR', 'PLANNER', 'FACTORY_MANAGER', 'ADMIN')")
    public ResponseEntity<List<MaintenanceMetadataResponse>> getAllTicketTypes() {
        return ResponseEntity.ok(maintenanceQueryUseCase.getAllTicketTypes());
    }

    @GetMapping("/engineers")
    @Operation(summary = "Lấy danh sách maintenance engineer")
    @PreAuthorize("hasAnyRole('MAINTENANCE_ENGINEER', 'OPERATOR', 'PLANNER', 'FACTORY_MANAGER', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<MaintenanceEngineerResponse>>> getMaintenanceEngineers() {
        List<MaintenanceEngineerResponse> engineers = maintenanceQueryUseCase.getMaintenanceEngineers();
        return ResponseEntity.ok(ApiResponse.success(engineers, "Maintenance engineers retrieved"));
    }

    @GetMapping("/{ticketId}/downtime")
    @PreAuthorize("hasAnyRole('MAINTENANCE_ENGINEER', 'OPERATOR', 'PLANNER', 'FACTORY_MANAGER', 'ADMIN')")
    @Operation(summary = "Lấy chi tiết bản ghi downtime theo Ticket ID", description = "Trả về thông tin dừng máy duy nhất gắn liền với ticket này")
    public ResponseEntity<ApiResponse<MachineDowntimeResponse>> getDowntimeByTicketId(@PathVariable UUID ticketId) {
        MachineDowntimeResponse downtimeDto = maintenanceQueryUseCase.getDowntimeByTicketId(ticketId);
        return ResponseEntity.ok(ApiResponse.success(downtimeDto, "Lấy thông tin downtime thành công."));
    }
}