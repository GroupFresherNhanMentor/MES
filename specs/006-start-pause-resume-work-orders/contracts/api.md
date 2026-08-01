# API Contract: Start, Pause, and Resume Work Orders

## Endpoint 1: Start Production

### `POST /api/work-orders/{id}/start`

Starts production execution on a Work Order.

#### Security
- **Bearer Token**: Required (JWT)
- **Required Roles**: `OPERATOR`, `PLANNER`, `ADMIN`

#### Request Body
```json
{
  "machineId": "019facbf-7000-7000-8000-000000000001"
}
```

`productionLineId` and `operatorId` are optional. When `operatorId` is omitted, the authenticated user is recorded as the operator.

#### Success Response (`200 OK`)
```json
{
  "code": 1000,
  "message": "Production started successfully",
  "data": {
    "id": "019facbf-1000-7000-8000-000000000001",
    "code": "WO-20260730-001",
    "statusName": "IN_PROGRESS",
    "createdAt": "2026-07-30T10:00:00Z"
  }
}
```

---

## Endpoint 2: Pause Production

### `POST /api/work-orders/{id}/pause`

Temporarily pauses an in-progress Work Order.

#### Security
- **Bearer Token**: Required (JWT)
- **Required Roles**: `OPERATOR`, `PLANNER`, `ADMIN`

#### Request Body
None

#### Success Response (`200 OK`)
```json
{
  "code": 1000,
  "message": "Production paused successfully",
  "data": {
    "id": "019facbf-1000-7000-8000-000000000001",
    "code": "WO-20260730-001",
    "statusName": "PAUSED",
    "createdAt": "2026-07-30T10:00:00Z"
  }
}
```

---

## Endpoint 3: Resume Production

### `POST /api/work-orders/{id}/resume`

Resumes a paused Work Order.

#### Security
- **Bearer Token**: Required (JWT)
- **Required Roles**: `OPERATOR`, `PLANNER`, `ADMIN`

#### Request Body
None

#### Success Response (`200 OK`)
```json
{
  "code": 1000,
  "message": "Production resumed successfully",
  "data": {
    "id": "019facbf-1000-7000-8000-000000000001",
    "code": "WO-20260730-001",
    "statusName": "IN_PROGRESS",
    "createdAt": "2026-07-30T10:00:00Z"
  }
}
```
