# Quickstart & Verification Guide: Create Work Order (`POST /api/v1/work-orders`)

This document describes how to execute end-to-end verification and run unit tests for the Create Work Order feature.

## 1. Automated Test Execution

Execute the Maven test command to run the WorkOrder service and controller test suites:

```bash
cd be
./mvnw test "-Dtest=WorkOrderServiceTest,WorkOrderControllerTest"
```

Expected output:
```text
[INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

---

## 2. Manual Verification Scenarios

### Scenario A: Successful Work Order Creation (Planner Role)

1. Obtain a JWT token for a user with `ROLE_PLANNER`.
2. Issue HTTP request:

```http
POST /api/v1/work-orders
Authorization: Bearer <PLANNER_JWT_TOKEN>
Content-Type: application/json

{
  "code": "WO-2026-0005",
  "finishedProductId": "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11",
  "plannedQuantity": 100,
  "plannedStartDate": "2026-08-01T08:00:00Z",
  "plannedEndDate": "2026-08-05T17:00:00Z"
}
```

3. Expected Response (HTTP 201 Created):
   - `success`: `true`
   - `data.code`: `"WO-2026-0005"`
   - `data.bomId`: Non-null UUID matching the active BOM.

---

### Scenario B: Product Without Active BOM (Error Handling)

1. Issue HTTP request for a product without an active BOM:

```http
POST /api/v1/work-orders
Authorization: Bearer <PLANNER_JWT_TOKEN>
Content-Type: application/json

{
  "code": "WO-2026-0006",
  "finishedProductId": "99999999-9c0b-4ef8-bb6d-6bb9bd380a11",
  "plannedQuantity": 50,
  "plannedStartDate": "2026-08-01T08:00:00Z",
  "plannedEndDate": "2026-08-05T17:00:00Z"
}
```

2. Expected Response (HTTP 400 Bad Request):
   - `success`: `false`
   - `errorCode`: `"BOM_NOT_ACTIVE"`

---

### Scenario C: Unauthorized Role Access (Admin Role)

1. Issue HTTP request with a user having `ROLE_ADMIN` (or `ROLE_OPERATOR`):

```http
POST /api/v1/work-orders
Authorization: Bearer <ADMIN_JWT_TOKEN>
Content-Type: application/json

{
  "code": "WO-2026-0007",
  "finishedProductId": "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11",
  "plannedQuantity": 10,
  "plannedStartDate": "2026-08-01T08:00:00Z",
  "plannedEndDate": "2026-08-05T17:00:00Z"
}
```

2. Expected Response (HTTP 403 Forbidden):
   - `status`: `403`
   - `errorCode`: `"FORBIDDEN"`
