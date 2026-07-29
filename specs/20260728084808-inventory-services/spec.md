# Feature Specification: Inventory Application Services

**Feature Branch**: `20260728084808-inventory-services`

**Created**: 2026-07-28

**Status**: Draft

**Input**: User description: "help me program services (don't program the controller) that related to inventory aspect"

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Record Stock Movement & Auto-update Stock Balances (Priority: P1)

As an inventory manager, I want the backend application service to record stock movements (RECEIPT/IN, ISSUE/OUT, TRANSFER, ADJUSTMENT) and automatically maintain accurate stock balances per product, lot, warehouse, and location without involving presentation controllers.

**Why this priority**: Core inventory accounting and stock balance accuracy are critical to manufacturing operations and prevent stockouts or over-allocation.

**Independent Test**: Can be tested independently via application service unit/integration tests (`InventoryServiceTest`) by calling `recordMovement` with various movement types and verifying movement audit logs and balance updates.

**Acceptance Scenarios**:

1. **Given** a valid product, warehouse, location, and lot with 100 units available, **When** `recordMovement` is invoked with type `ISSUE` for 20 units, **Then** a `StockMovement` record is created and the `StockBalance` quantity is updated to 80 units.
2. **Given** a valid lot and target warehouse location, **When** `recordMovement` is invoked with type `TRANSFER` for 50 units, **Then** stock balances in the source location decrease by 50 and target location increase by 50 under transaction control.
3. **Given** stock balance is 10 units, **When** `recordMovement` is invoked with type `ISSUE` for 15 units, **Then** the service throws `AppException` (BUSINESS_ERROR / INSUFFICIENT_STOCK) and no stock movement or balance mutation is saved.

---

### User Story 2 - Manage Stock Lot Lifecycle & Status (Priority: P2)

As a quality/inventory supervisor, I want to create new stock lots and retrieve stock lot details so that inventory items can be tracked by lot number, expiration date, and quality status (AVAILABLE, QUARANTINE, EXPIRED, HOLD).

**Why this priority**: Essential for traceability in manufacturing and quality compliance, enabling lot tracking from raw material receipt to finished goods.

**Independent Test**: Can be tested via service layer tests by creating stock lots with `createStockLot` and verifying status, lot codes, manufacturing/expiration dates, and uniqueness constraints.

**Acceptance Scenarios**:

1. **Given** valid lot creation details (product ID, lot number, total quantity, manufacturing/expiration dates), **When** `createStockLot` is executed, **Then** a new `StockLot` is persisted with initial status `AVAILABLE` and returned as a `StockLotDto`.
2. **Given** an existing stock lot ID, **When** `getStockLotById` is called, **Then** the service returns the full details of the stock lot including current status and remaining quantity.

---

### User Story 3 - Query Real-Time Stock Balances & Movement History (Priority: P3)

As a warehouse clerk or planning system, I want to query paginated stock movement history and current stock balances by warehouse or product so that inventory visibility is maintained.

**Why this priority**: Provides auditability and visibility for warehouse operations and production planning.

**Independent Test**: Can be tested by seeding multiple stock movement records and querying `getMovements` and `getStockBalances` to verify correct filtering, pagination, and sorting.

**Acceptance Scenarios**:

1. **Given** existing stock movements in the database, **When** `getMovements` is called with page number and size, **Then** a `PageResponse<StockMovementDto>` is returned containing page metadata and content.
2. **Given** stock balances across warehouses, **When** `getStockBalances` is queried with warehouse ID and product ID, **Then** a list of matching `StockBalanceDto` objects is returned with precise on-hand and reserved quantities.

---

### Edge Cases

- **Insufficient Quantity**: Attempting an `ISSUE` or `TRANSFER` movement when requested quantity exceeds available stock balance must raise a domain/application error and roll back transaction.
- **Concurrent Movements**: Simultaneous stock movement requests targeting the same product lot and location must execute under concurrency control (pessimistic lock / database lock) to maintain balance integrity.
- **Inconsistent Location Transfer**: Transferring stock between invalid or identical source and destination locations must be blocked with an explicit error.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: Application service MUST implement `InventoryUseCase` interface without placing business rules in the presentation controller layer.
- **FR-002**: `InventoryServiceImpl` MUST record all stock movements (`IN`, `OUT`, `TRANSFER`, `ADJUSTMENT`) within a transactional boundary.
- **FR-003**: System MUST update or create corresponding `StockBalance` records whenever a stock movement is successfully recorded.
- **FR-004**: System MUST validate that stock movement quantities are positive numbers and that stock balance never falls below zero.
- **FR-005**: System MUST support paginated retrieval of stock lot records (`getStockLots`) and stock movement history (`getMovements`).
- **FR-006**: System MUST persist and return stock lot lifecycle information including lot number, manufacture date, expiration date, supplier lot code, and lot status.
- **FR-007**: Service implementation MUST throw `AppException` with appropriate HTTP status and error codes when business validations fail (e.g. `INSUFFICIENT_STOCK`, `LOT_NOT_FOUND`).

### Key Entities *(include if feature involves data)*

- **StockLot**: Represents a specific batch/lot of materials or products. Key attributes: `id`, `productId`, `lotNumber`, `quantity`, `status`, `manufactureDate`, `expirationDate`, `supplierLotCode`.
- **StockMovement**: Represents an inventory transaction log. Key attributes: `id`, `movementType` (RECEIPT, ISSUE, TRANSFER, ADJUSTMENT), `productId`, `lotId`, `fromWarehouseId`, `fromLocationId`, `toWarehouseId`, `toLocationId`, `quantity`, `referenceNo`, `performedBy`, `createdAt`.
- **StockBalance**: Tracks aggregated on-hand inventory position per location. Key attributes: `id`, `warehouseId`, `locationId`, `productId`, `lotId`, `onHandQuantity`, `reservedQuantity`, `updatedAt`.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 100% of stock movement execution paths maintain strict quantity integrity (on-hand balance equals total IN movements minus total OUT movements).
- **SC-002**: Zero stock movements permit balance reduction resulting in negative stock.
- **SC-003**: Service unit tests achieve >90% code coverage across `InventoryServiceImpl`, domain mappers, and custom repository methods.
- **SC-004**: Concurrency integration tests verify zero race conditions or double-deductions during concurrent stock movement execution.

## Assumptions

- Presentation layer (HTTP `@RestController`) implementation is explicitly excluded from this task scope per user instruction.
- The underlying database schema and jOOQ code generation (`fpt.qn.mes.jooq.tables.*`) are configured in the project.
- Domain entities, repositories, application ports, services, DTOs, mappers, and exceptions follow the project Clean Architecture guidelines.
