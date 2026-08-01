package fpt.qn.mes.workorder.infrastructure.persistence.adapter;

import static fpt.qn.mes.jooq.Tables.AUDIT_LOGS;

import java.util.UUID;

import org.jooq.DSLContext;
import org.jooq.JSONB;
import org.springframework.stereotype.Repository;

import fpt.qn.mes.common.util.UuidV7;
import fpt.qn.mes.workorder.application.port.out.AuditLogPort;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Repository("workOrderAuditLogPersistenceAdapter")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuditLogPersistenceAdapter implements AuditLogPort {

    DSLContext ctx;

    @Override
    public void recordStatusTransition(UUID actorId, UUID workOrderId, String oldStatus, String newStatus) {
        // Reservation flow uses the historical default action for backward-compatible audit entries.
        recordStatusTransition(actorId, workOrderId, oldStatus, newStatus, "RESERVE_MATERIAL");
    }

    @Override
    public void recordStatusTransition(UUID actorId, UUID workOrderId, String oldStatus, String newStatus, String action) {
        // Store status values as JSON strings because audit_logs uses JSONB columns.
        ctx.insertInto(AUDIT_LOGS)
                .set(AUDIT_LOGS.ID, UuidV7.generate())
                .set(AUDIT_LOGS.ACTOR_ID, actorId)
                .set(AUDIT_LOGS.ACTION, action)
                .set(AUDIT_LOGS.ENTITY_TYPE, "WORK_ORDER")
                .set(AUDIT_LOGS.ENTITY_ID, workOrderId)
                .set(AUDIT_LOGS.OLD_VALUE, statusJson(oldStatus))
                .set(AUDIT_LOGS.NEW_VALUE, statusJson(newStatus))
                .execute();
    }

    private JSONB statusJson(String status) {
        // A null status remains a JSONB null database value rather than the string "null".
        return status == null ? null : JSONB.valueOf("\"" + status + "\"");
    }
}
