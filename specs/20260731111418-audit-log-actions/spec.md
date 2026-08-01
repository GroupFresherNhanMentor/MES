# Feature Specification: Audit Log for Important Actions (FR-AUD-001)

**Feature Branch**: `004-audit-log-actions`

**Created**: 2026-07-31

**Status**: Draft

**Input**: User description: "5.12. Audit Log FR-AUD-001 — Audit Important Actions. Hệ thống phải ghi audit log cho các action quan trọng: CREATE_WORK_ORDER, RESERVE_MATERIAL, RELEASE_RESERVATION, START_PRODUCTION, PAUSE_PRODUCTION, RESUME_PRODUCTION, COMPLETE_PRODUCTION, QC_PASS, QC_FAIL, QC_HOLD, QC_RELEASE, SCRAP_STOCK, CREATE_MAINTENANCE_TICKET, START_MAINTENANCE, CLOSE_MAINTENANCE_TICKET, ADJUST_STOCK, ACTIVATE_BOM. Audit log gồm: id, actorId, action, entityType, entityId, oldValue, newValue, createdAt, ipAddress (optional). Business rule: Audit log không được sửa hoặc xóa. Action quan trọng không có audit log xem như chưa đạt requirement."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Automated Audit Trail Recording for Critical Actions (Priority: P1)

As a System Auditor or Compliance Officer, I want every critical manufacturing and operational action automatically recorded in an immutable audit log, so that I can trace who performed what action, on which entity, when it occurred, and what state changes were applied.

**Why this priority**: Immutability and compliance recording are foundational requirement P1. Any key business action performed without an audit log entry is considered a compliance failure.

**Independent Test**: Can be tested by performing any of the 17 specified important actions (e.g. `CREATE_WORK_ORDER` or `ACTIVATE_BOM`) and verifying that an immutable audit log record with actor details, action type, entity references, state diffs, and timestamp is generated.

**Acceptance Scenarios**:

1. **Given** an authenticated user performing a critical action (e.g. `CREATE_WORK_ORDER`, `ACTIVATE_BOM`, or `ADJUST_STOCK`), **When** the operation completes successfully, **Then** an audit log entry is recorded with `actorId`, `action`, `entityType`, `entityId`, `oldValue`, `newValue`, `createdAt`, and optional `ipAddress`.
2. **Given** an operational state transition (e.g., `START_PRODUCTION`, `PAUSE_PRODUCTION`, `RESUME_PRODUCTION`, `COMPLETE_PRODUCTION`), **When** state changes occur, **Then** the audit log captures the pre-transition state in `oldValue` and post-transition state in `newValue`.
3. **Given** quality control and maintenance operations (`QC_PASS`, `QC_FAIL`, `QC_HOLD`, `QC_RELEASE`, `CREATE_MAINTENANCE_TICKET`, `START_MAINTENANCE`, `CLOSE_MAINTENANCE_TICKET`), **When** executed, **Then** dedicated audit records are generated linking to the specific target entity.

---

### User Story 2 - Audit Log Immutability Enforcement (Priority: P2)

As a Compliance Officer, I want to ensure that no audit log entry can ever be modified or deleted by any user or administrator, so that audit history remains tamper-proof.

**Why this priority**: Ensures data integrity and legal compliance for manufacturing audit trailing.

**Independent Test**: Can be tested by attempting update or delete operations on audit log records and verifying that all modification attempts are strictly rejected.

**Acceptance Scenarios**:

1. **Given** existing audit log records in the system, **When** any user or automated process attempts to modify or update an existing log record, **Then** the system rejects the operation and preserves original log data intact.
2. **Given** existing audit log records in the system, **When** any request is made to delete log entries, **Then** the system denies the deletion operation.

---

### User Story 3 - Audit Log Query and Traceability (Priority: P3)

As a Plant Supervisor or Quality Auditor, I want to search and filter audit log entries by entity, actor, action type, or date range, so that I can investigate operational incidents and verify compliance history.

**Why this priority**: Enables operational visibility and quick investigation of historical events.

