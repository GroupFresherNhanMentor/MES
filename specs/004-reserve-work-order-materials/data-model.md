# Data Model: Reserve Work Order Materials

## Existing Entities

### WorkOrder

| Field | Type | Rules for this feature |
|---|---|---|
| `id` | UUID | Path identifier; must exist. |
| `bomId` | UUID | Existing BOM association used to derive material requirements. |
| `workOrderStatusId` | UUID | Must resolve to `PLANNED` or `MATERIAL_SHORTAGE` before reservation. |
| `plannedQuantity` | BigDecimal | Used with BOM item quantities and scrap rate. |

### WorkOrderMaterial

| Field | Type | Rules for this feature |
|---|---|---|
| `workOrderId` | UUID | Foreign key to the Work Order. |
| `materialProductId` | UUID | Product to reserve. |
| `requiredQuantity` | BigDecimal | Required amount calculated from BOM. |
| `reservedQuantity` | BigDecimal | Increased only after every material passes availability validation. |
| `consumedQuantity` | BigDecimal | Not changed by this operation. |

Requirement calculation:

```text
requiredQuantity = plannedQuantity * quantityPerUnit * (1 + scrapRate)
quantityToReserve = requiredQuantity - reservedQuantity
```

The operation must reject inconsistent negative remaining quantities. A normal retry after `MATERIAL_SHORTAGE` has no partial reservation and therefore reserves the full remaining requirement.

### Warehouse

| Field | Type | Rules for this feature |
|---|---|---|
| `id` | UUID | Identifies an eligible stock source and is never supplied by the client. |
| `warehouseStatusId` | UUID | Only warehouses with status `ACTIVE` are eligible for stock operations. |

The request contains no `sourceWarehouseId`. Stock queries include all `ACTIVE` warehouses.

### StockLot

| Field | Type | Rules for this feature |
|---|---|---|
| `id` | UUID | Identifies the lot used by a reservation movement. |
| `productId` | UUID | Must match the Work Order material product. |
| `createdAt` | Instant | Primary FIFO ordering field. |

FIFO ordering uses `createdAt` followed by deterministic `id`/location tie-breakers.

### StockBalance

| Field | Type | Rules for this feature |
|---|---|---|
| `warehouseId` | UUID | Must belong to an `ACTIVE` warehouse. |
| `locationId` | UUID | Identifies the stock location. |
| `productId` | UUID | Matches a Work Order material. |
| `lotId` | UUID | Joins FIFO lot ordering. |
| `stockStatusId` | UUID | Source rows are `AVAILABLE`; destination rows are `RESERVED`. |
| `quantity` | BigDecimal | Must remain non-negative. |
| `version` | Long | Retained but not used as the primary concurrency mechanism for this feature. |

The unique balance slot is `(warehouseId, locationId, productId, lotId, stockStatusId)`. Both source and destination status rows must be handled without conflating them.

### StockMovement

| Field | Type | Rules for this feature |
|---|---|---|
| `movementTypeId` | UUID | Must resolve to `RESERVE`. |
| `productId` | UUID | Reserved material product. |
| `lotId` | UUID | Selected FIFO lot. |
| `workOrderId` | UUID | Must link the movement to the reserving Work Order. |
| `fromWarehouseId` / `fromLocationId` | UUID | Source balance location. |
| `toWarehouseId` / `toLocationId` | UUID | Same physical location for status-only reservation. |
| `fromStatusId` | UUID | `AVAILABLE`. |
| `toStatusId` | UUID | `RESERVED`. |
| `quantity` | BigDecimal | Strictly positive allocation amount. |
| `createdBy` | UUID | Authenticated Planner actor. |

Stock movements are insert-only and one movement is created for each selected lot allocation.

### AuditLog

| Field | Type | Rules for this feature |
|---|---|---|
| `actorId` | UUID | Authenticated Planner. |
| `action` | String | `RESERVE_MATERIAL`. |
| `entityType` | String | `WORK_ORDER`. |
| `entityId` | UUID | Work Order ID. |
| `oldValue` | String | Previous status. |
| `newValue` | String | `READY_TO_PRODUCE` or `MATERIAL_SHORTAGE`. |
| `createdAt` | Instant | Insert timestamp. |

Audit records are immutable and written in the same transaction as the corresponding status change.

## API DTOs

The reservation endpoint has no request DTO or request body. The Work Order ID is supplied in the path, and the source warehouse is selected from all active warehouse balances.

### ReserveWorkOrderMaterialsResponse

| Field | Type | Rules |
|---|---|---|
| `workOrderId` | UUID | ID from the path. |
| `status` | String | `READY_TO_PRODUCE` on HTTP 200. |

## State Transitions

| Current status | Condition | New status | Result |
|---|---|---|---|
| `PLANNED` | All required material is available | `READY_TO_PRODUCE` | HTTP 200; stock and audit updated. |
| `MATERIAL_SHORTAGE` | All required material is available | `READY_TO_PRODUCE` | HTTP 200; retry succeeds. |
| `PLANNED` | Any material is insufficient | `MATERIAL_SHORTAGE` | HTTP 400 `INSUFFICIENT_STOCK`; no stock/material reservation mutation. |
| `MATERIAL_SHORTAGE` | Any material remains insufficient | `MATERIAL_SHORTAGE` | HTTP 400 `INSUFFICIENT_STOCK`; no stock/material reservation mutation. |
| Any other status | Reservation requested | Unchanged | HTTP 400 `INVALID_INPUT`. |

## Transaction and Locking Invariants

- Lock the Work Order before evaluating or mutating its reservation state.
- Select and lock all eligible `AVAILABLE` balance rows in deterministic FIFO order before mutating any row.
- Validate every material before changing any balance.
- Decrement source `AVAILABLE` rows and increment or insert destination `RESERVED` rows atomically.
- Never allow `quantity < 0` or reservation beyond available quantity.
- On shortage, persist only the shortage status and audit record; no stock balance, Work Order material, or reservation movement may be written.
