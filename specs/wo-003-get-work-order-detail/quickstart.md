# Quickstart & Verification Guide: Get Work Order Detail (`GET /api/work-orders/{id}`)

This document describes how to execute automated tests and manual verification scenarios for retrieving Work Order detail.

## 1. Automated Test Execution

Execute the Maven test command to run the service and controller test suites:

```bash
cd be
./mvnw test "-Dtest=WorkOrderServiceTest,WorkOrderControllerTest"
```

Expected output:
```text
[INFO] Tests run: 6, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

---

## 2. Manual Verification Scenarios

### Scenario A: Successful Work Order Detail Lookup

1. Issue HTTP request:

```http
GET /api/work-orders/a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11
Authorization: Bearer <VALID_JWT_TOKEN>
```

2. Expected Response (HTTP 200 OK):
   - `success`: `true`
   - `data.id`: `"a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11"`
   - `data.materials`: Array containing material objects with `requiredQuantity`, `reservedQuantity`, `consumedQuantity`.
   - `data.events`: Array containing history events with `eventTimestamp` and `operatorId`.

---

### Scenario B: Non-Existent Work Order ID

1. Issue HTTP request with a non-existent UUID:

```http
GET /api/work-orders/99999999-9c0b-4ef8-bb6d-6bb9bd380a11
Authorization: Bearer <VALID_JWT_TOKEN>
```

2. Expected Response (HTTP 404 Not Found):
   - `success`: `false`
   - `errorCode`: `"NOT_FOUND"`
   - `message`: `"Work Order not found with ID: 99999999-9c0b-4ef8-bb6d-6bb9bd380a11"`
