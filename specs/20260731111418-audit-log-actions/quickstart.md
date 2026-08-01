# Quickstart Validation Guide: Audit Log for Important Actions (FR-AUD-001)

## Overview
This guide provides step-by-step verification steps to prove end-to-end functionality of the Audit Log feature, including automated log capture and strict immutability enforcement.

---

## 1. Prerequisites
- Backend running on `http://localhost:8080` (or test environment via `./mvnw test`).
- PostgreSQL 18 instance with Flyway migrations applied up to `V20260731143000__create_audit_logs_table.sql`.
- Authenticated JWT token for an authorized user.

---

## 2. Test Execution Commands

### Unit & Integration Test Validation
Run unit and integration test suites:

```bash
cd be
./mvnw test -Dtest=AuditLogServiceTest,AuditLogIntegrationTest,AuditLogImmutabilityTest
```

---

## 3. End-to-End Validation Steps

### Step A: Execute a Critical Business Action
Execute an operation triggering an audit log entry (e.g. `POST /api/stock-adjustments` or `POST /api/work-orders`):

```bash
curl -X POST http://localhost:8080/api/stock-adjustments \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "stockBalanceId": "019fb6c7-292a-79d0-9124-5ee0715ef474",
    "quantityAdjustment": 10.00,
    "reason": "Audit Test Fix"
  }'
```

### Step B: Query Audit Log Entry
Search for recorded audit log entries:

```bash
curl -X GET "http://localhost:8080/api/audit-logs?action=ADJUST_STOCK" \
  -H "Authorization: Bearer $TOKEN"
```

**Expected Outcome**: Returns HTTP 200 OK with `PageResponse<AuditLogResponse>` containing the generated `ADJUST_STOCK` entry, matching `actorId`, `entityType`, `entityId`, `newValue`, and timestamp.

### Step C: Verify Immutability Protection
Attempt a direct database UPDATE or DELETE on `audit_logs` table via SQL or repository:

**Expected Outcome**: Throws SQL Exception / Exception with message:
`Audit logs are immutable and cannot be updated or deleted`.
