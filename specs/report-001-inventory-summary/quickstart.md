# Quickstart Validation Guide: Inventory Summary Report

This guide describes how to validate the **Inventory Summary Report** end-to-end.

## 1. Prerequisites

- Backend Spring Boot server running: `./mvnw spring-boot:run`
- Authenticated JWT token for `ADMIN`, `FACTORY_MANAGER`, or `AUDITOR`

## 2. Validation Scenarios

### Scenario 1: Fetch Full Inventory Summary Report (Happy Path)

**HTTP Request**:
```http
GET /api/reports/inventory-summary
Authorization: Bearer <JWT_TOKEN>
```

**Expected Outcome**:
- Status: `200 OK`
- Body:
```json
{
  "code": 200,
  "message": "OK",
  "data": [
    {
      "productCode": "PROD-001",
      "productName": "Steel Sheet 2mm",
      "warehouseName": "Main Warehouse",
      "availableQuantity": 150.00,
      "reservedQuantity": 30.00,
      "qualityInspectionQuantity": 10.00,
      "onHoldQuantity": 0.00,
      "scrappedQuantity": 5.00,
      "totalOnHand": 195.00
    }
  ]
}
```

---

### Scenario 2: Filter by Warehouse

**HTTP Request**:
```http
GET /api/reports/inventory-summary?warehouseId=00000000-0000-0000-0000-000000000001
Authorization: Bearer <JWT_TOKEN>
```

**Expected Outcome**:
- Status: `200 OK` (Only rows matching the specified warehouse)

---

### Scenario 3: Unauthorized Role Attempt

**HTTP Request**:
```http
GET /api/reports/inventory-summary
Authorization: Bearer <OPERATOR_JWT_TOKEN>
```

**Expected Outcome**:
- Status: `403 Forbidden`
