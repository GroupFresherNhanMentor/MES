# Contract: Stock Transfer API

## Endpoint

`POST /api/stock-transfers`

### Headers
- `Content-Type: application/json`
- `Authorization: Bearer <jwt_token>`

---

### Request Body

```json
{
  "fromWarehouseId": "019facbf-0000-7000-8000-000000000001",
  "fromLocationId": "019facbf-0000-7000-8000-000000000010",
  "toWarehouseId": "019facbf-0000-7000-8000-000000000001",
  "toLocationId": "019facbf-0000-7000-8000-000000000020",
  "productId": "019facbf-0000-7000-8000-000000000100",
  "lotId": "019facbf-0000-7000-8000-000000000200",
  "quantity": 20.00
}
```

---

### Response 200 OK

```json
{
  "status": "SUCCESS",
  "message": "Stock transfer completed successfully",
  "data": {
    "transferOutMovement": {
      "id": "019facbf-0000-7000-8000-000000000301",
      "referenceNumber": "TRF-20260730-WH1/A01->WH1/A02",
      "quantity": 20.00
    },
    "transferInMovement": {
      "id": "019facbf-0000-7000-8000-000000000302",
      "referenceNumber": "TRF-20260730-WH1/A01->WH1/A02",
      "quantity": 20.00
    },
    "sourceBalance": {
      "quantity": 30.00
    },
    "destinationBalance": {
      "quantity": 20.00
    }
  },
  "timestamp": "2026-07-30T12:00:00Z"
}
```

---

### Error Responses

#### 400 Bad Request (Validation Error)
- Transfer quantity <= 0
- Identical location IDs (`fromLocationId == toLocationId`)

#### 409 Conflict / Insufficient Stock
- Source location available stock balance is insufficient (`< quantity`)

#### 404 Not Found
- Source warehouse, location, product, or lot not found
