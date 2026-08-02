# API Contract: Release and Cancel Work Orders

## Endpoint 1: Release Reserved Materials

### `POST /api/work-orders/{id}/release-materials`

Releases all reserved materials associated with a Work Order back into available stock.
The operation is idempotent: it releases only the outstanding net reservation for each original stock lot.

#### Security
- **Bearer Token**: Required (JWT)
- **Required Roles**: `PLANNER`, `ADMIN`

#### Path Parameters
- `id` (UUID, required): Work Order ID

#### Request Headers
- `Authorization: Bearer <token>`
- `Content-Type: application/json`

#### Request Body
None

#### Success Response (`200 OK`)
```json
{
  "code": 1000,
  "message": "Materials released successfully",
  "data": {
    "id": "019facbf-1000-7000-8000-000000000001",
    "code": "WO-20260730-001",
    "finishedProductId": "019facbf-2000-7000-8000-000000000002",
    "bomId": "019facbf-3000-7000-8000-000000000003",
    "plannedQuantity": 100.0,
    "statusId": "019facbf-4000-7000-8000-000000000004",
    "statusName": "PLANNED",
    "materials": [
      {
        "id": "019facbf-5000-7000-8000-000000000005",
        "materialProductId": "019facbf-6000-7000-8000-000000000006",
        "materialProductName": "Steel Sheet A",
        "requiredQuantity": 200.0,
        "reservedQuantity": 0.0,
        "consumedQuantity": 0.0
      }
    ],
    "createdAt": "2026-07-30T10:00:00Z"
  }
}
```

#### Error Responses
- **`400 Bad Request`** (Invalid State / Production already started):
```json
{
  "code": 4001,
  "message": "Cannot release materials for work order in IN_PROGRESS or COMPLETED status",
  "data": null
}
```
- **`400 Bad Request`** (Reserved balance integrity failure): the system could not lock or debit the required `RESERVED` balance. No stock movement or status change is persisted.
- **`404 Not Found`**:
```json
{
  "code": 4004,
  "message": "Work Order not found",
  "data": null
}
```

---

## Endpoint 2: Cancel Work Order

### `POST /api/work-orders/{id}/cancel`

Cancels a Work Order and automatically releases any reserved materials.

#### Security
- **Bearer Token**: Required (JWT)
- **Required Roles**: `PLANNER`, `ADMIN`

#### Path Parameters
- `id` (UUID, required): Work Order ID

#### Request Headers
- `Authorization: Bearer <token>`

#### Request Body
None

#### Success Response (`200 OK`)
```json
{
  "code": 1000,
  "message": "Work order cancelled successfully",
  "data": {
    "id": "019facbf-1000-7000-8000-000000000001",
    "code": "WO-20260730-001",
    "finishedProductId": "019facbf-2000-7000-8000-000000000002",
    "bomId": "019facbf-3000-7000-8000-000000000003",
    "plannedQuantity": 100.0,
    "statusId": "019facbf-4000-7000-8000-000000000009",
    "statusName": "CANCELLED",
    "materials": [
      {
        "id": "019facbf-5000-7000-8000-000000000005",
        "materialProductId": "019facbf-6000-7000-8000-000000000006",
        "materialProductName": "Steel Sheet A",
        "requiredQuantity": 200.0,
        "reservedQuantity": 0.0,
        "consumedQuantity": 0.0
      }
    ],
    "createdAt": "2026-07-30T10:00:00Z"
  }
}
```

#### Error Responses
- **`400 Bad Request`** (Invalid State):
```json
{
  "code": 4001,
  "message": "Cannot cancel work order in IN_PROGRESS or COMPLETED status",
  "data": null
}
```
