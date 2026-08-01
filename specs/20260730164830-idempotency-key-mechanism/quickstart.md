# Quickstart & Validation Guide: Idempotency Key Mechanism

## Prerequisites

- PostgreSQL database running (via `docker-compose up -d` or local PostgreSQL instance)
- Java 25 & Maven wrapper (`./mvnw`)

## Verification Steps

### 1. Database Migration & Compilation
Run the Flyway migration and compile the project to verify jOOQ code generation:
```bash
cd be
./mvnw clean compile
```

### 2. Run Idempotency Tests
Execute unit and integration tests covering single execution, duplicate replay, and concurrency locking:
```bash
cd be
./mvnw test -Dtest=Idempotency*Test
```

### 3. End-to-End Manual API Validation

1. **Submit initial request with `X-Idempotency-Key`**:
   ```bash
   curl -X POST http://localhost:8080/api/stock-in \
     -H "Content-Type: application/json" \
     -H "X-Idempotency-Key: test-key-12345" \
     -d '{"productId":"...", "warehouseId":"...", "quantity": 100}'
   ```
   *Expected Response*: `200 OK` (or `201 Created`) with stock movement payload.

2. **Re-submit exact duplicate request with same `X-Idempotency-Key`**:
   ```bash
   curl -X POST http://localhost:8080/api/stock-in \
     -H "Content-Type: application/json" \
     -H "X-Idempotency-Key: test-key-12345" \
     -d '{"productId":"...", "warehouseId":"...", "quantity": 100}'
   ```
   *Expected Response*: Identical `200 OK` cached payload in <50ms without creating a second stock movement entry in the database.
