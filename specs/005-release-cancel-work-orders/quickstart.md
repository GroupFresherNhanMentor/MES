# Quickstart & Manual Validation Guide: Release and Cancel Work Orders

This guide outlines end-to-end verification steps for `POST /api/work-orders/{id}/release-materials` and `POST /api/work-orders/{id}/cancel`.

## Prerequisites

1. PostgreSQL database running (`docker-compose up -d`).
2. Spring Boot application running (`cd be && ./mvnw spring-boot:run`).
3. Active `PLANNER` JWT token or admin credentials.

---

## Scenario 1: Release Reserved Materials

### Step 1: Create and Reserve a Work Order
- Call `POST /api/work-orders` to create a Work Order.
- Call `POST /api/work-orders/{id}/reserve-materials` to reserve raw materials.
- Verify status is `READY_TO_PRODUCE` and material `reservedQuantity > 0`.

### Step 2: Call Release Materials Endpoint
```bash
curl -X POST http://localhost:8080/api/work-orders/{WO_ID}/release-materials \
  -H "Authorization: Bearer $PLANNER_TOKEN" \
  -H "Content-Type: application/json"
```

### Expected Outcome
1. HTTP Status `200 OK`.
2. Work Order status reverts to `PLANNED`.
3. Material `reservedQuantity` becomes `0.0`.
4. `stock_balances` table: `RESERVED` quantity decreases, `AVAILABLE` quantity increases.
5. `stock_movements` table: A new record with `RELEASE_RESERVATION` movement type is created.

---

## Scenario 2: Cancel Work Order (Auto-Release)

### Step 1: Create and Reserve another Work Order
- Call `POST /api/work-orders` to create a Work Order.
- Call `POST /api/work-orders/{id}/reserve-materials`.

### Step 2: Call Cancel Endpoint
```bash
curl -X POST http://localhost:8080/api/work-orders/{WO_ID}/cancel \
  -H "Authorization: Bearer $PLANNER_TOKEN" \
  -H "Content-Type: application/json"
```

### Expected Outcome
1. HTTP Status `200 OK`.
2. Work Order status becomes `CANCELLED`.
3. Reserved materials are automatically released back to `AVAILABLE`.
4. `stock_movements` table contains a `RELEASE_RESERVATION` movement record.

---

## Scenario 3: Validation Error Handling

### Step 1: Attempt to Cancel an In-Progress Work Order
- Call `POST /api/work-orders/{WO_ID}/cancel` on a Work Order that is in `IN_PROGRESS` status.

### Expected Outcome
1. HTTP Status `400 Bad Request`.
2. Error message indicates cancellation is blocked for in-progress orders.
