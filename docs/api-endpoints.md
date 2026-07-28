# FactoryFlow — API Endpoint Reference

> **Advisory only — not the source of truth.**
> The real source of truth is the controller code and DTOs in `be/src/main/java/`.
> When planning or coding, verify against actual controllers.
> If your implementation differs from what is listed here, **update this file immediately**.

**Base URL:** `/api`  
**Auth:** All endpoints (except `POST /auth/login`) require `Authorization: Bearer <accessToken>`  
**Response envelope:** All responses wrap in `ApiResponse<T>`

```json
// Success
{ "success": true, "data": { ... }, "message": "OK", "timestamp": "..." }

// Error
{ "success": false, "errorCode": "NOT_FOUND", "message": "...", "timestamp": "..." }
```

**Roles:** `ADMIN` · `WAREHOUSE_MANAGER` · `PLANNER` · `OPERATOR` · `QC_INSPECTOR` · `MAINTENANCE_ENGINEER` · `FACTORY_MANAGER` · `AUDITOR`

---

## 1. Authentication

### POST `/auth/login`
> Public — no token required

**Request body:**
```json
{ "username": "string", "password": "string" }
```
**Response `200`:**
```json
{ "accessToken": "string", "refreshToken": "string" }
```

---

### POST `/auth/refresh`
> Public — no token required

**Request body:**
```json
{ "refreshToken": "string" }
```
**Response `200`:**
```json
{ "accessToken": "string", "refreshToken": "string" }
```

---

## 2. Users

### GET `/users`
> **Roles:** `ADMIN`

**Query params:** `page` (default 0) · `size` (default 20)

**Response `200`:**
```json
{
  "content": [{ "id": "uuid", "username": "string", "fullName": "string", "active": true, "createdAt": "instant" }],
  "page": 0, "size": 20, "totalElements": 10, "totalPages": 1
}
```

---

### GET `/users/{id}`
> **Roles:** `ADMIN`

**Response `200`:**
```json
{ "id": "uuid", "username": "string", "fullName": "string", "active": true, "createdAt": "instant" }
```

---

### POST `/users`
> **Roles:** `ADMIN`

**Request body:**
```json
{ "username": "string (3–100)", "password": "string (8–255)", "fullName": "string" }
```
**Response `201`:** `UserDto`

---

### PUT `/users/{id}`
> **Roles:** `ADMIN`

**Request body:**
```json
{ "fullName": "string" }
```
**Response `200`:** `UserDto`

---

### PATCH `/users/{id}/activate`
> **Roles:** `ADMIN`

**Response `200`:** `UserDto`

---

### PATCH `/users/{id}/deactivate`
> **Roles:** `ADMIN`

**Response `200`:** `UserDto`

---

## 3. Roles

### GET `/roles`
> **Roles:** `ADMIN`

**Response `200`:**
```json
[{ "id": "uuid", "name": "string", "description": "string", "permissionNames": ["string"] }]
```

---

### GET `/roles/{id}`
> **Roles:** `ADMIN`

**Response `200`:** `RoleDto`

---

### POST `/roles`
> **Roles:** `ADMIN`

**Request body:**
```json
{ "name": "string", "description": "string" }
```
**Response `201`:** `RoleDto`

---

### PUT `/roles/{id}`
> **Roles:** `ADMIN`

**Request body:**
```json
{ "name": "string", "description": "string" }
```
**Response `200`:** `RoleDto`

---

### DELETE `/roles/{id}`
> **Roles:** `ADMIN`

**Response `200`:** no data

---

### POST `/roles/{id}/permissions`
> **Roles:** `ADMIN`

**Request body:**
```json
{ "permissionIds": ["uuid"] }
```
**Response `200`:** `RoleDto`

---

## 4. Permissions

### GET `/permissions`
> **Roles:** `ADMIN`

**Response `200`:**
```json
[{ "id": "uuid", "name": "string", "description": "string" }]
```

---

### GET `/permissions/{id}`
> **Roles:** `ADMIN`

**Response `200`:** `PermissionDto`

---

### POST `/permissions`
> **Roles:** `ADMIN`

**Request body:**
```json
{ "name": "string", "description": "string" }
```
**Response `201`:** `PermissionDto`

---

### DELETE `/permissions/{id}`
> **Roles:** `ADMIN`

**Response `200`:** no data

---

## 5. Products & Materials

### GET `/products`
> **Roles:** All authenticated

**Query params:** `page` · `size` · `keyword` (name/code search)

