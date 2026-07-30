# Data Model: Stock Transfers API

## Entities & Request/Response DTOs

### 1. StockTransferRequest (Application DTO)

| Field | Type | Validation / Constraints | Description |
|-------|------|-------------------------|-------------|
| `fromWarehouseId` | `UUID` | `@NotNull` | Source warehouse ID |
| `fromLocationId` | `UUID` | `@NotNull` | Source warehouse location ID |
| `toWarehouseId` | `UUID` | `@NotNull` | Destination warehouse ID |
| `toLocationId` | `UUID` | `@NotNull` | Destination warehouse location ID |
| `productId` | `UUID` | `@NotNull` | Product ID being transferred |
| `lotId` | `UUID` | `@NotNull` | Product lot ID being transferred |
| `quantity` | `BigDecimal` | `@NotNull`, `@Positive` | Quantity to transfer (> 0) |

### Validation Rules
- `quantity > 0`
- `fromLocationId != toLocationId` (or `fromWarehouseId != toWarehouseId` or `fromLocationId != toLocationId`)
- Source stock balance for `(fromWarehouseId, fromLocationId, productId, lotId, AVAILABLE)` must exist and `quantity >= request.quantity`

---

### 2. StockTransferResponse (Application DTO)

| Field | Type | Description |
|-------|------|-------------|
| `transferOutMovement` | `StockMovementDto` | Generated TRANSFER_OUT movement DTO |
| `transferInMovement` | `StockMovementDto` | Generated TRANSFER_IN movement DTO |
| `sourceBalance` | `StockBalanceDto` | Source location updated available balance DTO |
| `destinationBalance` | `StockBalanceDto` | Destination location updated available balance DTO |

---

### 3. Domain Entities Involved

- **StockBalance**:
  - `id`: `UUID`
  - `warehouseId`: `UUID`
  - `locationId`: `UUID`
  - `productId`: `UUID`
  - `lotId`: `UUID`
  - `statusId`: `UUID` (AVAILABLE)
  - `quantity`: `BigDecimal`

- **StockMovement**:
  - `id`: `UUID`
  - `referenceNumber`: `String` (e.g. `TRF-20260730-XXXX`)
  - `movementTypeId`: `UUID` (TRANSFER_OUT / TRANSFER_IN)
  - `productId`: `UUID`
  - `lotId`: `UUID`
  - `warehouseId`: `UUID`
  - `locationId`: `UUID`
  - `quantity`: `BigDecimal`
  - `fromStatusId`: `UUID` (AVAILABLE)
  - `toStatusId`: `UUID` (AVAILABLE)
  - `createdBy`: `UUID`
  - `createdAt`: `Instant`
