# Research & Architectural Decisions: Domain Entity Validation & Value Objects

**Feature Branch**: `20260728104201-domain-validation`

## Decision 1: Service Layer Immunity from Status and Type Checks

### Decision
Application services (`InventoryService`, `WorkOrderService`, `QualityService`, `MaintenanceService`, etc.) MUST NOT check or validate `StockStatus`, `LotType`, or `MovementType`.

### Rationale
- Outer services delegate domain entity creation and mutation directly to entity methods without performing explicit status or type verification queries.
- Prevents logic duplication between application services and domain entities.

---

## Decision 2: Modeling Statuses & Types as Pure Java Domain Value Objects

### Decision
Model `StockStatus`, `LotType`, and `MovementType` as immutable Value Objects inside `fpt.qn.mes.inventory.domain.entities` holding `id`, `name`, `description`, and domain behavior methods (`isAvailableForIssue`, `requiresExpiryDate`, `isReceipt`, `isIssue`, `isTransfer`).

### Rationale
- Matches the PostgreSQL schema in `V20260726194837__init.sql` where `stock_statuses`, `lot_types`, and `movement_types` have `id` (UUID), `name` (VARCHAR), and `description` (VARCHAR).
- Allows domain entities (`StockBalance`, `StockLot`, `StockMovement`) to validate business rules in pure Java without calling application services or database repositories.

---

## Decision 3: Domain Layer Exception Strategy

### Decision
Throw standard Java exceptions (`IllegalArgumentException`, `IllegalStateException`) directly from domain entity factory and state mutation methods when business invariants are violated.

### Rationale
- Standard Java exceptions belong to `java.lang.*` and introduce zero dependencies on outer layers (`application/` or `infrastructure/`).