**Independent Test**: Can be tested by querying audit log records using combinations of `entityType`, `entityId`, `actorId`, and `action` filters and verifying correct matching log entries are returned.

**Acceptance Scenarios**:

1. **Given** recorded audit log entries across various entities, **When** searching by a specific `entityType` and `entityId`, **Then** the system returns all historical audit records for that entity in chronological order.
2. **Given** recorded audit log entries across various actions, **When** filtering by action type (e.g., `QC_FAIL` or `SCRAP_STOCK`), **Then** the system returns only matching audit records.

---

### Edge Cases

- What happens when a business action transaction rolls back due to a failure? The audit log entry associated with that transaction MUST also be rolled back so uncommitted actions are not logged as completed.
- How does the system handle missing or optional `ipAddress` (e.g. background job or batch execution)? `ipAddress` remains null while all required fields (`actorId`, `action`, `entityType`, `entityId`, `createdAt`) are fully captured.
- What happens if `oldValue` is null (e.g. creation actions like `CREATE_WORK_ORDER` or `CREATE_MAINTENANCE_TICKET`)? `oldValue` is recorded as null while `newValue` contains the initial entity payload.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: System MUST record an immutable audit log entry whenever any of the following 17 critical actions occur: `CREATE_WORK_ORDER`, `RESERVE_MATERIAL`, `RELEASE_RESERVATION`, `START_PRODUCTION`, `PAUSE_PRODUCTION`, `RESUME_PRODUCTION`, `COMPLETE_PRODUCTION`, `QC_PASS`, `QC_FAIL`, `QC_HOLD`, `QC_RELEASE`, `SCRAP_STOCK`, `CREATE_MAINTENANCE_TICKET`, `START_MAINTENANCE`, `CLOSE_MAINTENANCE_TICKET`, `ADJUST_STOCK`, `ACTIVATE_BOM`.
- **FR-002**: Every audit log record MUST contain: unique identifier (`id`), actor identifier (`actorId`), action type (`action`), entity category (`entityType`), entity identifier (`entityId`), prior state snapshot (`oldValue`), updated state snapshot (`newValue`), creation timestamp (`createdAt`), and optional client IP address (`ipAddress`).
- **FR-003**: System MUST strictly enforce immutability on audit log records — modification (UPDATE) and deletion (DELETE) operations on audit logs MUST be prohibited.
- **FR-004**: System MUST record audit log entries within the same transactional boundary as the audited business action to prevent audit mismatch.
- **FR-005**: System MUST support querying audit log records with pagination and filtering by `entityType`, `entityId`, `actorId`, `action`, and date range.

### Key Entities *(include if feature involves data)*

- **AuditLog**: Represents an immutable audit trail record of a critical business action.
  - `id`: Unique identifier (UUID).
  - `actorId`: User ID of the actor who performed the action.
  - `action`: Specific audited action enum name (e.g. `CREATE_WORK_ORDER`, `QC_PASS`, etc.).
  - `entityType`: Name of the target entity type (e.g., `WORK_ORDER`, `STOCK_BALANCE`, `BOM`, `MAINTENANCE_TICKET`).
  - `entityId`: Unique identifier of the target entity.
  - `oldValue`: JSON or text representation of the entity state before the action (nullable for creations).
  - `newValue`: JSON or text representation of the entity state after the action.
  - `createdAt`: Timestamp when the audit log was recorded (`OffsetDateTime` / `Instant`).
  - `ipAddress`: Optional IP address of the client request.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 100% of defined critical business actions generate audit log entries upon successful completion.
- **SC-002**: 0% of audit log modification or deletion attempts succeed (100% immutability enforcement).
- **SC-003**: Audit log entries are retrievable via search queries in under 500ms for routine operational checks.

## Assumptions

- Audit actions are triggered synchronously within the application services executing the respective business operations.
- `actorId` is populated from the authenticated user principal executing the request (`CurrentUserPort`).
- `oldValue` and `newValue` format uses structured JSON strings for ease of diffing and historical display.