**Response `200`:** `PageResponse<ProductDto>`
```json
{ "id": "uuid", "code": "string", "name": "string", "productTypeId": "uuid", "unitId": "uuid", "productStatusId": "uuid", "version": 0, "createdAt": "instant", "createdBy": "uuid" }
```

---

### GET `/products/{id}`
> **Roles:** All authenticated

**Response `200`:** `ProductDto`

---

### POST `/products`
> **Roles:** `ADMIN`

**Request body:**
```json
{ "code": "string", "name": "string", "productTypeId": "uuid", "unitId": "uuid", "productStatusId": "uuid" }
```
**Response `201`:** `ProductDto`

---

### PUT `/products/{id}`
> **Roles:** `ADMIN`

**Request body:**
```json
{ "name": "string", "productStatusId": "uuid" }
```
**Response `200`:** `ProductDto`

---

### DELETE `/products/{id}`
> **Roles:** `ADMIN`

**Response `200`:** no data

---

### GET `/products/types`
> **Roles:** All authenticated

**Response `200`:** `[{ "id": "uuid", "name": "string" }]`

---

### GET `/products/statuses`
> **Roles:** All authenticated

**Response `200`:** `[{ "id": "uuid", "name": "string" }]`

---

## 6. Warehouses

### GET `/warehouses`
> **Roles:** All authenticated

**Query params:** `page` · `size`

**Response `200`:** `PageResponse<WarehouseDto>`
```json
{ "id": "uuid", "code": "string", "name": "string", "address": "string", "warehouseStatusId": "uuid", "createdAt": "instant", "createdBy": "uuid", "updatedAt": "instant", "updatedBy": "uuid" }
```

---

### GET `/warehouses/{id}`
> **Roles:** All authenticated

**Response `200`:** `WarehouseDto`

---

### POST `/warehouses`
> **Roles:** `ADMIN`

**Request body:**
```json
{ "code": "string", "name": "string", "address": "string", "warehouseStatusId": "uuid" }
```
**Response `201`:** `WarehouseDto`

---

### PUT `/warehouses/{id}`
> **Roles:** `ADMIN`

**Request body:**
```json
{ "name": "string", "address": "string", "warehouseStatusId": "uuid" }
```
**Response `200`:** `WarehouseDto`

---

### DELETE `/warehouses/{id}`
> **Roles:** `ADMIN`

**Response `200`:** no data

---

### GET `/warehouses/statuses`
> **Roles:** All authenticated

**Response `200`:** `[{ "id": "uuid", "name": "string" }]`

---

## 7. Warehouse Locations

### GET `/warehouses/{warehouseId}/locations`
> **Roles:** All authenticated

**Response `200`:** `PageResponse<WarehouseLocationDto>`
```json
{ "id": "uuid", "warehouseId": "uuid", "code": "string", "name": "string", "locationStatusId": "uuid", "createdAt": "instant", "updatedAt": "instant" }
```

---

### GET `/warehouses/{warehouseId}/locations/{id}`
> **Roles:** All authenticated

**Response `200`:** `WarehouseLocationDto`

---

### POST `/warehouses/{warehouseId}/locations`
> **Roles:** `ADMIN` · `WAREHOUSE_MANAGER`

**Request body:**
```json
{ "code": "string", "name": "string", "locationStatusId": "uuid" }
```
**Response `201`:** `WarehouseLocationDto`

---

### PUT `/warehouses/{warehouseId}/locations/{id}`
> **Roles:** `ADMIN` · `WAREHOUSE_MANAGER`

**Request body:**
```json
{ "name": "string", "locationStatusId": "uuid" }
```
**Response `200`:** `WarehouseLocationDto`

---

### DELETE `/warehouses/{warehouseId}/locations/{id}`
> **Roles:** `ADMIN`

**Response `200`:** no data

---

### GET `/warehouses/{warehouseId}/locations/statuses`
> **Roles:** All authenticated

**Response `200`:** `[{ "id": "uuid", "name": "string" }]`

---

## 8. Machines

### GET `/machines`
> **Roles:** All authenticated

**Query params:** `page` · `size` · `keyword`

**Response `200`:** `PageResponse<MachineDto>`
```json
{ "id": "uuid", "productionLineId": "uuid", "code": "string", "name": "string", "machineStatusId": "uuid", "createdAt": "instant", "updatedAt": "instant" }
```

---

### GET `/machines/{id}`
> **Roles:** All authenticated

**Response `200`:** `MachineDto`

