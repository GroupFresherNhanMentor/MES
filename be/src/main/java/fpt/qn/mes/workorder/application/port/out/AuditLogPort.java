package fpt.qn.mes.workorder.application.port.out;

import java.util.UUID;

public interface AuditLogPort {
    void recordStatusTransition(UUID actorId, UUID workOrderId, String oldStatus, String newStatus, String action);

    void recordCompletion(UUID actorId, UUID workOrderId);
}
