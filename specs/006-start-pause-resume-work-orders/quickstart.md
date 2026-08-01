# Quickstart & Manual Validation Guide: Start, Pause, and Resume Work Orders

This guide outlines end-to-end verification steps for `POST /start`, `POST /pause`, and `POST /resume`.

## Prerequisites

1. PostgreSQL database running (`docker-compose up -d`).
2. Spring Boot application running (`cd be && ./mvnw spring-boot:run`).
3. Active `OPERATOR` or `PLANNER` JWT token.

---

## Scenario 1: Start Production

### Step 1: Prepare Work Order & Machine
- Ensure Work Order is in `READY_TO_PRODUCE` status.
- Ensure Machine is in `AVAILABLE` status.

### Step 2: Execute Start API
```bash
curl -X POST http://localhost:8080/api/v1/work-orders/{WO_ID}/start \
  -H "Authorization: Bearer $OPERATOR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "machineId": "'$MACHINE_ID'"
  }'
```

### Expected Outcome
1. HTTP Status `200 OK`.
2. Work Order status becomes `IN_PROGRESS`.
3. Machine status becomes `RUNNING`.
4. A new record is created in `production_runs`.
5. A `START` event is recorded in `work_order_events`.

---

## Scenario 2: Pause and Resume Production

### Step 1: Execute Pause API
```bash
curl -X POST http://localhost:8080/api/v1/work-orders/{WO_ID}/pause \
  -H "Authorization: Bearer $OPERATOR_TOKEN"
```

### Expected Outcome
1. HTTP Status `200 OK`.
2. Work Order status becomes `PAUSED`.
3. Machine status remains `RUNNING`.
4. A `PAUSE` event is recorded in `work_order_events`.

### Step 2: Execute Resume API
```bash
curl -X POST http://localhost:8080/api/v1/work-orders/{WO_ID}/resume \
  -H "Authorization: Bearer $OPERATOR_TOKEN"
```

### Expected Outcome
1. HTTP Status `200 OK`.
2. Work Order status reverts to `IN_PROGRESS`.
3. A `RESUME` event is recorded in `work_order_events`.
