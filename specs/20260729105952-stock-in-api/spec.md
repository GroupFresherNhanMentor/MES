# Feature Specification: Stock-In API (Goods Receipt)

**Feature Branch**: `004-stock-in-api`

**Created**: 2026-07-29

**Status**: Draft

**Input**: User description: "POST /api/stock-in help me complete this api with its requirement"

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Record Stock Receipt into Warehouse (Priority: P1)

As a warehouse operator or inventory manager, I want to record incoming stock items (raw materials or purchased goods) into a specific warehouse location and lot so that the inventory system accurately reflects real-world physical stock balances and maintains a complete movement audit ledger.

**Why this priority**: Core inventory functionality required to register new physical inventory into the system. Without stock-in capability, no stock can enter the warehouse database.

**Independent Test**: Can be tested by posting a valid stock-in request with product ID, warehouse ID, location ID, quantity, and reference number, then verifying that the stock balance for the location is created/increased and a corresponding stock movement ledger entry is recorded.

**Acceptance Scenarios**:

1. **Given** a valid product, active warehouse, and active location, **When** a user submits a `POST /api/stock-in` request with a positive quantity, lot details, and purchase reference number, **Then** the system creates or updates the stock balance record, logs a `PURCHASE_IN` stock movement transaction, and returns a 201 Created response with the recorded movement details.
2. **Given** an existing stock balance for the same warehouse, location, product, lot, and status, **When** a `POST /api/stock-in` request is processed for the same target, **Then** the system atomically increments the existing stock balance quantity and updates the version counter.

---

### User Story 2 - Validation of Stock-In Input Data (Priority: P2)

As a warehouse supervisor, I want invalid or incomplete stock-in requests to be rejected immediately with clear error messages so that corrupted or non-existent master data references (such as invalid product IDs or inactive locations) do not corrupt the inventory ledger.

**Why this priority**: Protects data integrity and prevents invalid stock entries from creating orphaned ledger records.

**Independent Test**: Can be tested by sending requests with negative quantities, non-existent warehouse/location IDs, or missing mandatory fields, and asserting proper validation failure responses (HTTP 400/404).

**Acceptance Scenarios**:

1. **Given** a stock-in request with a non-positive quantity (e.g. `<= 0`), **When** the endpoint receives the request, **Then** the request is rejected with a validation error detailing that quantity must be greater than zero.
2. **Given** a stock-in request targeting a non-existent or inactive warehouse/location/product, **When** processed, **Then** the system returns an appropriate resource not found or invalid business state error response.

---

### Edge Cases

- How does the system handle concurrent stock-in requests targeting the exact same warehouse, location, product, and lot? The system MUST execute balance updates with optimistic or pessimistic database row locking to prevent lost updates or race conditions.
- What happens if a new lot number is provided in the stock-in payload that does not exist yet? The system MUST automatically create the new `StockLot` record associated with the specified product before recording the balance and movement.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: System MUST provide an endpoint `POST /api/stock-in` (or `POST /api/stock-movements/stock-in`) to receive and process physical stock receipts.
- **FR-002**: System MUST validate that `quantity` is greater than zero and that mandatory references (`productId`, `warehouseId`, `locationId`, `referenceNo`) are present.
- **FR-003**: System MUST verify that the specified warehouse, warehouse location, product, and stock status exist and are in an active state.
- **FR-004**: System MUST check if the specified `lotNumber` exists in the system:
  - If it exists, verify that it belongs to the same `productId`. If it belongs to a different product, reject with an HTTP 400 validation error ("Lot number belongs to a different product").
  - If it exists and belongs to the same product, reuse the existing `StockLot`.
  - If it does NOT exist, create a new `StockLot` record associated with `productId`.
- **FR-005**: System MUST increment or create a `StockBalance` entry for the unique combination of `(warehouseId, locationId, productId, lotId, stockStatusId)`.
- **FR-006**: System MUST record an immutable ledger transaction in `stock_movements` with movement type `PURCHASE_IN`, recording `quantity`, `toWarehouseId`, `toLocationId`, `toStatusId`, `referenceNo`, `reason`, and creator ID.
- **FR-007**: System MUST support paginated search for stock lots (`GET /api/stock-lots`) filtered by `productId`, `lotTypeId`, `lotNumber`, and `expiryBefore`, with configurable page size and sort fields.

### Key Entities

- **StockInRequest**: Input payload containing `productId`, `warehouseId`, `locationId`, `lotNumber`, `lotTypeId`, `expiryDate`, `stockStatusId`, `quantity`, `referenceNo`, `reason`.
- **StockBalance**: Inventory position tracking available quantity per warehouse location, product, lot, and status.
- **StockMovement**: Immutable audit ledger recording inventory movements between locations or status changes.
- **StockLot**: Batch or lot tracking record holding lot number, product association, and optional expiration date.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Warehouse operators can complete a stock-in transaction in under 2 seconds API roundtrip.
- **SC-002**: 100% of successful stock-in transactions produce both an updated stock balance and an immutable stock movement ledger entry with zero discrepancy between movement quantity and balance increment.
- **SC-003**: Invalid stock-in requests (bad IDs, zero/negative quantity) are rejected with 100% accuracy without modifying database state.

## Assumptions

- Stock-in defaults `stockStatusId` to `AVAILABLE` unless explicitly provided as `QUALITY_INSPECTION` or `ON_HOLD`.
- The authenticated user ID extracted from security context is recorded as `created_by` in the stock movement ledger.
- Existing inventory schema and jOOQ code generation bindings support `stock_balances` and `stock_movements` relationships.
