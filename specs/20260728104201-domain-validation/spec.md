# Feature Specification: Domain Entity Validation & Value Objects

**Feature Branch**: `002-domain-validation`

**Created**: 2026-07-28

**Status**: Draft

**Input**: User description: "requiresExpiryDate or other function in type don't need to have it"

## Clarifications

### Session 2026-07-28
- Q: How should StockStatus, LotType, and MovementType be modeled inside domain/entities/? → A: Model as simple Value Objects holding `id`, `name`, and `description` without custom behavior methods.
- Q: Should StockStatus, LotType, or MovementType contain helper methods like requiresExpiryDate() or isAvailableForIssue()? → A: NO. Value Objects hold simple attributes (id, name, description) only.
- Q: Should application services check stock_status, lot_type, or movement_type? → A: NO. Application services do not perform status/type checks.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Model StockStatus Value Object & Enforce Balance Invariants (Priority: P1)

As a domain model developer, I want `StockStatus` to be represented as a pure Java Value Object in `domain/entities/` holding `id`, `name`, and `description` so that `StockBalance` can hold status while self-validating non-negative quantities and valid deductions.

**Why this priority**: Core inventory accounting requires that balance quantities can never be negative and deductions cannot exceed available balance.

**Independent Test**: Can be tested via `StockBalanceTest` unit tests by attempting `deductQuantity(amount)` with amount > on-hand balance or negative quantity and verifying that an exception is raised without mutating state.

**Acceptance Scenarios**:

1. **Given** a `StockBalance` with sufficient quantity, **When** `deductQuantity(amount)` is executed with valid amount, **Then** on-hand quantity decreases by amount.
2. **Given** a deduction amount exceeding available balance, **When** `deductQuantity(amount)` is executed, **Then** an `IllegalArgumentException` is thrown.
3. **Given** a zero or negative deduction amount, **When** `deductQuantity(amount)` is executed, **Then** an `IllegalArgumentException` is thrown.

---

### User Story 2 - Model LotType Value Object & Enforce Lot Creation Rules (Priority: P2)

As a domain model developer, I want `LotType` to be represented as a pure Java Value Object in `domain/entities/` holding `id`, `name`, and `description` so that `StockLot` can validate mandatory attributes (`lotNumber`, `productId`) upon creation.

**Why this priority**: Lot traceability requires valid lot numbers and product references upon creation.

**Independent Test**: Can be tested via `StockLotTest` unit tests by attempting to create lots with null/blank lot numbers or null product IDs.

**Acceptance Scenarios**:

1. **Given** valid `lotNumber` and `productId`, **When** `StockLot.create(...)` is called, **Then** a valid lot entity is returned.
2. **Given** a blank or null lot number, **When** `StockLot.create(...)` is executed, **Then** lot creation fails immediately.

---

### User Story 3 - Model MovementType Value Object & Enforce Movement Invariants (Priority: P3)

As a domain model developer, I want `MovementType` to be represented as a pure Java Value Object in `domain/entities/` holding `id`, `name`, and `description` so that `StockMovement` can enforce positive quantities and required audit metadata.

**Why this priority**: Movement logs must be immutable and positive in quantity.

**Independent Test**: Can be tested via `StockMovementTest` unit tests by verifying valid movement creation and rejection of zero/negative quantities.

**Acceptance Scenarios**:

1. **Given** a valid `MovementType` and positive quantity, **When** `StockMovement.create(...)` is executed, **Then** a valid movement entity is returned.
2. **Given** a zero or negative quantity, **When** `StockMovement.create(...)` is executed, **Then** creation fails with a positive quantity requirement error.

---

### Edge Cases

- **Simple Data Holders**: `StockStatus`, `LotType`, and `MovementType` Value Objects hold `id`, `name`, and `description` fields only, with no custom behavior methods.
- **Zero Balance Operations**: Setting or deducting to exactly `0.00` balance is valid; negative balance (< `0.00`) is rejected.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: System MUST represent `StockStatus`, `LotType`, and `MovementType` as simple Java Value Objects inside `fpt.qn.mes.inventory.domain.entities` holding `id`, `name`, and `description` without framework dependencies or custom behavior methods.
- **FR-002**: `StockBalance` MUST enforce `quantity >= 0` and validate that `deductQuantity(amount)` amount is positive and <= on-hand quantity.
- **FR-003**: `StockLot` MUST enforce non-blank `lotNumber` and non-null `productId`.
- **FR-004**: `StockMovement` MUST enforce positive `quantity (> 0)`, non-null `productId`, non-null `warehouseId`, and non-null `createdBy`.
- **FR-005**: Domain validation failures MUST throw standard Java exceptions (`IllegalArgumentException`, `IllegalStateException`) with clear diagnostic messages.

### Key Entities *(include if feature involves data)*

- **StockStatus** (Value Object): Attributes: `id`, `name`, `description`.
- **LotType** (Value Object): Attributes: `id`, `name`, `description`.
- **MovementType** (Value Object): Attributes: `id`, `name`, `description`.
- **StockBalance**: Aggregate root tracking stock position with `StockStatus` Value Object.
- **StockLot**: Entity representing a product batch with `LotType` Value Object.
- **StockMovement**: Immutable audit record with `MovementType` Value Object.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 100% of domain entity factory and mutation methods validate entity invariants (`quantity >= 0`, `lotNumber` presence) at instantiation time in pure Java.
- **SC-002**: `StockStatus`, `LotType`, and `MovementType` remain clean Value Objects containing `id`, `name`, and `description` without helper methods.
- **SC-003**: Unit test coverage reaches 100% across all Value Objects and Domain Entities.
- **SC-004**: Zero framework or database imports leak into `domain/entities/`.

## Assumptions

- Database lookup tables (`stock_statuses`, `lot_types`, `movement_types`) remain dynamic in PostgreSQL (`V20260726194837__init.sql`) and are mapped to domain Value Objects by `infrastructure/persistence` adapters.
- Domain layer validation rules remain pure Java without external annotations.
