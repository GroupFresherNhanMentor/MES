# API Contract: Inventory Reference Lookup Endpoints

## 1. GET /api/lot-types
Returns list of all available lot types.

- **Request**: None
- **Response**: HTTP 200 OK
```json
{
  "success": true,
  "data": [
    {
      "id": "019f...",
      "name": "RAW_MATERIAL",
      "description": "Raw material lot"
    }
  ],
  "message": "OK",
  "timestamp": "2026-07-30T09:58:00"
}
```

---

## 2. GET /api/stock-statuses
Returns list of all available stock statuses.

- **Request**: None
- **Response**: HTTP 200 OK
```json
{
  "success": true,
  "data": [
    {
      "id": "019f...",
      "name": "AVAILABLE",
      "description": "Available stock"
    }
  ],
  "message": "OK",
  "timestamp": "2026-07-30T09:58:00"
}
```

---

## 3. GET /api/movement-types
Returns list of all available stock movement categories.

- **Request**: None
- **Response**: HTTP 200 OK
```json
{
  "success": true,
  "data": [
    {
      "id": "019f...",
      "name": "PURCHASE_IN",
      "description": "Purchase receipt"
    }
  ],
  "message": "OK",
  "timestamp": "2026-07-30T09:58:00"
}
```
