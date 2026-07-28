# Quickstart Validation Guide: Get BOM Statuses (Lookup)

This guide describes how to validate the **Get BOM Statuses (lookup)** endpoint end-to-end.

## 1. Prerequisites

- Backend Spring Boot server running: `./mvnw spring-boot:run`
- Authenticated JWT token for any user role

## 2. Validation Scenarios

### Scenario 1: Fetch BOM Statuses (Happy Path)

**HTTP Request**:
```http
GET /api/boms/statuses
Authorization: Bearer <JWT_TOKEN>
```

**Expected Outcome**:
- Status: `200 OK`
- Body:
```json
{
  "code": 200,
  "message": "OK",
  "data": [
    {
      "id": "00000000-0000-0000-0000-000000000001",
      "name": "DRAFT",
      "description": "Draft BOM version, can be edited"
    },
    {
      "id": "00000000-0000-0000-0000-000000000002",
      "name": "ACTIVE",
      "description": "Active BOM version used for production Work Orders"
    },
    {
      "id": "00000000-0000-0000-0000-000000000003",
      "name": "INACTIVE",
      "description": "Superseded or deactivated BOM version"
    }
  ]
}
```

---

### Scenario 2: Unauthenticated Request

**HTTP Request**:
```http
GET /api/boms/statuses
```

**Expected Outcome**:
- Status: `401 Unauthorized`