---

### POST `/machines`
> **Roles:** `ADMIN`

**Request body:**
```json
{ "productionLineId": "uuid", "code": "string", "name": "string", "machineStatusId": "uuid" }
```
**Response `201`:** `MachineDto`

---

### PUT `/machines/{id}`
> **Roles:** `ADMIN` · `MAINTENANCE_ENGINEER`

**Request body:**
```json
{ "productionLineId": "uuid", "name": "string", "machineStatusId": "uuid" }
```
**Response `200`:** `MachineDto`

---

### DELETE `/machines/{id}`
> **Roles:** `ADMIN`

**Response `200`:** no data

---

### GET `/machines/statuses`
> **Roles:** All authenticated

**Response `200`:** `[{ "id": "uuid", "name": "string" }]`

---

## 9. Production Lines

### GET `/production-lines`
> **Roles:** All authenticated

**Query params:** `page` · `size`

**Response `200`:** `PageResponse<ProductionLineDto>`
```json
{ "id": "uuid", "code": "string", "name": "string", "lineStatusId": "uuid", "createdAt": "instant", "updatedAt": "instant" }
```

---

### GET `/production-lines/{id}`
> **Roles:** All authenticated

**Response `200`:** `ProductionLineDto`

---

### POST `/production-lines`
> **Roles:** `ADMIN`

**Request body:**
```json
{ "code": "string", "name": "string", "lineStatusId": "uuid" }
```
**Response `201`:** `ProductionLineDto`

---

### PUT `/production-lines/{id}`
> **Roles:** `ADMIN`

**Request body:**
```json
{ "name": "string", "lineStatusId": "uuid" }
```
**Response `200`:** `ProductionLineDto`

---

### DELETE `/production-lines/{id}`
> **Roles:** `ADMIN`

**Response `200`:** no data

---

### GET `/production-lines/statuses`
> **Roles:** All authenticated

**Response `200`:** `[{ "id": "uuid", "name": "string" }]`

---

## 10. BOM — Bill of Materials

### GET `/boms`
> **Roles:** `ADMIN` · `PLANNER` · `FACTORY_MANAGER` · `OPERATOR` · `QC_INSPECTOR`

**Query params:** `page` · `size`

**Response `200`:** `PageResponse<BomDto>`
```json
{ "id": "uuid", "finishedProductId": "uuid", "version": 1, "bomStatusId": "uuid", "createdBy": "uuid", "createdAt": "instant", "items": [] }
```

---

### GET `/boms/{id}`
> **Roles:** `ADMIN` · `PLANNER` · `FACTORY_MANAGER` · `OPERATOR` · `QC_INSPECTOR`

**Response `200`:** `BomDto` (includes `items`)

---

### POST `/boms`
> **Roles:** `ADMIN` · `PLANNER`  
> **SRS:** `FR-BOM-001` — Create BOM Header (Status: `DRAFT`)

**Request body:**
```json
{ "finishedProductId": "uuid", "version": 1 }
```
**Response `201`:** `BomDto`

---

### POST `/boms/{id}/activate`
> **Roles:** `ADMIN` · `PLANNER`  
> **SRS:** `FR-BOM-002` — Activate BOM (Deactivates current ACTIVE BOM for product; sets target to `ACTIVE`)

**Response `200`:** `BomDto`

---

### POST `/boms/{bomId}/items`
> **Roles:** `ADMIN` · `PLANNER`  
> Must be in `DRAFT` status

**Request body:**
```json
{ "materialProductId": "uuid", "quantityPerUnit": 1.5, "unit": "string", "scrapRate": 0.02 }
```
**Response `201`:** `BomItemDto`

---

### DELETE `/boms/{bomId}/items/{itemId}`
> **Roles:** `ADMIN` · `PLANNER`  
> Must be in `DRAFT` status

**Response `200`:** no data

---

### GET `/boms/statuses`
> **Roles:** All authenticated

**Response `200`:** `[{ "id": "uuid", "name": "string" }]`

---

## 11. Inventory

### GET `/stock-lots`
> **Roles:** `ADMIN` · `WAREHOUSE_MANAGER` · `PLANNER` · `FACTORY_MANAGER` · `AUDITOR`

**Query params:** `page` · `size` · `productId`

**Response `200`:** `PageResponse<StockLotDto>`
```json
{ "id": "uuid", "lotNumber": "string", "productId": "uuid", "lotTypeId": "uuid", "expiryDate": "date", "createdAt": "instant" }
```

