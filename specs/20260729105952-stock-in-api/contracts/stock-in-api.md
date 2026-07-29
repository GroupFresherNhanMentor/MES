# REST API Contracts: Stock-In & Stock Lot Search

## 1. POST /api/stock-in

Receives physical goods into a warehouse, creates or links the specified stock lot, updates stock balance, and logs a `PURCHASE_IN` movement.

### Request Body
```json
{
  "productId": "9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d",
  "warehouseId": "a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d",
  "locationId": "f1e2d3c4-b5a6-9788-7654-3210fedcba98",
  "lotNumber": "LOT-2026-RAW-001",
  "lotTypeId": "3c7a8b9c-0d1e-2f3a-4b5c-6d7e8f9a0b1c",
  "expiryDate": "2027-12-31",
  "quantity": 150.00,
  "referenceNo": "PO-2026-08912",
  "reason": "Initial raw material receipt from supplier"
}
```

### Responses
- **201 Created**: Successful stock-in operation.
```json
{
  "code": 1000,
  "message": "Stock received successfully",
  "result": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "productId": "9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d",
    "lotId": "6ba7b810-9dad-11d1-80b4-00c04fd430c8",
    "toWarehouseId": "a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d",
    "toLocationId": "f1e2d3c4-b5a6-9788-7654-3210fedcba98",
    "quantity": 150.00,
    "referenceNo": "PO-2026-08912",
    "createdAt": "2026-07-29T11:15:00Z"
  }
}
```
- **400 Bad Request**: Validation error (e.g. quantity <= 0, or lot belongs to another product).
```json
{
  "code": 1001,
  "message": "Stock lot 'LOT-2026-RAW-001' belongs to a different product"
}
```

---

## 2. GET /api/stock-lots

Paginated search endpoint for stock lots with filtering capabilities.

### Query Parameters
- `productId` (UUID, optional): Filter lots by product ID.
- `lotTypeId` (UUID, optional): Filter lots by lot type ID.
- `lotNumber` (String, optional): Filter by lot number substring.
- `expiryBefore` (LocalDate, optional): Filter lots expiring on or before date.
- `page` (Integer, default `0`): Page index.
- `size` (Integer, default `20`): Page size.
- `sort` (List<String>, optional): E.g. `lotNumber,asc` or `createdAt,desc`.

### Response
- **200 OK**:
```json
{
  "code": 1000,
  "message": "OK",
  "result": {
    "items": [
      {
        "id": "6ba7b810-9dad-11d1-80b4-00c04fd430c8",
        "lotNumber": "LOT-2026-RAW-001",
        "productId": "9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d",
        "lotTypeId": "3c7a8b9c-0d1e-2f3a-4b5c-6d7e8f9a0b1c",
        "expiryDate": "2027-12-31",
        "createdAt": "2026-07-29T11:15:00Z"
      }
    ],
    "totalElements": 1,
    "totalPages": 1,
    "pageNumber": 0,
    "pageSize": 20
  }
}
```
