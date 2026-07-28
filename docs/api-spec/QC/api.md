# Quality Control — API Specification

**Base URL:** `/api/quality-inspections`  
**Auth:** All endpoints require `Authorization: Bearer <accessToken>`  
**Response envelope:** `ApiResponse<T>`

---

## 1. Lookup Tables (Master Data)

### GET `/statuses`
> **Roles:** All authenticated

**Response `200`:**
```json
[
  { "id": "uuid", "name": "PENDING_INSPECTION", "description": "Inspection has not been performed yet" },
  { "id": "uuid", "name": "PASSED",             "description": "Lot passed quality inspection and is available" },
  { "id": "uuid", "name": "FAILED",             "description": "Lot failed quality inspection" },
  { "id": "uuid", "name": "ON_HOLD",            "description": "Lot is on hold pending further investigation" },
  { "id": "uuid", "name": "REWORK_REQUIRED",    "description": "Lot requires rework before reinspection" },
  { "id": "uuid", "name": "SCRAPPED",           "description": "Lot has been scrapped and written off" }
]
```

---

### POST `/statuses`
> **Roles:** `ADMIN`

**Request body:** `{ "name": "string", "description": "string" }`
**Response `201`:** `QcStatusDto`

---

### GET `/actions`
> **Roles:** All authenticated

**Response `200`:**
```json
[
  { "id": "uuid", "name": "HOLD",   "description": "Hold the lot for further investigation" },
  { "id": "uuid", "name": "REWORK", "description": "Send the lot back for rework" },
  { "id": "uuid", "name": "SCRAP",  "description": "Scrap the defective lot" }
]
```

---

### POST `/actions`
> **Roles:** `ADMIN`

**Request body:** `{ "name": "string", "description": "string" }`
**Response `201`:** `QcActionDto`

---

### GET `/defect-types`
> **Roles:** All authenticated

**Response `200`:**
```json
[
  { "id": "uuid", "code": "SCRATCH",          "name": "Scratch",          "description": "Surface scratch or abrasion" },
  { "id": "uuid", "code": "DIMENSION_ERROR",  "name": "Dimension Error",  "description": "Product dimensions out of tolerance" },
  { "id": "uuid", "code": "WEIGHT_ERROR",     "name": "Weight Error",     "description": "Product weight out of specification" },
  { "id": "uuid", "code": "COLOR_DEFECT",     "name": "Color Defect",     "description": "Incorrect color or uneven coating" },
  { "id": "uuid", "code": "CRACK",            "name": "Crack",            "description": "Structural crack or fracture" },
  { "id": "uuid", "code": "CONTAMINATION",    "name": "Contamination",    "description": "Foreign material present in product" },
  { "id": "uuid", "code": "FUNCTIONAL_FAIL",  "name": "Functional Failure","description": "Product does not function as expected" },
  { "id": "uuid", "code": "ASSEMBLY_ERROR",   "name": "Assembly Error",   "description": "Incorrect or incomplete assembly" },
  { "id": "uuid", "code": "LABEL_ERROR",      "name": "Label Error",      "description": "Missing, incorrect, or unreadable label" },
  { "id": "uuid", "code": "OTHER",            "name": "Other",            "description": "Defect not covered by other categories" }
]
```

---

### POST `/defect-types`
> **Roles:** `ADMIN`

**Request body:** `{ "code": "string", "name": "string", "description": "string" }`
**Response `201`:** `DefectTypeDto`

---

## 2. Quality Inspections

### GET `/`
> **Roles:** `ADMIN` · `QC_INSPECTOR` · `FACTORY_MANAGER`

**Query params:** `page` (default 0) · `size` (default 20) · `statusId` · `workOrderId` · `productId`