---

### GET `/stock-lots/{id}`
> **Roles:** `ADMIN` · `WAREHOUSE_MANAGER` · `PLANNER` · `FACTORY_MANAGER` · `AUDITOR`

**Response `200`:** `StockLotDto`

---

### POST `/stock-lots`
> **Roles:** `ADMIN` · `WAREHOUSE_MANAGER`

**Request body:**
```json
{ "lotNumber": "string", "productId": "uuid", "lotTypeId": "uuid", "expiryDate": "date" }
```
**Response `201`:** `StockLotDto`

---

### GET `/stock-balances`
> **Roles:** `ADMIN` · `WAREHOUSE_MANAGER` · `PLANNER` · `FACTORY_MANAGER` · `AUDITOR`

**Query params:** `page` · `size` · `warehouseId` · `productId` · `locationId`

**Response `200`:** `PageResponse<StockBalanceDto>`
```json
{ "id": "uuid", "warehouseId": "uuid", "locationId": "uuid", "productId": "uuid", "lotId": "uuid", "stockStatusId": "uuid", "quantity": 100.00, "version": 1 }
```

---

### GET `/stock-movements`
> **Roles:** `ADMIN` · `WAREHOUSE_MANAGER` · `FACTORY_MANAGER` · `AUDITOR`

**Query params:** `page` · `size` · `productId` · `warehouseId` · `movementTypeId` · `from` · `to`

**Response `200`:** `PageResponse<StockMovementDto>`

---

### POST `/stock-movements`
> **Roles:** `ADMIN` · `WAREHOUSE_MANAGER`

**Request body:**
```json
{
  "movementTypeId": "uuid", "productId": "uuid", "lotId": "uuid",
  "warehouseId": "uuid", "locationId": "uuid", "quantity": 50.0,
  "fromStatusId": "uuid", "toStatusId": "uuid", "reason": "string"
}
```
**Response `201`:** `StockMovementDto`

---

### GET `/lot-types`
> **Roles:** All authenticated

**Response `200`:** `[{ "id": "uuid", "name": "string" }]`

---

### GET `/stock-statuses`
> **Roles:** All authenticated

**Response `200`:** `[{ "id": "uuid", "name": "string" }]`

---

### GET `/movement-types`
> **Roles:** All authenticated

**Response `200`:** `[{ "id": "uuid", "name": "string" }]`

---

## 12. Work Orders

### GET `/work-orders`
> **Roles:** `ADMIN` · `PLANNER` · `OPERATOR` · `FACTORY_MANAGER`

**Query params:** `page` · `size` · `statusId` · `productId`

**Response `200`:** `PageResponse<WorkOrderDto>`
```json
{
  "id": "uuid", "code": "string", "finishedProductId": "uuid", "bomId": "uuid",
  "plannedQuantity": 100.0, "plannedStartDate": "instant", "plannedEndDate": "instant",
  "priorityId": "uuid", "workOrderStatusId": "uuid", "createdBy": "uuid", "createdAt": "instant"
}
```

---

### GET `/work-orders/{id}`
> **Roles:** `ADMIN` · `PLANNER` · `OPERATOR` · `FACTORY_MANAGER`

**Response `200`:** `WorkOrderDto`

---

### POST `/work-orders`
> **Roles:** `ADMIN` · `PLANNER`

**Request body:**
```json
{
  "code": "string", "finishedProductId": "uuid", "bomId": "uuid",
  "plannedQuantity": 100.0, "plannedStartDate": "instant", "plannedEndDate": "instant",
  "priorityId": "uuid", "workOrderStatusId": "uuid"
}
```
**Response `201`:** `WorkOrderDto`

---

### PUT `/work-orders/{id}`
> **Roles:** `ADMIN` · `PLANNER`

**Request body:**
```json
{
  "code": "string", "plannedQuantity": 100.0,
  "plannedStartDate": "instant", "plannedEndDate": "instant",
  "priorityId": "uuid", "workOrderStatusId": "uuid"
}
```
**Response `200`:** `WorkOrderDto`

---

### DELETE `/work-orders/{id}`
> **Roles:** `ADMIN` · `PLANNER`

**Response `200`:** no data

---

### GET `/work-orders/{workOrderId}/materials`
> **Roles:** `ADMIN` · `PLANNER` · `WAREHOUSE_MANAGER`

