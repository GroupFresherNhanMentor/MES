package fpt.qn.mes.workorder.infrastructure.persistence.adapter;

import static fpt.qn.mes.jooq.Tables.AUDIT_LOGS;

import java.util.UUID;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import fpt.qn.mes.common.util.UuidV7;
import fpt.qn.mes.workorder.application.port.out.AuditLogPort;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Repository
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuditLogPersistenceAdapter implements AuditLogPort {

    DSLContext ctx;

    @Override
    public void recordStatusTransition(UUID actorId, UUID workOrderId, String oldStatus, String newStatus) {
        ctx.insertInto(AUDIT_LOGS)
                .set(AUDIT_LOGS.ID, UuidV7.generate())
                .set(AUDIT_LOGS.ACTOR_ID, actorId)
                .set(AUDIT_LOGS.ACTION, "RESERVE_MATERIAL")
                .set(AUDIT_LOGS.ENTITY_TYPE, "WORK_ORDER")
                .set(AUDIT_LOGS.ENTITY_ID, workOrderId)
                .set(AUDIT_LOGS.OLD_VALUE, oldStatus)
                .set(AUDIT_LOGS.NEW_VALUE, newStatus)
                .execute();
    }
}
