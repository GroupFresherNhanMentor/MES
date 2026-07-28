# Quickstart & Validation Guide: Activate Bill of Materials (Activate BOM)

**Feature**: Activate BOM (`bom-002-activate-bom`)  
**Created**: 2026-07-28  

## Validation Scenarios

### Scenario 1: Happy Path — Activate Draft BOM

**Prerequisites**: A DRAFT BOM exists for Product A with at least 1 component item.

**HTTP Request**:
```http
POST /api/boms/123e4567-e89b-12d3-a456-426614174000/activate
Authorization: Bearer <PLANNER_TOKEN>
```

**Expected Response** (`200 OK`):
```json
{
  "success": true,
  "data": {
    "id": "123e4567-e89b-12d3-a456-426614174000",
    "finishedProductId": "987e6543-e89b-12d3-a456-426614174000",
    "version": 1,
    "bomStatusId": "<ACTIVE_STATUS_UUID>",
    "createdBy": "456e7890-e89b-12d3-a456-426614174000",
    "createdAt": "2026-07-28T10:00:00Z"
  },
  "message": "OK",
  "timestamp": "2026-07-28T10:05:00Z"
}
```

---

### Scenario 2: Auto-Deactivate Existing Active BOM

**Prerequisites**: Product A has BOM v1 (`ACTIVE`) and BOM v2 (`DRAFT` with 1+ items).

**HTTP Request**:
```http
POST /api/boms/<BOM_V2_UUID>/activate
Authorization: Bearer <PLANNER_TOKEN>
```

**Expected Result**:
- BOM v2 status changes to `ACTIVE`.
- BOM v1 status changes to `INACTIVE`.
- Single SQL transaction guarantees exactly 1 `ACTIVE` version.

---

### Scenario 3: Reject Empty BOM Activation

**Prerequisites**: BOM v3 (`DRAFT`) exists for Product B with 0 items.

**HTTP Request**:
```http
POST /api/boms/<EMPTY_BOM_UUID>/activate
Authorization: Bearer <PLANNER_TOKEN>
```

**Expected Response** (`400 Bad Request`):
```json
{
  "success": false,
  "errorCode": "INVALID_INPUT",
  "message": "Cannot activate an empty BOM (must contain at least 1 item)",
  "timestamp": "2026-07-28T10:05:00Z"
}
```

---

### Scenario 4: Reject Activation of Historical Inactive BOM

**Prerequisites**: BOM v1 is in `INACTIVE` status.

**HTTP Request**:
```http
POST /api/boms/<INACTIVE_BOM_UUID>/activate
Authorization: Bearer <PLANNER_TOKEN>
```

**Expected Response** (`400 Bad Request`):
```json
{
  "success": false,
  "errorCode": "INVALID_INPUT",
  "message": "Only DRAFT BOMs can be activated",
  "timestamp": "2026-07-28T10:05:00Z"
}
```
