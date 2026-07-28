# Quickstart & Validation Guide: Bill of Materials Versioning (BOM Versioning)

**Feature**: BOM Versioning (`bom-003-bom-versioning`)  
**Created**: 2026-07-28  

## Validation Scenarios

### Scenario 1: Happy Path — Create New Version from Existing BOM

**Prerequisites**: Product A has BOM v1 (`ACTIVE`) with 3 items.

**HTTP Request**:
```http
POST /api/boms/<SOURCE_BOM_V1_UUID>/new-version
Authorization: Bearer <PLANNER_TOKEN>
```

**Expected Response** (`201 Created`):
```json
{
  "success": true,
  "data": {
    "id": "<NEW_BOM_V2_UUID>",
    "finishedProductId": "<PRODUCT_A_UUID>",
    "version": 2,
    "bomStatusId": "<DRAFT_STATUS_UUID>",
    "createdBy": "<PLANNER_USER_UUID>",
    "createdAt": "2026-07-28T11:00:00Z"
  },
  "message": "Created",
  "timestamp": "2026-07-28T11:00:00Z"
}
```

**Database State Verification**:
- A new BOM header is created with `version = 2` and `status = DRAFT`.
- Exactly 3 new `bom_items` are created linking to `<NEW_BOM_V2_UUID>` with cloned quantities and scrap rates.

---

### Scenario 2: Reject Modification on ACTIVE or INACTIVE BOM

**Prerequisites**: BOM v1 is in `ACTIVE` or `INACTIVE` status.

**HTTP Request**:
```http
POST /api/boms/<ACTIVE_BOM_V1_UUID>/items
Authorization: Bearer <PLANNER_TOKEN>
Content-Type: application/json

{
  "materialProductId": "987e6543-e89b-12d3-a456-426614174000",
  "quantityPerUnit": 1.5,
  "scrapRate": 0.01
}
```

**Expected Response** (`400 Bad Request`):
```json
{
  "success": false,
  "errorCode": "INVALID_INPUT",
  "message": "Only DRAFT BOMs can be modified; create a new version instead",
  "timestamp": "2026-07-28T11:05:00Z"
}
```
