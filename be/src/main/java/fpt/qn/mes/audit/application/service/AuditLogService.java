package fpt.qn.mes.audit.application.service;

import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import fpt.qn.mes.audit.application.dto.AuditLogResponse;
import fpt.qn.mes.audit.application.dto.AuditLogSearchRequest;
import fpt.qn.mes.audit.application.exception.AuditLogNotFoundException;
import fpt.qn.mes.audit.application.mapper.AuditLogDtoMapper;
import fpt.qn.mes.audit.application.port.in.AuditLogUseCase;
import fpt.qn.mes.audit.application.port.out.AuditLogPort;
import fpt.qn.mes.audit.domain.entities.AuditAction;
import fpt.qn.mes.audit.domain.entities.AuditLog;
import fpt.qn.mes.audit.domain.repository.AuditLogRepository;
import fpt.qn.mes.audit.domain.repository.criteria.AuditLogSearchCriteria;
import fpt.qn.mes.auth.application.port.out.CurrentUserPort;
import fpt.qn.mes.common.dto.response.PageResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuditLogService implements AuditLogPort, AuditLogUseCase {

    AuditLogRepository auditLogRepository;
    AuditLogDtoMapper mapper;
    CurrentUserPort currentUserPort;

    @Override
    @Transactional
    public AuditLog log(
            UUID actorId,
            AuditAction action,
            String entityType,
            UUID entityId,
            String oldValue,
            String newValue,
            String ipAddress) {
        UUID effectiveActorId = actorId;
        if (effectiveActorId == null) {
            try {
                effectiveActorId = currentUserPort.getCurrentUserId();
            } catch (Exception ignored) {
            }
        }
        AuditLog auditLog = AuditLog.create(
                effectiveActorId,
                action,
                entityType,
                entityId,
                oldValue,
                newValue,
                ipAddress
        );
        return auditLogRepository.save(auditLog);
    }

    @Override
    @Transactional
    public AuditLog log(
            AuditAction action,
            String entityType,
            UUID entityId,
            String oldValue,
            String newValue) {
        return log(null, action, entityType, entityId, oldValue, newValue, null);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AuditLogResponse> getAuditLogs(AuditLogSearchRequest request) {
        if (request == null) {
            request = AuditLogSearchRequest.builder().page(0).size(20).build();
        }
        AuditLogSearchCriteria criteria = AuditLogSearchCriteria.builder()
                .entityType(request.getEntityType())
                .entityId(request.getEntityId())
                .actorId(request.getActorId())
                .action(request.getAction())
                .from(request.getFrom())
                .to(request.getTo())
                .page(request.getPage())
                .size(request.getSize() > 0 ? request.getSize() : 20)
                .sort(request.getSort() != null ? request.getSort() : List.of())
                .build();

        long totalElements = auditLogRepository.count(criteria);
        List<AuditLog> items = auditLogRepository.search(criteria);
        List<AuditLogResponse> dtos = items.stream().map(mapper::toDto).toList();

        return PageResponse.of(dtos, totalElements, criteria.getPage(), criteria.getSize());
    }

    @Override
    @Transactional(readOnly = true)
    public AuditLogResponse getAuditLogById(UUID id) {
        AuditLog auditLog = auditLogRepository.findById(id)
                .orElseThrow(() -> new AuditLogNotFoundException("Audit log entry not found with ID: " + id));
        return mapper.toDto(auditLog);
    }
}
