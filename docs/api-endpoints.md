# FactoryFlow — API Endpoint Reference

> **Advisory only — not the source of truth.**
> The real source of truth is the controller code and DTOs in `be/src/main/java/`.
> When planning or coding, verify against actual controllers.
> If your implementation differs from what is listed here, **update this file immediately**.

**Base URL:** `/api`  
**Auth:** All endpoints except `POST /auth/login` and `POST /auth/refresh` require
`Authorization: Bearer <accessToken>`. Access JWTs expire after 15 minutes;
refresh JWTs expire after 7 days. Refresh is stateless: a valid refresh JWT can
be reused until it expires; each successful refresh issues a new access/refresh
pair without storing either token in the database.
**Response envelope:** All responses wrap in `ApiResponse<T>`

```json
// Success
{ "success": true, "data": { ... }, "message": "OK", "timestamp": "..." }

// Error
{ "success": false, "errorCode": "NOT_FOUND", "message": "...", "timestamp": "..." }
```

**Authorization:** Role assignments are reloaded from the database for every
request. Spring Security exposes only `ROLE_<ROLE_NAME>` authorities. User,
role and user-role management endpoints are ADMIN-only. Authorization rules for
each business module are owned and declared by that module's maintainers.

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
{
  "userId": "uuid",
  "username": "string",
  "accessToken": "string",
  "refreshToken": "string"
}
```

The response intentionally exposes only the authenticated identity and token
pair. Invalid credentials always return the same `401` response; login attempts
are not throttled by this service.

---

### POST `/auth/refresh`
> Public — no token required

**Request body:**
```json
{ "refreshToken": "string" }
```
**Response `200`:**
```json
{
  "userId": "uuid",
  "username": "string",
  "accessToken": "string",
  "refreshToken": "string"
}
```

---

## 2. Users

### GET `/users`
> **Roles:** `ADMIN`

**Query params:** `page` (default 0) · `size` (default 20)

**Response `200`:**
```json
{
  "items": [{ "id": "uuid", "username": "string", "fullName": "string", "active": true, "createdAt": "instant" }],
  "pageNumber": 0, "pageSize": 20, "totalElements": 10, "totalPages": 1
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

### GET `/users/{id}/roles`
> **Roles:** `ADMIN`

**Response `200`:** `List<RoleDto>`

---

### PUT `/users/{id}/roles`
> **Roles:** `ADMIN`

Replaces the user's complete role set atomically. Repeated valid IDs are
deduplicated; any missing ID rejects the complete mutation.

**Request body:**
```json
{ "roleIds": ["uuid"] }
```
**Response `200`:** `List<RoleDto>`

---

## 3. Roles

### GET `/roles`
> **Roles:** `ADMIN`

**Response `200`:**
```json
[{ "id": "uuid", "name": "string", "description": "string" }]
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

## 4. Products & Materials

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

**Query params:** `page` (default 0) · `size` (default 20) · `finishedProductId` (optional UUID) · `bomStatusId` (optional UUID)

**Response `200`:** `PageResponse<BomDto>`
```json
{ "id": "uuid", "finishedProductId": "uuid", "finishedProductCode": "string", "finishedProductName": "string", "version": 1, "bomStatusId": "uuid", "bomStatusName": "string", "createdBy": "uuid", "createdAt": "instant", "items": [] }
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
{ "finishedProductId": "uuid", "version": "integer (optional — auto-calculated maxVersion + 1 if omitted/duplicate)", "bomStatusId": "uuid (optional — defaults to DRAFT)" }
```
**Response `201`:** `BomDto`

---

### POST `/boms/{id}/activate`
> **Roles:** `ADMIN` · `PLANNER`
> **SRS:** `FR-BOM-002` — Activate BOM (Deactivates current ACTIVE BOM for product; sets target to `ACTIVE`)

**Response `200`:** `BomDto`

---

### POST `/boms/{id}/new-version`
> **Roles:** `ADMIN` · `PLANNER`  
> **SRS:** `FR-BOM-003` — Create New BOM Version (Clones BOM header with auto-incremented version and deep-copies component items in `DRAFT` status)

**Response `201`:** `BomDto`

---

### POST `/boms/{bomId}/items`
> **Roles:** `ADMIN` · `PLANNER`  
> Must be in `DRAFT` status

**Request body:**
```json
{ "materialProductId": "uuid", "quantityPerUnit": 1.5, "unit": "string", "scrapRate": 0.02 }
```
**Response `201`:** `BomItemDto`
```json
{ "id": "uuid", "bomId": "uuid", "materialProductId": "uuid", "materialProductCode": "string", "materialProductName": "string", "quantityPerUnit": 1.5, "unitId": "uuid", "unit": "string", "scrapRate": 0.02 }
```

---

### PUT `/boms/{bomId}/items/{itemId}`
> **Roles:** `ADMIN` · `PLANNER`
> Must be in `DRAFT` status

**Request body:**
```json
{ "quantityPerUnit": 2.0, "unit": "string", "scrapRate": 0.05 }
```
**Response `200`:** `BomItemDto`

---

### DELETE `/boms/{bomId}/items/{itemId}`
> **Roles:** `ADMIN` · `PLANNER`
> Must be in `DRAFT` status

**Response `200`:** no data

---

### GET `/boms/statuses`
> **Roles:** All authenticated

**Response `200`:** `[{ "id": "uuid", "name": "string", "description": "string" }]`

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
  "code": "string",
  "plannedQuantity": 100.0,
  "plannedStartDate": "instant",
  "plannedEndDate": "instant",
  "priorityId": "uuid",
  "workOrderStatusId": "uuid"
}
```
**Constraints:**
* `plannedQuantity` must be > 0.
* `plannedStartDate` must be before `plannedEndDate`.
* If `workOrderStatusId` is provided, it only allows simple planning transitions `DRAFT ⇄ PLANNED`. Other states (like `IN_PROGRESS`, `CANCELLED`, etc.) passed via PUT will be rejected (HTTP 400).
* Requires the client to use dedicated Action Endpoints (`/reserve-materials`, `/start`, `/pause`, `/resume`, `/complete`, `/cancel`) for operational status transitions.
**Response `201`:** `BomDto`

---

### POST `/boms/{id}/activate`
> **Roles:** `ADMIN` · `PLANNER`
> **SRS:** `FR-BOM-002` — Activate BOM (Deactivates current ACTIVE BOM for product; sets target to `ACTIVE`)

**Response `200`:** `BomDto`

---

### POST `/boms/{id}/new-version`
> **Roles:** `ADMIN` · `PLANNER`
> **SRS:** `FR-BOM-003` — Create New BOM Version (Clones BOM header with auto-incremented version and deep-copies component items in `DRAFT` status)

**Response `201`:** `BomDto`

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

**Response `200`:** `[{ "id": "uuid", "name": "string", "description": "string" }]`

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
  "code": "string", 
  "plannedQuantity": 100.0,
  "plannedStartDate": "instant", 
  "plannedEndDate": "instant",
  "priorityId": "uuid", 
  "workOrderStatusId": "uuid"
}
```
**Constraints:**
* `plannedQuantity` must be > 0.
* `plannedStartDate` must be before `plannedEndDate`.
* If `workOrderStatusId` is provided, it only allows simple planning transitions `DRAFT ⇄ PLANNED`. Other states (like `IN_PROGRESS`, `CANCELLED`, etc.) passed via PUT will be rejected (HTTP 400).
* Requires the client to use dedicated Action Endpoints (`/reserve-materials`, `/start`, `/pause`, `/resume`, `/complete`, `/cancel`) for operational status transitions.

**Response `200`:** `WorkOrderDto`

---

### POST `/work-orders/{id}/reserve-materials`
> **Roles:** `PLANNER`

**Request body:**
```json
{
  "machineId": "uuid"
}
```

**Business rules:**
* The system resolves the source warehouse configured with code `RAW_MATERIAL_WAREHOUSE`. The client must not provide `sourceWarehouseId`.
* Only `AVAILABLE` stock balances within the resolved source warehouse may be considered for reservation.
* When multiple lots contain the same material, lots must be selected in FIFO order by `stock_lots.created_at`.
* The Work Order must be in `PLANNED` or `MATERIAL_SHORTAGE` status.
* The specified machine must exist and have status `AVAILABLE`.
* The Work Order moves to `READY_TO_PRODUCE` only when all required materials are available and the specified machine is available.
* Reservation uses pessimistic locking (`SELECT FOR UPDATE`) for the relevant stock balances.
* The operation must run in one transaction. Partial reservation is not allowed and stock quantity must never become negative.
* On success, `AVAILABLE` quantities are moved to `RESERVED`, `work_order_materials.reserved_quantity` is updated, and `RESERVE` stock movements are created for the selected lots.
* Every important status transition must create an audit log with action `RESERVE_MATERIAL`.
* If any material is insufficient, no stock balance or reservation quantity is changed. The Work Order moves to `MATERIAL_SHORTAGE` and the API returns `INSUFFICIENT_STOCK` with the missing materials and quantities.

**Response `200`:**
```json
{
  "success": true,
  "message": "Materials reserved successfully. Work Order is now READY_TO_PRODUCE.",
  "data": {
    "workOrderId": "uuid-lệnh-sản-xuất",
    "status": "READY_TO_PRODUCE"
  }
}
```

---

### POST `/work-orders/{id}/release-materials`
> **Roles:** `ADMIN` · `PLANNER`

Releases outstanding reservations. A `READY_TO_PRODUCE` Work Order returns to `PLANNED`; an already `PLANNED` Work Order remains unchanged. The operation records `RELEASE_RESERVATION` movements and audit data when applicable.

**Response `200`:** `ApiResponse<WorkOrderDto>`

---

### POST `/work-orders/{id}/cancel`
> **Roles:** `ADMIN` · `PLANNER`

Releases outstanding reservations and moves the Work Order to `CANCELLED` only when the configured lifecycle transition is active.

**Response `200`:** `ApiResponse<WorkOrderDto>`

---

### POST `/work-orders/{id}/start`
> **Roles:** `ADMIN` · `PLANNER` · `OPERATOR`

**Request body:**
```json
{ "machineId": "uuid", "productionLineId": "uuid", "operatorId": "uuid" }
```
The Work Order must be `READY_TO_PRODUCE`. The machine row is locked before availability and active-run checks so concurrent starts cannot double-book it.

**Response `200`:** `ApiResponse<WorkOrderDto>`

---

### POST `/work-orders/{id}/pause`
> **Roles:** `ADMIN` · `PLANNER` · `OPERATOR`

Moves an `IN_PROGRESS` Work Order with an active production run to `PAUSED` and records a `PAUSE` event.

**Response `200`:** `ApiResponse<WorkOrderDto>`

---

### POST `/work-orders/{id}/resume`
> **Roles:** `ADMIN` · `PLANNER` · `OPERATOR`

Moves a `PAUSED` Work Order with an active production run to `IN_PROGRESS` and records a `RESUME` event.

**Response `200`:** `ApiResponse<WorkOrderDto>`

---

### POST `/work-orders/{id}/complete`
> **Roles:** `OPERATOR`

Completes an in-progress Work Order with a running production run. Classified good and defective output is placed in quality inspection, while raw-material reservations are consumed or released atomically.

**Request body:**
```json
{
  "actualQuantity": 100.0000,
  "goodQuantity": 90.0000,
  "defectQuantity": 5.0000,
  "scrapQuantity": 5.0000,
  "outputWarehouseId": "uuid",
  "outputLocationId": "uuid",
  "note": "Shift B completion"
}
```
`goodQuantity + defectQuantity + scrapQuantity` must equal `actualQuantity`; all quantities are non-negative and the location must belong to the warehouse.

**Response `200`:** `ApiResponse<WorkOrderDto>` with message `Production completed successfully`.

---

> **Implementation status:** The remaining Work Order routes below are planned controller stubs and currently throw `UnsupportedOperationException("Not implemented")`.

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
  "id": "uuid", "workOrderId": "uuid", "productionRunId": "uuid",
  "eventTypeId": "uuid", "operatorId": "uuid",
  "eventTimestamp": "instant", "note": "string"
}
```

