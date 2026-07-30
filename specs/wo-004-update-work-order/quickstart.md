# Quickstart: Update Work Order

## Prerequisites

- PostgreSQL running with seed data loaded (work_order_statuses, work_order_priorities seeded)
- At least one Work Order created in DRAFT status
- Valid JWT token for a user with PLANNER or ADMIN role

## Validation Scenarios

### Scenario 1: Update Planning Fields (Happy Path)

**Goal**: Update code, quantity, dates, and priority of a DRAFT Work Order.

```bash
curl -X PUT http://localhost:8080/api/work-orders/{workOrderId} \
  -H "Authorization: Bearer {jwt_token}" \
  -H "Content-Type: application/json" \
  -d '{
    "code": "WO-2026-UPDATED",
    "plannedQuantity": 250.0,
    "plannedStartDate": "2026-08-01T00:00:00Z",
    "plannedEndDate": "2026-08-15T00:00:00Z",
    "priorityId": "{priorityUUID}"
  }'
```

**Expected**: HTTP 200 with updated `WorkOrderDto` including recalculated materials.

---

### Scenario 2: Transition DRAFT → PLANNED

**Goal**: Promote a Work Order from DRAFT to PLANNED status.

```bash
curl -X PUT http://localhost:8080/api/work-orders/{workOrderId} \
  -H "Authorization: Bearer {jwt_token}" \
  -H "Content-Type: application/json" \
  -d '{
    "workOrderStatusId": "{PLANNED_STATUS_UUID}"
  }'
```

**Expected**: HTTP 200 with `workOrderStatusId` set to the PLANNED status UUID.

---

### Scenario 3: Reject Non-Planning Status via PUT

**Goal**: Verify that attempting to set an operational status (e.g., IN_PROGRESS) via PUT is rejected.

```bash
curl -X PUT http://localhost:8080/api/work-orders/{workOrderId} \
  -H "Authorization: Bearer {jwt_token}" \
  -H "Content-Type: application/json" \
  -d '{
    "workOrderStatusId": "{IN_PROGRESS_STATUS_UUID}"
  }'
```

**Expected**: HTTP 400 with error code `BAD_REQUEST` and message about using dedicated action endpoints.

---

### Scenario 4: Reject Update on Non-Editable State

**Goal**: Verify that updating a Work Order in READY_TO_PRODUCE (or any non-DRAFT/PLANNED) status is rejected.

```bash
# First, ensure the Work Order is in READY_TO_PRODUCE status (via reserve-materials action)
curl -X PUT http://localhost:8080/api/work-orders/{readyWorkOrderId} \
  -H "Authorization: Bearer {jwt_token}" \
  -H "Content-Type: application/json" \
  -d '{
    "plannedQuantity": 300.0
  }'
```

**Expected**: HTTP 400 with error code `BAD_REQUEST` indicating WO cannot be modified.

---

### Scenario 5: Reject Duplicate Code

**Goal**: Verify uniqueness constraint on `code` field.

```bash
curl -X PUT http://localhost:8080/api/work-orders/{workOrderId} \
  -H "Authorization: Bearer {jwt_token}" \
  -H "Content-Type: application/json" \
  -d '{
    "code": "EXISTING-WO-CODE"
  }'
```

**Expected**: HTTP 400 with error code `WORK_ORDER_CODE_EXISTS`.

---

### Scenario 6: Reject Invalid Dates

**Goal**: Verify date validation (start must precede end).

```bash
curl -X PUT http://localhost:8080/api/work-orders/{workOrderId} \
  -H "Authorization: Bearer {jwt_token}" \
  -H "Content-Type: application/json" \
  -d '{
    "plannedStartDate": "2026-08-15T00:00:00Z",
    "plannedEndDate": "2026-08-01T00:00:00Z"
  }'
```

**Expected**: HTTP 400 with error code `INVALID_INPUT`.

---

### Scenario 7: Authorization Denied (OPERATOR role)

**Goal**: Verify only ADMIN/PLANNER can access.

```bash
curl -X PUT http://localhost:8080/api/work-orders/{workOrderId} \
  -H "Authorization: Bearer {operator_jwt_token}" \
  -H "Content-Type: application/json" \
  -d '{
    "plannedQuantity": 100.0
  }'
```

**Expected**: HTTP 403 Forbidden.

## Running Tests

```bash
cd be
./mvnw test "-Dtest=WorkOrderServiceTest,WorkOrderControllerTest"
```

**Expected**: All tests pass with `BUILD SUCCESS`.
