# Quickstart Validation Guide: Create BOM Header

**Feature**: `bom-001-create-header`  
**Date**: 2026-07-28  
**Spec**: [spec.md](spec.md) | **API Contract**: [contracts/create-bom-api.json](contracts/create-bom-api.json)

## Quickstart Scenarios

This guide details how to validate the **Create BOM Header** functionality end-to-end.

---

### Prerequisites

1. PostgreSQL database running with Flyway migrations applied (`./mvnw flyway:migrate`).
2. Spring Boot application running on `http://localhost:8080`.
3. Valid JWT Token for a user with `PLANNER` role.
4. Target product exists in `products` table with type `FINISHED_GOOD` or `SEMI_FINISHED`.

---

### Scenario 1: Create Valid BOM Header (Happy Path)

**Goal**: Verify a Planner can create a new BOM header in `DRAFT` status for a valid product.

**HTTP Request**:
```http
POST /api/boms
Content-Type: application/json
Authorization: Bearer <PLANNER_JWT_TOKEN>

{
  "finishedProductId": "550e8400-e29b-41d4-a716-446655440000",
  "version": 1
}
```

**Expected Response**:
- Status: `201 Created`
- Body:
```json
{
  "success": true,
  "data": {
    "id": "<GENERATED_UUID>",
    "finishedProductId": "550e8400-e29b-41d4-a716-446655440000",
    "version": 1,
    "bomStatusId": "<DRAFT_STATUS_UUID>",
    "createdBy": "<USER_UUID>",
    "createdAt": "2026-07-28T10:00:00Z",
    "items": []
  },
  "message": "Created",
  "timestamp": "2026-07-28T10:00:00Z"
}
```

---

### Scenario 2: Create Duplicate Version (Conflict)

**Goal**: Verify system rejects duplicate version numbers for the same product.

**HTTP Request**: Repeat Scenario 1 with the same `finishedProductId` and `version: 1`.

**Expected Response**:
- Status: `409 Conflict`
- Body `errorCode`: `CONFLICT`
- Body `message`: "BOM version 1 already exists for product 550e8400-..."

---

### Scenario 3: Create BOM for Raw Material (Invalid Product Type)

**Goal**: Verify system prevents creating a BOM header for a `RAW_MATERIAL`.

**HTTP Request**:
```http
POST /api/boms
Content-Type: application/json
Authorization: Bearer <PLANNER_JWT_TOKEN>

{
  "finishedProductId": "<RAW_MATERIAL_PRODUCT_UUID>",
  "version": 1
}
```

**Expected Response**:
- Status: `400 Bad Request`
- Body `errorCode`: `INVALID_INPUT`
- Body `message`: "BOM can only be created for FINISHED_GOOD or SEMI_FINISHED products"