---

### POST `/work-orders/{workOrderId}/events`
> **Roles:** `OPERATOR`

**Request body:**
```json
{
  "eventTypeId": "uuid", "productionRunId": "uuid", "operatorId": "uuid",
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

> Full API spec with request/response details: [`docs/api-spec/QC/api.md`](docs/api-spec/QC/api.md)

### 13.1 Quality Inspections

### GET `/quality-inspections`
> **Roles:** `ADMIN` · `QC_INSPECTOR` · `FACTORY_MANAGER`

**Query params:** `page` · `size` · `statusId` · `workOrderId` · `productId`

**Response `200`:** `PageResponse<QualityInspectionDto>`
```json
{
  "id": "uuid", "workOrderId": "uuid", "productId": "uuid", "productCode": "string",
  "lotId": "uuid", "lotNumber": "string", "quantity": 100.0,
  "qcStatusId": "uuid", "qcStatusName": "PENDING_INSPECTION", "createdAt": "instant"
}
```

---

### GET `/quality-inspections/{id}`
> **Roles:** `ADMIN` · `QC_INSPECTOR` · `FACTORY_MANAGER`

**Response `200`:** `QualityInspectionDto` (includes `results`)

---

### POST `/quality-inspections`
> **Roles:** `ADMIN` · `QC_INSPECTOR`
> **Note:** Normally auto-created after Complete Production.

**Request body:**
```json
{ "workOrderId": "uuid", "productId": "uuid", "lotId": "uuid", "quantity": 100.0, "qcStatusId": "uuid" }
```
**Response `201`:** `QualityInspectionDto`

---

### POST `/quality-inspections/{inspectionId}/pass`
> **Roles:** `ADMIN` · `QC_INSPECTOR`
> **QC status:** `PENDING_INSPECTION → PASSED`  
> **Stock:** `QUALITY_INSPECTION → AVAILABLE`, tạo movement `QC_RELEASE`

**Request body:**
```json
{ "passedQuantity": 95.0, "note": "All dimensions within tolerance" }
```
**Response `200`:**
```json
{ "resultId": "uuid", "qcStatusName": "PASSED", "stockMovementId": "uuid" }
```

---

### POST `/quality-inspections/{inspectionId}/fail`
> **Roles:** `ADMIN` · `QC_INSPECTOR`
> Chọn 1 trong 3 action:

| Action | QC status | Stock | Movement |
|--------|-----------|-------|----------|
| `SCRAP` | `FAILED` | `QUALITY_INSPECTION → SCRAPPED` | `SCRAP` |
| `HOLD` | `ON_HOLD` | `QUALITY_INSPECTION → ON_HOLD` | `QC_HOLD` |
| `REWORK` | `REWORK_REQUIRED` | giữ `QUALITY_INSPECTION` | *(không tạo)* |

**Request body:**
```json
{
  "failedQuantity": 5.0,
  "actionId": "uuid",
  "defectTypeId": "uuid",
  "reason": "Surface scratch exceeds tolerance",
  "note": "string"
}
```
**Response `200`:**
```json
{ "resultId": "uuid", "qcStatusName": "FAILED | ON_HOLD | REWORK_REQUIRED", "stockMovementId": "uuid" }
```

---

### 13.2 Lookup Tables — Read & Create

### GET `/quality-inspections/statuses`
> **Roles:** All authenticated

**Response `200`:**
```json
[{ "id": "uuid", "name": "PENDING_INSPECTION", "description": "Inspection has not been performed yet" }]
```

---

### POST `/quality-inspections/statuses`
> **Roles:** `ADMIN`

**Request body:** `{ "name": "string", "description": "string" }`
**Response `201`:** `QcStatusDto`

---

### GET `/quality-inspections/actions`
> **Roles:** All authenticated

**Response `200`:**
```json
[{ "id": "uuid", "name": "HOLD", "description": "Hold the lot for further investigation" }]
```

---

### POST `/quality-inspections/actions`
> **Roles:** `ADMIN`

**Request body:** `{ "name": "string", "description": "string" }`
**Response `201`:** `QcActionDto`

---

### GET `/quality-inspections/defect-types`
> **Roles:** All authenticated

**Response `200`:**
```json
[{ "id": "uuid", "code": "SCRATCH", "name": "Scratch", "description": "Surface scratch or abrasion" }]
```

---

### POST `/quality-inspections/defect-types`
> **Roles:** `ADMIN`

**Request body:** `{ "code": "string", "name": "string", "description": "string" }`
**Response `201`:** `DefectTypeDto`

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

## 13. Reports

### GET `/reports/inventory-summary`
> **Roles:** `FACTORY_MANAGER`, `ADMIN`, `AUDITOR`

**Query params:** `warehouseId` (UUID) · `productType` (string) · `productCode` (string) · `page` (default 0) · `size` (default 20)

**Response `200`:**
```json
{
  "items": [
    {
      "productId": "uuid",
      "productCode": "PROD-001",
      "productName": "Widget A",
      "productType": "FINISHED_GOOD",
      "warehouseId": "uuid",
      "warehouseName": "Main Warehouse",
      "availableQuantity": 150.0,
      "reservedQuantity": 20.0,
      "qualityInspectionQuantity": 10.0,
      "onHoldQuantity": 5.0,
      "scrappedQuantity": 0.0,
      "totalOnHand": 185.0
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "pageNumber": 0,
  "pageSize": 20
}
```

---

### GET `/reports/material-shortage`
> **Roles:** `FACTORY_MANAGER`, `ADMIN`, `AUDITOR`

**Response `200`:**
```json
[
  {
    "workOrderId": "uuid",
    "workOrderCode": "WO-2026-001",
    "materialProductId": "uuid",
    "materialCode": "RAW-001",
    "materialName": "Steel Sheet",
    "requiredQuantity": 500.0,
    "reservedQuantity": 200.0,
    "availableQuantity": 150.0,
    "shortageQuantity": 150.0
  }
]
```

---

### GET `/reports/production-output`
> **Roles:** `FACTORY_MANAGER`, `ADMIN`, `AUDITOR`

**Query params:** `fromDate` (YYYY-MM-DD) · `toDate` (YYYY-MM-DD) · `page` (default 0) · `size` (default 20)

**Response `200`:**
```json
{
  "items": [
    {
      "date": "2026-08-01",
      "workOrderId": "uuid",
      "workOrderCode": "WO-2026-001",
      "productCodeId": "uuid",
      "productCode": "PROD-001",
      "productName": "Widget A",
      "plannedQuantity": 100.0,
      "actualQuantity": 95.0,
      "goodQuantity": 90.0,
      "defectQuantity": 5.0,
      "scrapQuantity": 0.0,
      "completionRate": 95.00
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "pageNumber": 0,
  "pageSize": 20
}
```

---

### GET `/reports/defect-rate`
> **Roles:** `FACTORY_MANAGER`, `ADMIN`, `AUDITOR`

**Query params:** `fromDate` (YYYY-MM-DD) · `toDate` (YYYY-MM-DD)

**Response `200`:**
```json
[
  {
    "productId": "uuid",
    "productCode": "PROD-001",
    "productName": "Widget A",
    "totalInspected": 100.0,
    "defectQuantity": 5.0,
    "scrapQuantity": 1.0,
    "defectRate": 5.00,
    "topDefectTypes": ["SCRATCH", "DIMENSION_ERROR"]
  }
]
```

---

### GET `/reports/machine-downtime`
> **Roles:** `FACTORY_MANAGER`, `ADMIN`, `AUDITOR`

**Query params:** `fromDate` (YYYY-MM-DD) · `toDate` (YYYY-MM-DD)

**Response `200`:**
```json
[
  {
    "machineId": "uuid",
    "machineCode": "MCH-001",
    "machineName": "CNC Machine 1",
    "totalDowntimeMinutes": 120,
    "maintenanceTicketCount": 2,
    "lastDowntimeReason": "Motor overheating"
  }
]
```

---

### GET `/reports/stock-movement-history`
> **Roles:** `FACTORY_MANAGER`, `ADMIN`, `AUDITOR`

**Query params:** `productId` (UUID) · `warehouseId` (UUID) · `movementTypeId` (UUID) · `fromDate` (YYYY-MM-DD) · `toDate` (YYYY-MM-DD) · `page` (default 0) · `size` (default 20)

**Response `200`:**
```json
{
  "items": [
    {
      "movementId": "uuid",
      "movementTime": "2026-08-01T10:00:00Z",
      "movementType": "PURCHASE_IN",
      "productId": "uuid",
      "productCode": "RAW-001",
      "productName": "Steel Sheet",
      "lotNumber": "LOT-2026-001",
      "fromWarehouse": null,
      "fromLocation": null,
      "toWarehouse": "Main Warehouse",
      "toLocation": "A-01",
      "quantity": 500.0,
      "referenceType": "WORK_ORDER",
      "referenceId": "uuid",
      "createdBy": "Admin User",
      "reason": "Stock receipt"
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "pageNumber": 0,
  "pageSize": 20
}
```

---

## Role × Endpoint Matrix

| Module | `ADMIN` | `WH_MGR` | `PLANNER` | `OPERATOR` | `QC` | `MAINT` | `MGR` | `AUDITOR` |
|--------|:-------:|:---------:|:---------:|:----------:|:----:|:-------:|:-----:|:---------:|
| Users | CRUD | — | — | — | — | — | R | — |
| Roles | CRUD | — | — | — | — | — | — | — |
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
| Work Order Material Reservation | — | — | RESERVE | — | — | — | — | — |
| WO Events | R | — | R | CREATE | — | — | R | — |
| Quality Inspections | CRUD | — | — | — | CRUD | — | R | R |
| Maintenance Tickets | CRUD | — | — | — | — | CRUD | R | — |
