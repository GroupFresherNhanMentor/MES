package fpt.qn.mes.audit.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import fpt.qn.mes.audit.application.dto.AuditLogResponse;
import fpt.qn.mes.audit.application.dto.AuditLogSearchRequest;
import fpt.qn.mes.audit.application.exception.AuditLogNotFoundException;
import fpt.qn.mes.audit.application.mapper.AuditLogDtoMapper;
import fpt.qn.mes.audit.application.service.AuditLogService;
import fpt.qn.mes.audit.domain.entities.AuditAction;
import fpt.qn.mes.audit.domain.entities.AuditLog;
import fpt.qn.mes.audit.domain.repository.AuditLogRepository;
import fpt.qn.mes.audit.domain.repository.criteria.AuditLogSearchCriteria;
import fpt.qn.mes.auth.application.port.out.CurrentUserPort;
import fpt.qn.mes.common.dto.response.PageResponse;

@ExtendWith(MockitoExtension.class)
class AuditLogServiceTest {

    @Mock
    AuditLogRepository auditLogRepository;

    @Mock
    AuditLogDtoMapper mapper;

    @Mock
    CurrentUserPort currentUserPort;

    @InjectMocks
    AuditLogService auditLogService;

    UUID actorId;
    UUID entityId;
    UUID logId;

    @BeforeEach
    void setUp() {
        actorId = UUID.randomUUID();
        entityId = UUID.randomUUID();
        logId = UUID.randomUUID();
    }

    @Test
    @DisplayName("log with explicit actorId records audit entry")
    void log_WithExplicitActor_SavesAuditLog() {
        AuditLog savedLog = AuditLog.builder()
                .id(logId)
                .actorId(actorId)
                .action(AuditAction.CREATE_WORK_ORDER)
                .entityType("WORK_ORDER")
                .entityId(entityId)
                .newValue("{\"code\":\"WO-001\"}")
                .createdAt(Instant.now())
                .build();

        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(savedLog);

        AuditLog result = auditLogService.log(actorId, AuditAction.CREATE_WORK_ORDER, "WORK_ORDER", entityId, null, "{\"code\":\"WO-001\"}", "127.0.0.1");

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(logId);
        verify(auditLogRepository).save(any(AuditLog.class));
    }

    @Test
    @DisplayName("log without explicit actorId resolves actorId from CurrentUserPort")
    void log_WithoutActor_ResolvesFromCurrentUserPort() {
        when(currentUserPort.getCurrentUserId()).thenReturn(actorId);

        AuditLog savedLog = AuditLog.builder()
                .id(logId)
                .actorId(actorId)
                .action(AuditAction.ADJUST_STOCK)
                .entityType("STOCK_BALANCE")
                .entityId(entityId)
                .oldValue("{\"quantity\":100}")
                .newValue("{\"quantity\":110}")
                .createdAt(Instant.now())
                .build();

        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(savedLog);

        AuditLog result = auditLogService.log(AuditAction.ADJUST_STOCK, "STOCK_BALANCE", entityId, "{\"quantity\":100}", "{\"quantity\":110}");

        assertThat(result).isNotNull();
        verify(currentUserPort).getCurrentUserId();
        verify(auditLogRepository).save(any(AuditLog.class));
    }

    @Test
    @DisplayName("getAuditLogs returns PageResponse of AuditLogResponse")
    void getAuditLogs_Success() {
        AuditLogSearchRequest request = AuditLogSearchRequest.builder().page(0).size(20).action(AuditAction.QC_PASS).build();
        AuditLog domainLog = AuditLog.builder().id(logId).action(AuditAction.QC_PASS).build();
        AuditLogResponse dto = AuditLogResponse.builder().id(logId).action(AuditAction.QC_PASS).build();

        when(auditLogRepository.count(any(AuditLogSearchCriteria.class))).thenReturn(1L);
        when(auditLogRepository.search(any(AuditLogSearchCriteria.class))).thenReturn(List.of(domainLog));
        when(mapper.toDto(domainLog)).thenReturn(dto);

        PageResponse<AuditLogResponse> response = auditLogService.getAuditLogs(request);

        assertThat(response).isNotNull();
        assertThat(response.getItems()).hasSize(1);
        assertThat(response.getTotalElements()).isEqualTo(1L);
    }

    @Test
    @DisplayName("getAuditLogById returns DTO when found")
    void getAuditLogById_Found_ReturnsDto() {
        AuditLog domainLog = AuditLog.builder().id(logId).action(AuditAction.ACTIVATE_BOM).build();
        AuditLogResponse dto = AuditLogResponse.builder().id(logId).action(AuditAction.ACTIVATE_BOM).build();

        when(auditLogRepository.findById(logId)).thenReturn(Optional.of(domainLog));
        when(mapper.toDto(domainLog)).thenReturn(dto);

        AuditLogResponse result = auditLogService.getAuditLogById(logId);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(logId);
    }

    @Test
    @DisplayName("getAuditLogById throws AuditLogNotFoundException when missing")
    void getAuditLogById_NotFound_ThrowsException() {
        when(auditLogRepository.findById(logId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> auditLogService.getAuditLogById(logId))
                .isInstanceOf(AuditLogNotFoundException.class)
                .hasMessageContaining("Audit log entry not found");
    }
}
