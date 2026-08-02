# Quickstart Validation Guide: Query Work Orders (`GET /api/work-orders`)

## Verification Overview

This guide provides end-to-end instructions for validating the `GET /api/work-orders` API endpoint.

---

## 1. Prerequisites & Environment Setup

1. Start PostgreSQL 18 container via Docker Compose:
   ```bash
   docker-compose up -d
   ```
2. Verify Database Status:
   ```bash
   docker exec -it mes_postgres pg_isready -U mes_user -d mes_db
   ```
3. Run Spring Boot Backend:
   ```bash
   cd be && ./mvnw spring-boot:run
   ```

---

## 2. Automated Test Execution

Run unit and controller slice tests:

```bash
cd be && ./mvnw test -Dtest=WorkOrderServiceTest,WorkOrderControllerTest
```

Expected Output:
```text
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

---

## 3. Manual Scenario Validation

### Scenario 1: Authenticate as PLANNER
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "planner_user", "password": "password123"}'
```
*Save returned JWT access token as `$TOKEN`.*

### Scenario 2: Retrieve Default Paginated Work Orders
```bash
curl -X GET "http://localhost:8080/api/work-orders?page=0&size=20" \
  -H "Authorization: Bearer $TOKEN"
```
**Expected Outcome**: Returns HTTP 200 OK with `ApiResponse` envelope containing `content` list and pagination fields (`page: 0`, `size: 20`).

### Scenario 3: Filter Work Orders by Status & Product
```bash
curl -X GET "http://localhost:8080/api/work-orders?finishedProductId=a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d&statusId=c3d4e5f6-a7b8-9c0d-1e2f-3a4b5c6d7e8f" \
  -H "Authorization: Bearer $TOKEN"
```
**Expected Outcome**: Returns HTTP 200 OK with only Work Orders matching both specified product and status IDs.

### Scenario 4: Fuzzy Search by Code
```bash
curl -X GET "http://localhost:8080/api/work-orders?code=0001" \
  -H "Authorization: Bearer $TOKEN"
```
**Expected Outcome**: Returns HTTP 200 OK including Work Orders with code containing "0001" (e.g. `WO-2026-0001`).

### Scenario 5: Verify RBAC Protection (Unauthorized Access)
```bash
curl -X GET "http://localhost:8080/api/work-orders"
```
**Expected Outcome**: Returns HTTP 401 Unauthorized (`"errorCode": "UNAUTHORIZED"`).
