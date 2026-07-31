package fpt.qn.mes.audit.infrastructure.listener;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import fpt.qn.mes.audit.application.port.out.AuditLogPort;
import fpt.qn.mes.audit.domain.events.AuditEvent;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuditEventListener {

    AuditLogPort auditLogPort;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT, fallbackExecution = true)
    public void handleAuditEvent(AuditEvent event) {
        if (event == null || auditLogPort == null) {
            return;
        }

        String clientIp = event.getIpAddress();
        if (clientIp == null || clientIp.isBlank()) {
            clientIp = extractClientIpFromRequestContext();
        }

        auditLogPort.log(
                event.getActorId(),
                event.getAction(),
                event.getEntityType(),
                event.getEntityId(),
                event.getOldValue(),
                event.getNewValue(),
                clientIp
        );
    }

    private String extractClientIpFromRequestContext() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
    		HttpServletRequest request = attributes.getRequest();
                return request.getRemoteAddr(); // ◄◄ Direct TCP socket IP (non-spoofable)
            }
        } catch (Exception ignored) {
        }
        return null;
    }
}
