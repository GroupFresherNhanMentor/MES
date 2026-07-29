# Quickstart Validation Guide

Validate the `GET /api/stock-balances` endpoint:

## 1. Run Integration & Unit Tests
```bash
cd be
./mvnw test -Dtest=InventoryIntegrationTest,InventoryServiceTest
```

## 2. API Request Scenario
```http
GET /api/stock-balances?page=0&size=20&warehouseId=38997b55-4140-4611-98e2-08ee3d13b1e7
Authorization: Bearer <jwt-token>
```

**Expected Response**:
```json
{
  "success": true,
  "data": {
    "items": [
      {
        "id": "...",
        "warehouseId": "38997b55-4140-4611-98e2-08ee3d13b1e7",
        "quantity": 100.00
      }
    ],
    "totalElements": 1,
    "totalPages": 1,
    "pageNumber": 0,
    "pageSize": 20
  },
  "message": "OK",
  "timestamp": "2026-07-28T16:25:00Z"
}
```
