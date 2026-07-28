# Tasks: Domain Entity Validation & Value Objects

**Input**: Design documents from `/specs/20260728104201-domain-validation/`

**Prerequisites**: plan.md, spec.md, data-model.md, contracts/InventoryDomainContract.md

**Tests**: Pure domain unit tests (`JUnit 5`) are included for each Value Object and Domain Entity under `be/src/test/java/fpt/qn/mes/inventory/domain/`.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `- [ ] [TaskID] [P?] [Story?] Description with file path`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Domain package structure initialization

- [x] T001 Verify domain entities package structure in `be/src/main/java/fpt/qn/mes/inventory/domain/entities/`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core Value Objects required by domain entities

- [x] T002 [P] Create `StockStatus` Value Object in `be/src/main/java/fpt/qn/mes/inventory/domain/entities/StockStatus.java`
- [x] T003 [P] Create `LotType` Value Object in `be/src/main/java/fpt/qn/mes/inventory/domain/entities/LotType.java`
- [x] T004 [P] Create `MovementType` Value Object in `be/src/main/java/fpt/qn/mes/inventory/domain/entities/MovementType.java`
- [x] T005 [P] Create Value Object unit tests in `be/src/test/java/fpt/qn/mes/inventory/domain/ValueObjectsTest.java`

**Checkpoint**: Foundation ready - user story implementation can now begin

---

## Phase 3: User Story 1 - Model StockStatus & StockBalance Invariants (Priority: P1) 🎯 MVP

**Goal**: Implement `StockStatus` integration and `StockBalance` quantity & deduction invariants (`quantity >= 0`, `deductQuantity`).

**Independent Test**: Run `StockBalanceTest` in `be/src/test/java/fpt/qn/mes/inventory/domain/StockBalanceTest.java`.

### Tests & Implementation for User Story 1

- [x] T006 [P] [US1] Create unit test `StockBalanceTest` in `be/src/test/java/fpt/qn/mes/inventory/domain/StockBalanceTest.java`
- [x] T007 [US1] Refactor `StockBalance` entity in `be/src/main/java/fpt/qn/mes/inventory/domain/entities/StockBalance.java` to hold `StockStatus` and enforce `quantity >= 0`, `deductQuantity(amount)`, and `addQuantity(amount)`
- [x] T008 [US1] Update `InventoryRecordMapper` in `be/src/main/java/fpt/qn/mes/inventory/infrastructure/persistence/InventoryRecordMapper.java` to map `StockStatus` Value Object

**Checkpoint**: User Story 1 is fully functional and testable independently

---

## Phase 4: User Story 2 - Model LotType & StockLot Invariants (Priority: P2)

**Goal**: Implement `LotType` integration and `StockLot` creation rules (`lotNumber` non-blank, `productId` non-null).

**Independent Test**: Run `StockLotTest` in `be/src/test/java/fpt/qn/mes/inventory/domain/StockLotTest.java`.

### Tests & Implementation for User Story 2

- [x] T009 [P] [US2] Create unit test `StockLotTest` in `be/src/test/java/fpt/qn/mes/inventory/domain/StockLotTest.java`
- [x] T010 [US2] Refactor `StockLot` entity in `be/src/main/java/fpt/qn/mes/inventory/domain/entities/StockLot.java` to hold `LotType` and enforce non-blank `lotNumber` and non-null `productId`
- [x] T011 [US2] Update `InventoryRecordMapper` in `be/src/main/java/fpt/qn/mes/inventory/infrastructure/persistence/InventoryRecordMapper.java` to map `LotType` Value Object

**Checkpoint**: User Story 1 AND 2 work independently

---

## Phase 5: User Story 3 - Model MovementType & StockMovement Invariants (Priority: P3)

**Goal**: Implement `MovementType` integration and `StockMovement` creation rules (`quantity > 0`, non-null metadata).

**Independent Test**: Run `StockMovementTest` in `be/src/test/java/fpt/qn/mes/inventory/domain/StockMovementTest.java`.

### Tests & Implementation for User Story 3

- [x] T012 [P] [US3] Create unit test `StockMovementTest` in `be/src/test/java/fpt/qn/mes/inventory/domain/StockMovementTest.java`
- [x] T013 [US3] Refactor `StockMovement` entity in `be/src/main/java/fpt/qn/mes/inventory/domain/entities/StockMovement.java` to hold `MovementType` and enforce positive `quantity (> 0)` and non-null metadata
- [x] T014 [US3] Update `InventoryRecordMapper` in `be/src/main/java/fpt/qn/mes/inventory/infrastructure/persistence/InventoryRecordMapper.java` to map `MovementType` Value Object

**Checkpoint**: All user stories are independently functional

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Final domain validation and test suite execution

- [x] T015 [P] Run all unit tests `./mvnw test` in `be/`
- [x] T016 [P] Verify zero framework coupling in `be/src/main/java/fpt/qn/mes/inventory/domain/entities/`
- [x] T017 Execute `quickstart.md` validation guide scenarios

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: Can start immediately.
- **Foundational (Phase 2)**: Depends on Setup completion — BLOCKS all user stories.
- **User Stories (Phase 3+)**: All depend on Foundational phase completion (P1 → P2 → P3).
- **Polish (Phase 6)**: Depends on all user stories being complete.

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Setup + Foundational (T001–T005).
2. Complete User Story 1 (T006–T008).
3. Validate `StockBalanceTest`.