**Response `200`:** `[WorkOrderMaterialDto]`
```json
{ "id": "uuid", "workOrderId": "uuid", "materialProductId": "uuid", "requiredQuantity": 10.0, "reservedQuantity": 5.0, "consumedQuantity": 0.0 }
```

---

### POST `/work-orders/{workOrderId}/materials`
> **Roles:** `ADMIN` · `PLANNER`

**Request body:**
```json
{ "materialProductId": "uuid", "requiredQuantity": 10.0 }
```
**Response `201`:** `WorkOrderMaterialDto`

---

### DELETE `/work-orders/{workOrderId}/materials/{materialId}`
> **Roles:** `ADMIN` · `PLANNER`

**Response `200`:** no data

---

### GET `/work-orders/{workOrderId}/events`
> **Roles:** `ADMIN` · `PLANNER` · `OPERATOR` · `FACTORY_MANAGER`

**Response `200`:** `[WorkOrderEventDto]`
```json
{
  "id": "uuid", "workOrderId": "uuid", "eventTypeId": "uuid",
  "machineId": "uuid", "productionLineId": "uuid", "operatorId": "uuid",
  "actualQuantity": 0.0, "goodQuantity": 0.0, "defectQuantity": 0.0, "scrapQuantity": 0.0,
  "eventTimestamp": "instant", "note": "string"
}
```

---

### POST `/work-orders/{workOrderId}/events`
> **Roles:** `OPERATOR`

**Request body:**
```json
{
  "eventTypeId": "uuid", "machineId": "uuid", "productionLineId": "uuid", "operatorId": "uuid",
  "actualQuantity": 100.0, "goodQuantity": 95.0, "defectQuantity": 3.0, "scrapQuantity": 2.0,
  "note": "string"
}
```
**Response `201`:** `WorkOrderEventDto`

---

### GET `/work-orders/statuses`
> **Roles:** All authenticated

**Response `200`:** `[{ "id": "uuid", "name": "string" }]`

---

### GET `/work-orders/priorities`
> **Roles:** All authenticated

**Response `200`:** `[{ "id": "uuid", "name": "string" }]`

---

### GET `/work-orders/event-types`
> **Roles:** All authenticated

**Response `200`:** `[{ "id": "uuid", "name": "string" }]`

---

## 13. Quality Control

### GET `/quality-inspections`
> **Roles:** `ADMIN` · `QC_INSPECTOR` · `FACTORY_MANAGER`

**Query params:** `page` · `size` · `statusId` · `workOrderId`

**Response `200`:** `PageResponse<QualityInspectionDto>`
```json
{ "id": "uuid", "workOrderId": "uuid", "productId": "uuid", "lotId": "uuid", "quantity": 100.0, "qcStatusId": "uuid", "createdAt": "instant", "results": [] }
```

---

### GET `/quality-inspections/{id}`
> **Roles:** `ADMIN` · `QC_INSPECTOR` · `FACTORY_MANAGER`

**Response `200`:** `QualityInspectionDto` (includes `results`)

---

### POST `/quality-inspections`
> **Roles:** `ADMIN` · `QC_INSPECTOR`

**Request body:**
```json
{ "workOrderId": "uuid", "productId": "uuid", "lotId": "uuid", "quantity": 100.0, "qcStatusId": "uuid" }
```
**Response `201`:** `QualityInspectionDto`

---

### DELETE `/quality-inspections/{id}`
> **Roles:** `ADMIN`

**Response `200`:** no data

---

### GET `/quality-inspections/{inspectionId}/results`
> **Roles:** `ADMIN` · `QC_INSPECTOR` · `FACTORY_MANAGER` · `AUDITOR`

**Response `200`:** `[QualityInspectionResultDto]`
```json
{
  "id": "uuid", "inspectionId": "uuid", "isPass": true, "quantity": 95.0,
  "defectTypeId": "uuid", "reason": "string", "action": "string",
  "inspectorId": "uuid", "inspectedAt": "instant", "note": "string"
}
```

---

### POST `/quality-inspections/{inspectionId}/results`
> **Roles:** `QC_INSPECTOR`

**Request body:**
```json
{
  "isPass": false, "quantity": 5.0,
  "defectTypeId": "uuid", "reason": "Scratch on surface", "action": "SCRAP",
  "inspectorId": "uuid", "note": "string"
}
```
**Response `201`:** `QualityInspectionResultDto`

---

### GET `/quality-inspections/statuses`
> **Roles:** All authenticated

**Response `200`:** `[{ "id": "uuid", "name": "string" }]`

---

