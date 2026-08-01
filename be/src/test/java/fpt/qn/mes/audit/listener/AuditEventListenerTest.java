package fpt.qn.mes.audit.listener;

import static org.mockito.Mockito.verify;

import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import fpt.qn.mes.audit.application.port.out.AuditLogPort;
import fpt.qn.mes.audit.domain.entities.AuditAction;
import fpt.qn.mes.audit.domain.events.AuditEvent;
import fpt.qn.mes.audit.infrastructure.listener.AuditEventListener;

@ExtendWith(MockitoExtension.class)
class AuditEventListenerTest {

    @Mock
    AuditLogPort auditLogPort;

    @InjectMocks
    AuditEventListener listener;

    @Test
    @DisplayName("handleAuditEvent invokes auditLogPort.log with event parameters")
    void handleAuditEvent_DelegatesToAuditLogPort() {
        UUID actorId = UUID.randomUUID();
        UUID entityId = UUID.randomUUID();
        AuditEvent event = AuditEvent.create(actorId, AuditAction.CREATE_WORK_ORDER, "WORK_ORDER", entityId, null, "{\"code\":\"WO-1\"}", "127.0.0.1");

        listener.handleAuditEvent(event);

        verify(auditLogPort).log(actorId, AuditAction.CREATE_WORK_ORDER, "WORK_ORDER", entityId, null, "{\"code\":\"WO-1\"}", "127.0.0.1");
    }
}
