# Data Model: Audit Log for Important Actions (FR-AUD-001)

## Schema Design (`audit_logs` Table)

```sql
CREATE TABLE audit_logs (
    id UUID PRIMARY KEY,
    actor_id UUID NOT NULL,
    action VARCHAR(50) NOT NULL,
    entity_type VARCHAR(50) NOT NULL,
    entity_id UUID NOT NULL,
    old_value JSONB,
    new_value JSONB,
    ip_address VARCHAR(45),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_audit_logs_entity ON audit_logs (entity_type, entity_id);
CREATE INDEX idx_audit_logs_actor ON audit_logs (actor_id);
CREATE INDEX idx_audit_logs_action ON audit_logs (action);
CREATE INDEX idx_audit_logs_created_at ON audit_logs (created_at DESC);

-- Immutability enforcement trigger
CREATE OR REPLACE FUNCTION prevent_audit_log_modification()
RETURNS TRIGGER AS $$
BEGIN
    RAISE EXCEPTION 'Audit logs are immutable and cannot be updated or deleted';
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_prevent_audit_log_modification
BEFORE UPDATE OR DELETE ON audit_logs
FOR EACH ROW EXECUTE FUNCTION prevent_audit_log_modification();
```

## Domain Entities

### `AuditLog` (`fpt.qn.mes.audit.domain.entities.AuditLog`)
- `UUID id` (Primary key, UUIDv7)
- `UUID actorId` (User who executed the action)
- `AuditAction action` (Enum: `CREATE_WORK_ORDER`, `RESERVE_MATERIAL`, `RELEASE_RESERVATION`, `START_PRODUCTION`, `PAUSE_PRODUCTION`, `RESUME_PRODUCTION`, `COMPLETE_PRODUCTION`, `QC_PASS`, `QC_FAIL`, `QC_HOLD`, `QC_RELEASE`, `SCRAP_STOCK`, `CREATE_MAINTENANCE_TICKET`, `START_MAINTENANCE`, `CLOSE_MAINTENANCE_TICKET`, `ADJUST_STOCK`, `ACTIVATE_BOM`)
- `String entityType` (e.g. `WORK_ORDER`, `STOCK_BALANCE`, `BOM`, `QUALITY_INSPECTION`, `MAINTENANCE_TICKET`)
- `UUID entityId` (Target entity primary key)
- `String oldValue` (Nullable JSON representation of previous state)
- `String newValue` (JSON representation of post-action state)
- `String ipAddress` (Nullable client IP address)
- `Instant createdAt` (Recording timestamp)

### `AuditAction` Enum (`fpt.qn.mes.audit.domain.entities.AuditAction`)
- `CREATE_WORK_ORDER`
- `RESERVE_MATERIAL`
- `RELEASE_RESERVATION`
- `START_PRODUCTION`
- `PAUSE_PRODUCTION`
- `RESUME_PRODUCTION`
- `COMPLETE_PRODUCTION`
- `QC_PASS`
- `QC_FAIL`
- `QC_HOLD`
- `QC_RELEASE`
- `SCRAP_STOCK`
- `CREATE_MAINTENANCE_TICKET`
- `START_MAINTENANCE`
- `CLOSE_MAINTENANCE_TICKET`
- `ADJUST_STOCK`
- `ACTIVATE_BOM`
