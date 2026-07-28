# Quickstart — Master Data Management Validation

## Prerequisites

- Docker running (for PostgreSQL)
- Project built: `cd be && ./mvnw compile -q`

## Setup

```bash
# Start PostgreSQL
docker-compose up -d

# Run migrations and seed data
cd be && ./mvnw flyway:migrate

# Start the application
./mvnw spring-boot:run
```

The application starts on `http://localhost:8080`.

---

## Validation Scenarios

All scenarios assume:
- A JWT token for an ADMIN user is available (`$TOKEN`)
- Base URL: `http://localhost:8080/api`

### Scenario 1: Product CRUD + Duplicate Code Rejection

**Step 1.1 — Create a product**

```bash
curl -s -X POST http://localhost:8080/api/products \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"code":"SP-001","name":"Steel Plate","productTypeId":"<raw_material_uuid>","unitId":"<kg_uuid>"}'
```

**Expected**: HTTP 201, response with product details, status ACTIVE

**Step 1.2 — Create duplicate product code**

```bash
curl -s -X POST http://localhost:8080/api/products \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"code":"SP-001","name":"Duplicate Steel","productTypeId":"<raw_material_uuid>","unitId":"<kg_uuid>"}'
```

**Expected**: HTTP 409, error code `PRODUCT_CODE_DUPLICATE`

**Step 1.3 — List products**

```bash
curl -s "http://localhost:8080/api/products?page=0&size=20" \
  -H "Authorization: Bearer $TOKEN"
```

**Expected**: HTTP 200, paginated response with created product(s)

**Step 1.4 — Get product by ID**

```bash
curl -s http://localhost:8080/api/products/$PRODUCT_ID \
  -H "Authorization: Bearer $TOKEN"
```

**Expected**: HTTP 200, product details

**Step 1.5 — Deactivate product**

```bash
curl -s -X DELETE http://localhost:8080/api/products/$PRODUCT_ID \
  -H "Authorization: Bearer $TOKEN"
```

**Expected**: HTTP 200, product status changed to INACTIVE

**Step 1.6 — Get non-existent product**

```bash
curl -s http://localhost:8080/api/products/00000000-0000-0000-0000-000000000000 \
  -H "Authorization: Bearer $TOKEN"
```

**Expected**: HTTP 404, error code `PRODUCT_NOT_FOUND`

---

### Scenario 2: Warehouse + Location CRUD

**Step 2.1 — Create warehouse**

```bash
curl -s -X POST http://localhost:8080/api/warehouses \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"code":"WH-HN","name":"Hanoi Warehouse","address":"123 Factory Street"}'
```

**Expected**: HTTP 201

**Step 2.2 — Add location to warehouse**

```bash
curl -s -X POST http://localhost:8080/api/warehouses/$WH_ID/locations \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"code":"A01","name":"Aisle 1 Shelf A"}'
```

**Expected**: HTTP 201

**Step 2.3 — Duplicate location code (same warehouse)**

```bash
curl -s -X POST http://localhost:8080/api/warehouses/$WH_ID/locations \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"code":"A01","name":"Duplicate"}'
```

**Expected**: HTTP 409, error code `LOCATION_CODE_DUPLICATE`

**Step 2.4 — Create same location code in a different warehouse**

```bash
curl -s -X POST http://localhost:8080/api/warehouses/$OTHER_WH_ID/locations \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"code":"A01","name":"Aisle 1 Shelf A in other WH"}'
```

**Expected**: HTTP 201 (cross-warehouse duplicates allowed)

---

### Scenario 3: Production Line + Machine

**Step 3.1 — Create production line**

```bash
curl -s -X POST http://localhost:8080/api/production-lines \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"code":"LINE-01","name":"Assembly Line 1"}'
```

**Expected**: HTTP 201

**Step 3.2 — Add machine to line**

```bash
curl -s -X POST http://localhost:8080/api/production-lines/$LINE_ID/machines \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"code":"M-001","name":"CNC Machine 1"}'
```

**Expected**: HTTP 201, machine status AVAILABLE

**Step 3.3 — Change machine status**

```bash
curl -s -X PUT http://localhost:8080/api/production-lines/$LINE_ID/machines/$MACHINE_ID/status \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"statusId":"<in_use_uuid>"}'
```

**Expected**: HTTP 200

**Step 3.4 — Invalid status transition (BROKEN → AVAILABLE)**

```bash
curl -s -X PUT http://localhost:8080/api/production-lines/$LINE_ID/machines/$MACHINE_ID/status \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"statusId":"<available_uuid>"}'
```

**Expected**: HTTP 400, error code `MACHINE_INVALID_STATUS_TRANSITION`

---

### Scenario 4: Authorization Enforcement

**Step 4.1 — No authentication**

```bash
curl -s http://localhost:8080/api/products
```

**Expected**: HTTP 401 Unauthorized

**Step 4.2 — Non-ADMIN token**

```bash
curl -s -X POST http://localhost:8080/api/products \
  -H "Authorization: Bearer $NON_ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"code":"SP-002","name":"Test","productTypeId":"uuid","unitId":"uuid"}'
```

**Expected**: HTTP 403 Forbidden

**Step 4.3 — ADMIN token (read)**

```bash
curl -s http://localhost:8080/api/products?page=0&size=20 \
  -H "Authorization: Bearer $TOKEN"
```

**Expected**: HTTP 200 OK

---

## Expected Outcomes Matrix

| Scenario | Endpoint | Expected Result |
|----------|----------|-----------------|
| Create product | POST /api/products | 201 Created |
| Duplicate product code | POST /api/products | 409 CONFLICT |
| List products | GET /api/products | 200 + paginated |
| Get product by ID | GET /api/products/{id} | 200 |
| Deactivate product | DELETE /api/products/{id} | 200 (status→INACTIVE) |
| Get non-existent product | GET /api/products/{bad-id} | 404 |
| Create warehouse | POST /api/warehouses | 201 Created |
| Add location | POST /api/warehouses/{wh}/locations | 201 Created |
| Duplicate location in same WH | POST /api/warehouses/{wh}/locations | 409 CONFLICT |
| Duplicate location in diff WH | POST /api/warehouses/{wh}/locations | 201 Created |
| Create production line | POST /api/production-lines | 201 Created |
| Add machine | POST /api/production-lines/{line}/machines | 201 Created |
| Change machine status | PUT .../machines/{id}/status | 200 |
| Invalid status transition | PUT .../machines/{id}/status | 400 BAD REQUEST |
| No auth | ANY endpoint | 401 UNAUTHORIZED |
| Wrong role CUD | POST /api/products | 403 FORBIDDEN |
