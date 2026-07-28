# Quickstart Validation Guide: Add and Remove BOM Items

This guide describes how to validate the **Add BOM Item / Remove BOM Item** features end-to-end.

## 1. Prerequisites

- Backend Spring Boot server running: `./mvnw spring-boot:run`
- Authenticated JWT token with role `ADMIN` or `PLANNER`
- A target BOM in `DRAFT` status

## 2. Validation Scenarios

### Scenario 1: Add Component Item to DRAFT BOM (Happy Path)

**HTTP Request**:
```http
POST /api/boms/11111111-1111-1111-1111-111111111111/items
Content-Type: application/json
Authorization: Bearer <JWT_TOKEN>

{
  "materialProductId": "22222222-2222-2222-2222-222222222222",
  "quantityPerUnit": 3.50,
  "unit": "PCS",
  "scrapRate": 0.02
}
```

**Expected Outcome**:
- Status: `201 Created`
- Response contains generated `id`, matching `bomId`, `materialProductId`, `quantityPerUnit = 3.50`, `unit = "PCS"`, and `scrapRate = 0.02`.

---

### Scenario 2: Attempt Modification on ACTIVE BOM (Immutability Guard)

**HTTP Request**:
```http
POST /api/boms/<ACTIVE_BOM_UUID>/items
Content-Type: application/json
Authorization: Bearer <JWT_TOKEN>

{
  "materialProductId": "22222222-2222-2222-2222-222222222222",
  "quantityPerUnit": 1.00,
  "unit": "PCS"
}
```

**Expected Outcome**:
- Status: `400 Bad Request`
- Response message: `"Only DRAFT BOMs can be modified; create a new version instead"`

---

### Scenario 3: Remove Component Item from DRAFT BOM (Happy Path)

**HTTP Request**:
```http
DELETE /api/boms/11111111-1111-1111-1111-111111111111/items/33333333-3333-3333-3333-333333333333
Authorization: Bearer <JWT_TOKEN>
```

**Expected Outcome**:
- Status: `204 No Content`
- Subsequent `GET /api/boms/11111111-1111-1111-1111-111111111111` no longer lists the deleted item.
