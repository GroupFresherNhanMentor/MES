# Quickstart & Validation Guide: Get BOM List and BOM Detail (Query BOMs)

**Feature**: Query BOMs (`bom-004-query-boms`)  
**Created**: 2026-07-28  

## Validation Scenarios

### Scenario 1: Get Paginated BOM List with Filter

**HTTP Request**:
```http
GET /api/boms?page=0&size=10&finishedProductId=<PRODUCT_UUID>&bomStatusId=<STATUS_UUID>
Authorization: Bearer <VALID_TOKEN>
```

**Expected Response** (`200 OK`):
```json
{
  "success": true,
  "data": {
    "items": [
      {
        "id": "123e4567-e89b-12d3-a456-426614174000",
        "finishedProductId": "<PRODUCT_UUID>",
        "version": 1,
        "bomStatusId": "<STATUS_UUID>",
        "createdBy": "11111111-1111-1111-1111-111111111111",
        "createdAt": "2026-07-28T10:00:00Z",
        "items": []
      }
    ],
    "totalElements": 1,
    "totalPages": 1,
    "pageNumber": 0,
    "pageSize": 10
  },
  "message": "OK"
}
```

---

### Scenario 2: Get BOM Detail by ID

**HTTP Request**:
```http
GET /api/boms/<BOM_UUID>
Authorization: Bearer <VALID_TOKEN>
```

**Expected Response** (`200 OK`):
```json
{
  "success": true,
  "data": {
    "id": "<BOM_UUID>",
    "finishedProductId": "<PRODUCT_UUID>",
    "version": 1,
    "bomStatusId": "<STATUS_UUID>",
    "createdBy": "11111111-1111-1111-1111-111111111111",
    "createdAt": "2026-07-28T10:00:00Z",
    "items": [
      {
        "id": "22222222-2222-2222-2222-222222222222",
        "bomId": "<BOM_UUID>",
        "materialProductId": "<MATERIAL_PRODUCT_UUID>",
        "quantityPerUnit": 2.5000,
        "unit": "PCS",
        "scrapRate": 0.0200
      }
    ]
  },
  "message": "OK"
}
```
