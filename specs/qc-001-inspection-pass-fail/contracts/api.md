# API Contracts: QC Inspection — Pass / Fail

**Base URL**: `/api/quality-inspections`  
**Auth**: Bearer JWT token required (except public endpoints noted)  
**Response envelope**: `ApiResponse<T>`

---

## Inspection Endpoints

### GET `/api/quality-inspections`

List inspections with pagination and filtering.

**Query params**: `page` (0), `size` (20), `statusId`, `workOrderId`, `productId`

**Roles**: ADMIN, QC_INSPECTOR, FACTORY_MANAGER

**Response 200**:
```json
{
  "content": [
    {
      "id": "uuid",
      "workOrderId": "uuid",
      "productId": "uuid",
      "productCode": "string",
      "lotId": "uuid",
      "lotNumber": "string",
      "quantity": 100.0,
      "qcStatusId": "uuid",
      "qcStatusName": "PENDING_INSPECTION",
      "createdAt": "instant"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 10,
  "totalPages": 1
}
```

> List response does NOT include `results` array — only available in detail endpoint.

---

### GET `/api/quality-inspections/{id}`

Single inspection detail with all results.

**Roles**: ADMIN, QC_INSPECTOR, FACTORY_MANAGER

**Response 200**: Same structure as list item + `results` array:
```json
{
  "id": "uuid",
  "workOrderId": "uuid",
  "productId": "uuid",
  "productCode": "string",
  "lotId": "uuid",
  "lotNumber": "string",
  "quantity": 100.0,
  "qcStatusId": "uuid",
  "qcStatusName": "PASSED",
  "createdAt": "instant",
  "results": [
    {
      "id": "uuid",
      "isPass": true,
      "quantity": 95.0,
      "defectTypeId": null,
      "reason": null,
      "actionId": null,
      "inspectorId": "uuid",
      "inspectedAt": "instant",
      "note": "All dimensions within tolerance"
    }
  ]
}
```

---

### POST `/api/quality-inspections`

Create inspection manually (normally auto-created by Complete Production).

**Roles**: ADMIN, QC_INSPECTOR

**Request body**:
```json
{
  "workOrderId": "uuid",
  "productId": "uuid",
  "lotId": "uuid",
  "quantity": 100.0,
  "qcStatusId": "uuid"
}
```

**Response 201**: `QualityInspectionDto`

---

## Pass / Fail Endpoints

### POST `/api/quality-inspections/{inspectionId}/pass`

Pass QC — move stock from QUALITY_INSPECTION to AVAILABLE.

**Roles**: ADMIN, QC_INSPECTOR

**Request body**:
```json
{
  "passedQuantity": 95.0,
  "note": "All dimensions within tolerance"
}
```

**Validation**:
- `passedQuantity` > 0
- `passedQuantity` ≤ remainingQuantity

**Behavior**:
- QC status: `PENDING_INSPECTION → PASSED`
- Stock: `QUALITY_INSPECTION → AVAILABLE` (qty = passedQuantity)
- Movement: `QC_RELEASE`
- Result: `isPass = true`, actionId = null, defectTypeId = null

**Response 200**:
```json
{
  "resultId": "uuid",
  "qcStatusName": "PASSED",
  "stockMovementId": "uuid",
  "message": "Pass QC thành công"
}
```

---

### POST `/api/quality-inspections/{inspectionId}/fail`

Fail QC — choose action: SCRAP, HOLD, or REWORK.

**Roles**: ADMIN, QC_INSPECTOR

**Request body**:
```json
{
  "failedQuantity": 5.0,
  "actionId": "uuid",
  "defectTypeId": "uuid",
  "reason": "Surface scratch exceeds tolerance",
  "note": "string"
}
```

**Validation**:
- `failedQuantity` > 0
- `failedQuantity` ≤ remainingQuantity
- `actionId` must be one of: SCRAP, HOLD, REWORK
- `defectTypeId` + `reason` — both required

**Behavior by action**:

| Action | QC Status | Stock | Movement |
|--------|-----------|-------|----------|
| SCRAP | FAILED | QUALITY_INSPECTION → SCRAPPED | SCRAP |
| HOLD | ON_HOLD | QUALITY_INSPECTION → ON_HOLD | QC_HOLD |
| REWORK | REWORK_REQUIRED | giữ QUALITY_INSPECTION | none |

**Response 200**:
```json
{
  "resultId": "uuid",
  "qcStatusName": "FAILED | ON_HOLD | REWORK_REQUIRED",
  "stockMovementId": "uuid",
  "message": "Fail QC thành công"
}
```

---

## Lookup Endpoints

### GET `/api/quality-inspections/statuses`
> Roles: All authenticated

```json
[{ "id": "uuid", "name": "PENDING_INSPECTION", "description": "Inspection has not been performed yet" }]
```

### POST `/api/quality-inspections/statuses`
> Roles: ADMIN
```json
{ "name": "string", "description": "string" }
```

---

### GET `/api/quality-inspections/actions`
> Roles: All authenticated

```json
[{ "id": "uuid", "name": "HOLD", "description": "Hold the lot for further investigation" }]
```

### POST `/api/quality-inspections/actions`
> Roles: ADMIN

---

### GET `/api/quality-inspections/defect-types`
> Roles: All authenticated

```json
[{ "id": "uuid", "code": "SCRATCH", "name": "Scratch", "description": "Surface scratch or abrasion" }]
```

### POST `/api/quality-inspections/defect-types`
> Roles: ADMIN

---

## Error Codes

| Code | HTTP | Description |
|------|------|-------------|
| INSPECTION_NOT_FOUND | 404 | inspection ID không tồn tại |
| INSPECTION_ALREADY_CLOSED | 400 | inspection đã full processed |
| INSUFFICIENT_REMAINING_QUANTITY | 400 | pass/fail vượt remaining |
| INVALID_QC_ACTION | 400 | actionId không hợp lệ |
| DEFECT_TYPE_REQUIRED | 400 | fail thiếu defectTypeId |
| REASON_REQUIRED | 400 | fail thiếu reason |
| INVALID_STATUS_TRANSITION | 400 | transition không hợp lệ |

## Roles Matrix

| Endpoint | ADMIN | QC_INSPECTOR | FACTORY_MGR | Others |
|----------|:-----:|:------------:|:-----------:|:------:|
| GET inspections | ✅ | ✅ | ✅ | — |
| GET inspection/{id} | ✅ | ✅ | ✅ | — |
| POST inspection | ✅ | ✅ | — | — |
| POST pass | ✅ | ✅ | — | — |
| POST fail | ✅ | ✅ | — | — |
| GET lookup (statuses/actions/defect-types) | ✅ | ✅ | ✅ | ✅ |
| POST lookup | ✅ | — | — | — |