**Response `200`: `PageResponse<QualityInspectionDto>`**
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
  "qcStatusName": "PENDING_INSPECTION",
  "createdAt": "instant"
}
```

---

### GET `/{id}`
> **Roles:** `ADMIN` · `QC_INSPECTOR` · `FACTORY_MANAGER`

**Response `200`:** `QualityInspectionDto` (includes `results`)

---

### POST `/`
> **Roles:** `ADMIN` · `QC_INSPECTOR`
> **Note:** Normally auto-created after Complete Production.

**Request body:**
```json
{
  "workOrderId": "uuid",
  "productId": "uuid",
  "lotId": "uuid",
  "quantity": 100.0,
  "qcStatusId": "uuid"
}
```
**Response `201`:** `QualityInspectionDto`

---

## 3. QC Actions (Core Business Logic)

### POST `/{inspectionId}/pass`

> **Roles:** `ADMIN` · `QC_INSPECTOR`  
> **QC status:** `PENDING_INSPECTION → PASSED`  
> **Stock:** `QUALITY_INSPECTION → AVAILABLE` (số lượng = `passedQuantity`)  
> **Movement:** `QC_PASS`

**Request body:**
```json
{
  "passedQuantity": 95.0,
  "note": "All dimensions within tolerance"
}
```

**Validation:**
- `passedQuantity` > 0
- `passedQuantity` ≤ `quantity - SUM(is_pass=true) - SUM(SCRAP) - SUM(HOLD)` (REWORK không count vào remaining)

**Response `200`:**
```json
{
  "resultId": "uuid",
  "qcStatusName": "PASSED",
  "stockMovementId": "uuid",
  "message": "Pass QC thành công"
}
```

---

### POST `/{inspectionId}/fail`

> **Roles:** `ADMIN` · `QC_INSPECTOR`  
> Chọn 1 trong 3 action để quyết định xử lý hàng lỗi

**Request body:**
```json
{
  "failedQuantity": 5.0,
  "actionId": "uuid {SCRAP|HOLD|REWORK}",
  "defectTypeId": "uuid",
  "reason": "Surface scratch exceeds tolerance",
  "note": "string"
}
```

**Validation:**
- `failedQuantity` > 0
- `failedQuantity` ≤ `quantity - SUM(is_pass=true) - SUM(SCRAP) - SUM(HOLD)` (REWORK không count)
- `actionId` phải là UUID của 1 trong 3 action: `SCRAP`, `HOLD`, `REWORK`
- `defectTypeId` + `reason` — bắt buộc

**Transitions theo action:**

| Action | QC status | Stock transition | Movement |
|--------|-----------|-----------------|----------|
| **SCRAP** | `FAILED` | `QUALITY_INSPECTION → SCRAPPED` | `SCRAP` |
| **HOLD** | `ON_HOLD` | `QUALITY_INSPECTION → ON_HOLD` | `QC_HOLD` |
| **REWORK** | `REWORK_REQUIRED` | `QUALITY_INSPECTION` *(giữ nguyên)* | *(không tạo)* |

> **Lưu ý REWORK:** Operator rework xong → QC Inspector gọi tiếp **pass()** hoặc **fail()** trên cùng inspection. Không cần API riêng.

**Response `200`:**
```json
{
  "resultId": "uuid",
  "qcStatusName": "FAILED | ON_HOLD | REWORK_REQUIRED",
  "stockMovementId": "uuid",
  "message": "Fail QC thành công"
}
```

---

## 4. QC Status & Transition Summary

```
                          ┌────────────────────────────┐
                          │     PENDING_INSPECTION      │  ◄── Auto tạo sau Complete Production
                          │     Stock: QUALITY_INSPECTION│
                          └─────────────┬───────────────┘
                                        │
            ┌───────────────┬───────────┼───────────┐
            │               │           │           │
            ▼               ▼           ▼           ▼
        pass()         fail()       fail()       fail()
                   action=SCRAP  action=HOLD  action=REWORK
            │               │           │           │
            ▼               ▼           ▼           ▼
     ┌──────────┐    ┌──────────┐ ┌──────────┐ ┌───────────────┐
     │  PASSED  │    │  FAILED  │ │ ON_HOLD  │ │REWORK_REQUIRED│
     │  Stock:  │    │  Stock:  │ │ Stock:   │ │  Stock:       │
     │ AVAILABLE│    │SCRAPPED  │ │ ON_HOLD  │ │QUALITY_INSPECT│
     └──────────┘    └──────────┘ └──────────┘ └───────┬───────┘
                                                         │
                                                  (Operator rework)
                                                         │
                                                  pass() / fail() lại
                                                         │
                                                         ▼
                                               ┌────────────────────┐
                                               │ quay vòng PENDING  │
                                               │ hoặc chuyển tiếp   │
                                               └────────────────────┘
```

---

## 5. Error Codes

| Error code | HTTP | Khi nào |
|-----------|------|---------|
| `INSPECTION_NOT_FOUND` | 404 | inspection ID không tồn tại |
| `INSPECTION_ALREADY_CLOSED` | 400 | inspection đã PASSED hoặc all SCRAPPED |
| `INSUFFICIENT_REMAINING_QUANTITY` | 400 | pass/fail vượt quá số lượng còn lại |
| `INVALID_QC_ACTION` | 400 | action ID không hợp lệ |
| `DEFECT_TYPE_REQUIRED` | 400 | fail QC nhưng thiếu defectTypeId |
| `REASON_REQUIRED` | 400 | fail QC nhưng thiếu reason |
| `INVALID_STATUS_TRANSITION` | 400 | chuyển trạng thái không hợp lệ (VD: PASSED → fail) |