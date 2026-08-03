package fpt.qn.mes.audit.presentation;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fpt.qn.mes.audit.application.dto.AuditLogResponse;
import fpt.qn.mes.audit.application.dto.AuditLogSearchRequest;
import fpt.qn.mes.audit.application.port.in.AuditLogUseCase;
import fpt.qn.mes.common.dto.response.ApiResponse;
import fpt.qn.mes.common.dto.response.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/api/audit-logs")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Audit Logs", description = "Query immutable audit logs for critical actions")
@SecurityRequirement(name = "bearerAuth")
public class AuditLogController {

    AuditLogUseCase auditLogUseCase;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'FACTORY_MANAGER', 'AUDITOR')")
    @Operation(summary = "Search audit logs", description = "Search and filter immutable audit log records with pagination")
    public ResponseEntity<ApiResponse<PageResponse<AuditLogResponse>>> getAuditLogs(
            @ModelAttribute AuditLogSearchRequest request) {
        PageResponse<AuditLogResponse> page = auditLogUseCase.getAuditLogs(request);
        return ResponseEntity.ok(ApiResponse.success(page,"OK"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FACTORY_MANAGER', 'AUDITOR')")
    @Operation(summary = "Get audit log details", description = "Retrieve a specific audit log record by ID")
    public ResponseEntity<ApiResponse<AuditLogResponse>> getAuditLogById(
            @PathVariable UUID id) {
        AuditLogResponse dto = auditLogUseCase.getAuditLogById(id);
        return ResponseEntity.ok(ApiResponse.success(dto,"OK"));
    }
}
