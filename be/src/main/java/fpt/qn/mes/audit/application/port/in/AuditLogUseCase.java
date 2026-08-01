package fpt.qn.mes.audit.application.port.in;

import java.util.UUID;

import fpt.qn.mes.audit.application.dto.AuditLogResponse;
import fpt.qn.mes.audit.application.dto.AuditLogSearchRequest;
import fpt.qn.mes.common.dto.response.PageResponse;

public interface AuditLogUseCase {

    PageResponse<AuditLogResponse> getAuditLogs(AuditLogSearchRequest request);

    AuditLogResponse getAuditLogById(UUID id);
}
