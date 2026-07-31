package fpt.qn.mes.audit.integration;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fpt.qn.mes.AbstractIntegrationTest;
import fpt.qn.mes.audit.domain.entities.AuditAction;
import fpt.qn.mes.audit.domain.entities.AuditLog;
import fpt.qn.mes.audit.infrastructure.persistence.audit.AuditLogPersistenceAdapter;

class AuditLogImmutabilityIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    AuditLogPersistenceAdapter persistenceAdapter;

    @Test
    @DisplayName("deleteById throws UnsupportedOperationException preventing deletion")
    void deleteById_ThrowsUnsupportedOperationException() {
        UUID id = UUID.randomUUID();
        assertThatThrownBy(() -> persistenceAdapter.deleteById(id))
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessageContaining("Audit logs are immutable and cannot be updated or deleted");
    }

    @Test
    @DisplayName("Audit logs persisted cannot be mutated")
    void saveAuditLog_IsImmutable() {
        AuditLog auditLog = AuditLog.create(
                UUID.randomUUID(),
                AuditAction.CREATE_WORK_ORDER,
                "WORK_ORDER",
                UUID.randomUUID(),
                null,
                "{\"code\":\"WO-IMMUTABLE\"}",
                "127.0.0.1"
        );
        AuditLog saved = persistenceAdapter.save(auditLog);

        assertThatThrownBy(() -> persistenceAdapter.deleteById(saved.getId()))
                .isInstanceOf(UnsupportedOperationException.class);
    }
}
