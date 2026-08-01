package fpt.qn.mes.maintenance.presentation;

import java.util.UUID;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import fpt.qn.mes.common.dto.response.ApiResponse;
import fpt.qn.mes.maintenance.application.dto.request.*;
import fpt.qn.mes.maintenance.application.dto.response.MaintenanceTicketResponse;
import fpt.qn.mes.maintenance.application.port.in.MaintenanceUseCase;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/api/maintenance")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Maintenance Commands", description = "Các API thay đổi trạng thái và xử lý nghiệp vụ bảo trì")
public class MaintenanceController {

    MaintenanceUseCase maintenanceUseCase;

    @PostMapping
    @Operation(summary = "Tạo ticket bảo trì mới")
    @PreAuthorize("hasAnyRole('MAINTENANCE_ENGINEER', 'OPERATOR', 'PLANNER', 'FACTORY_MANAGER', 'ADMIN')")
    public ResponseEntity<ApiResponse<MaintenanceTicketResponse>> create(
            @Valid @RequestBody CreateMaintenanceTicketRequest req) {
        MaintenanceTicketResponse ticketDto = maintenanceUseCase.createTicket(req);
        ApiResponse<MaintenanceTicketResponse> response = ApiResponse.success(ticketDto, "Tạo ticket bảo trì thành công.");
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Hủy ticket bảo trì", description = "Chuyển trạng thái sang CANCELLED (Chỉ áp dụng cho ticket OPEN)")
    @PreAuthorize("hasAnyRole('MAINTENANCE_ENGINEER', 'OPERATOR', 'PLANNER', 'FACTORY_MANAGER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> cancelTicket(@PathVariable("id") UUID ticketId) {
        maintenanceUseCase.cancelTicket(ticketId);
        return ResponseEntity.ok(ApiResponse.success(null, "Hủy ticket thành công"));
    }

    @PostMapping("/{ticketId}/start")
    @Operation(summary = "Bắt đầu thực hiện bảo trì", description = "Gán kỹ sư và chuyển trạng thái ticket sang IN_PROGRESS, máy sang UNDER_MAINTENANCE")
    @PreAuthorize("hasAnyRole('MAINTENANCE_ENGINEER', 'FACTORY_MANAGER', 'ADMIN')")
    public ResponseEntity<ApiResponse<MaintenanceTicketResponse>> startTicket(
            @PathVariable UUID ticketId,
            @Valid @RequestBody StartMaintenanceTicketRequest request) {
        MaintenanceTicketResponse data = maintenanceUseCase.startTicket(ticketId, request);
        return ResponseEntity.ok(ApiResponse.success(data, "Maintenance ticket started successfully"));
    }

    @PostMapping("/{ticketId}/resolve")
    @PreAuthorize("hasAnyRole('MAINTENANCE_ENGINEER', 'FACTORY_MANAGER', 'ADMIN')")
    @Operation(summary = "Hoàn thành sửa chữa", description = "Chuyển trạng thái ticket sang RESOLVED để chờ nghiệm thu đóng")
    public ResponseEntity<ApiResponse<Void>> resolveTicket(@PathVariable UUID ticketId) {
        maintenanceUseCase.resolveTicket(ticketId);
        return ResponseEntity.ok(ApiResponse.success(null, "Maintenance ticket has been resolved successfully"));
    }

    @PostMapping("/{ticketId}/close")
    @PreAuthorize("hasAnyRole('MAINTENANCE_ENGINEER', 'FACTORY_MANAGER', 'ADMIN')")
    @Operation(summary = "Nghiệm thu và đóng ticket", description = "Cập nhật dữ liệu downtime, đóng ticket và đưa máy về trạng thái hoạt động")
    public ResponseEntity<ApiResponse<Void>> closeTicket(
            @PathVariable UUID ticketId,
            @Valid @RequestBody CloseMaintenanceTicketRequest request) {
        // Đảm bảo ticketId đồng bộ giữa Path và Body
        request.setTicketId(ticketId);
        maintenanceUseCase.closeTicket(request);
        return ResponseEntity.ok(ApiResponse.success(null, "Maintenance ticket closed successfully"));
    }
}