### GET `/quality-inspections/defect-types`
> **Roles:** All authenticated

**Response `200`:** `[{ "id": "uuid", "code": "string", "name": "string", "description": "string" }]`

---

## 14. Maintenance

### GET `/maintenance-tickets`
> **Roles:** `ADMIN` · `MAINTENANCE_ENGINEER` · `FACTORY_MANAGER`

**Query params:** `page` · `size` · `machineId` · `statusId`

**Response `200`:** `PageResponse<MaintenanceTicketDto>`
```json
{ "id": "uuid", "machineId": "uuid", "ticketTypeId": "uuid", "priorityId": "uuid", "description": "string", "ticketStatusId": "uuid", "createdBy": "uuid", "createdAt": "instant" }
```

---

### GET `/maintenance-tickets/{id}`
> **Roles:** `ADMIN` · `MAINTENANCE_ENGINEER` · `FACTORY_MANAGER`

**Response `200`:** `MaintenanceTicketDto`

---

### POST `/maintenance-tickets`
> **Roles:** `ADMIN` · `MAINTENANCE_ENGINEER`

**Request body:**
```json
{ "machineId": "uuid", "ticketTypeId": "uuid", "priorityId": "uuid", "description": "string", "ticketStatusId": "uuid" }
```
**Response `201`:** `MaintenanceTicketDto`

---

### PUT `/maintenance-tickets/{id}`
> **Roles:** `ADMIN` · `MAINTENANCE_ENGINEER`

**Request body:**
```json
{ "description": "string", "ticketStatusId": "uuid", "priorityId": "uuid" }
```
**Response `200`:** `MaintenanceTicketDto`

---

### DELETE `/maintenance-tickets/{id}`
> **Roles:** `ADMIN`

**Response `200`:** no data

---

### GET `/maintenance-tickets/{ticketId}/downtime`
> **Roles:** `ADMIN` · `MAINTENANCE_ENGINEER` · `FACTORY_MANAGER`

**Response `200`:** `MachineDowntimeDto`
```json
{ "id": "uuid", "ticketId": "uuid", "machineId": "uuid", "startTime": "instant", "endTime": "instant", "totalDowntimeMinutes": 120, "rootCause": "string", "actionTaken": "string" }
```

---

### POST `/maintenance-tickets/{ticketId}/downtime`
> **Roles:** `ADMIN` · `MAINTENANCE_ENGINEER`

**Request body:**
```json
{ "machineId": "uuid", "startTime": "instant", "endTime": "instant", "totalDowntimeMinutes": 120, "rootCause": "string", "actionTaken": "string" }
```
**Response `201`:** `MachineDowntimeDto`

---

### GET `/maintenance-tickets/types`
> **Roles:** All authenticated

**Response `200`:** `[{ "id": "uuid", "name": "string" }]`

---

### GET `/maintenance-tickets/statuses`
> **Roles:** All authenticated

**Response `200`:** `[{ "id": "uuid", "name": "string" }]`

---

### GET `/maintenance-tickets/priorities`
> **Roles:** All authenticated

**Response `200`:** `[{ "id": "uuid", "name": "string" }]`

---


## Role × Endpoint Matrix

| Module | `ADMIN` | `WH_MGR` | `PLANNER` | `OPERATOR` | `QC` | `MAINT` | `MGR` | `AUDITOR` |
|--------|:-------:|:---------:|:---------:|:----------:|:----:|:-------:|:-----:|:---------:|
| Users | CRUD | — | — | — | — | — | R | — |
| Roles / Permissions | CRUD | — | — | — | — | — | — | — |
| Products | CRUD | R | R | R | R | R | R | R |
| Warehouses | CRUD | R | R | — | — | — | R | R |
| Locations | CRUD | CRU | R | — | — | — | R | R |
| Machines | CRUD | — | R | R | — | RU | R | — |
| Production Lines | CRUD | — | R | R | — | R | R | — |
| BOM | CRU | — | CRU | R | R | — | R | — |
| Stock Lots | CRUD | CRU | R | — | R | — | R | R |
| Stock Balances | R | R | R | — | — | — | R | R |
| Stock Movements | CRUD | CRU | R | — | — | — | R | R |
| Work Orders | CRUD | — | CRUD | R | — | — | R | — |
| WO Events | R | — | R | CREATE | — | — | R | — |
| Quality Inspections | CRUD | — | — | — | CRUD | — | R | R |
| Maintenance Tickets | CRUD | — | — | — | — | CRUD | R | — |
