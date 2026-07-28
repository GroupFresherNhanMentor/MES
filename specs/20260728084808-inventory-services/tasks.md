# Tasks: Inventory Application Services

**Input**: Design documents from `specs/20260728084808-inventory-services/`

**Prerequisites**: `plan.md`, `spec.md`, `data-model.md`, `contracts/InventoryUseCaseContract.md`, `research.md`

**Tests**: Required. Unit tests with Mockito, database integration tests, and concurrency test cases for balance integrity per MES Constitution.

**Organization**: Tasks are grouped by user story (US1, US2, US3) to enable independent implementation and testing.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: User story label (US1, US2, US3)
- Includes exact file paths in task descriptions

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Module structure verification and exceptions setup

- [X] T001 Verify module package directory layout under `be/src/main/java/fpt/qn/mes/inventory/`
- [X] T002 [P] Implement `InventoryNotFoundException.java` in `be/src/main/java/fpt/qn/mes/inventory/application/exception/InventoryNotFoundException.java`
- [X] T003 [P] Implement `InsufficientStockException.java` in `be/src/main/java/fpt/qn/mes/inventory/application/exception/InsufficientStockException.java`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core domain repositories and persistence infrastructure required by all user stories

- [X] T004 [P] Define `StockLotRepository.java` interface in `be/src/main/java/fpt/qn/mes/inventory/domain/repository/StockLotRepository.java`
- [X] T005 [P] Define `StockMovementRepository.java` interface in `be/src/main/java/fpt/qn/mes/inventory/domain/repository/StockMovementRepository.java`
- [X] T006 [P] Define `StockBalanceRepository.java` interface in `be/src/main/java/fpt/qn/mes/inventory/domain/repository/StockBalanceRepository.java`
- [X] T007 Implement jOOQ `StockLotPersistenceAdapter.java` in `be/src/main/java/fpt/qn/mes/inventory/infrastructure/persistence/StockLotPersistenceAdapter.java`
- [X] T008 Implement jOOQ `StockMovementPersistenceAdapter.java` in `be/src/main/java/fpt/qn/mes/inventory/infrastructure/persistence/StockMovementPersistenceAdapter.java`
- [X] T009 Implement jOOQ `StockBalancePersistenceAdapter.java` with pessimistic locking support (`forUpdate`) in `be/src/main/java/fpt/qn/mes/inventory/infrastructure/persistence/StockBalancePersistenceAdapter.java`
- [X] T010 [P] Create MapStruct `StockLotDtoMapper.java` in `be/src/main/java/fpt/qn/mes/inventory/application/mapper/StockLotDtoMapper.java`
- [X] T011 [P] Create MapStruct `StockMovementDtoMapper.java` in `be/src/main/java/fpt/qn/mes/inventory/application/mapper/StockMovementDtoMapper.java`
- [X] T012 [P] Create MapStruct `StockBalanceDtoMapper.java` in `be/src/main/java/fpt/qn/mes/inventory/application/mapper/StockBalanceDtoMapper.java`

**Checkpoint**: Core domain repositories, jOOQ implementations, and mappers ready.

---

## Phase 3: User Story 1 - Record Stock Movement & Auto-update Stock Balances (Priority: P1) 🎯 MVP

**Goal**: Enable recording stock movements (`RECEIPT`, `ISSUE`, `TRANSFER`, `ADJUSTMENT`) with atomic balance updates, negative balance prevention, and concurrency control.

**Independent Test**: Execute `InventoryServiceImplTest.recordMovement_*` unit tests and `InventoryIntegrationTest.recordMovement_*` integration & concurrency tests.

### Tests for User Story 1

- [X] T013 [P] [US1] Unit test: Add movement recording test cases to `be/src/test/java/fpt/qn/mes/inventory/service/InventoryServiceTest.java` (verify RECEIPT, ISSUE, TRANSFER, and INSUFFICIENT_STOCK exception paths)
- [X] T014 [P] [US1] Integration & Concurrency test: Add database transaction and parallel stock movement test methods (`CountDownLatch` + `ExecutorService`) in `be/src/test/java/fpt/qn/mes/inventory/integration/InventoryIntegrationTest.java`

### Implementation for User Story 1

- [X] T015 [US1] Implement domain entity factory logic in `StockMovement.create(...)` inside `be/src/main/java/fpt/qn/mes/inventory/domain/entities/StockMovement.java`
- [X] T016 [US1] Implement `recordMovement` transactional logic in `be/src/main/java/fpt/qn/mes/inventory/application/service/InventoryService.java` (validating movement type, applying pessimistic locks, Mutating `StockBalance`, and persisting `StockMovement`)

**Checkpoint**: User Story 1 complete and independently testable (MVP functional).

---

## Phase 4: User Story 2 - Manage Stock Lot Lifecycle & Status (Priority: P2)

**Goal**: Implement stock lot creation, status assignment, expiration date tracking, and single lot retrieval.

**Independent Test**: Run `InventoryServiceTest.createStockLot_*` and `getStockLotById_*`.

### Tests for User Story 2

- [X] T017 [P] [US2] Unit test: Add stock lot creation and ID query unit test methods in `be/src/test/java/fpt/qn/mes/inventory/service/InventoryServiceTest.java`
- [X] T018 [P] [US2] Integration test: Add stock lot persistence and unique lot number constraint test methods in `be/src/test/java/fpt/qn/mes/inventory/integration/InventoryIntegrationTest.java`

### Implementation for User Story 2

- [X] T019 [US2] Implement domain entity factory logic in `StockLot.create(...)` inside `be/src/main/java/fpt/qn/mes/inventory/domain/entities/StockLot.java`
- [X] T020 [US2] Implement `createStockLot` and `getStockLotById` methods in `be/src/main/java/fpt/qn/mes/inventory/application/service/InventoryService.java`

**Checkpoint**: User Story 2 complete and independently testable alongside US1.

---

## Phase 5: User Story 3 - Query Real-Time Stock Balances & Movement History (Priority: P3)

**Goal**: Provide paginated retrieval of stock movements and filtered list queries for current stock balances.

**Independent Test**: Execute `InventoryServiceTest.getMovements_*` and `getStockBalances_*`.

### Tests for User Story 3

- [X] T021 [P] [US3] Unit test: Add paginated movement list and stock balance filtering test cases in `be/src/test/java/fpt/qn/mes/inventory/service/InventoryServiceTest.java`
- [X] T022 [P] [US3] Integration test: Add database query verification for paginated movements and warehouse/product stock balances in `be/src/test/java/fpt/qn/mes/inventory/integration/InventoryIntegrationTest.java`

### Implementation for User Story 3

- [X] T023 [US3] Implement `getStockLots` paginated query in `be/src/main/java/fpt/qn/mes/inventory/application/service/InventoryService.java`
- [X] T024 [US3] Implement `getMovements` paginated query in `be/src/main/java/fpt/qn/mes/inventory/application/service/InventoryService.java`
- [X] T025 [US3] Implement `getStockBalances` filter query by warehouse ID and product ID in `be/src/main/java/fpt/qn/mes/inventory/application/service/InventoryService.java`

**Checkpoint**: All user stories functional and covered by unit and integration tests.

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Validation, code coverage, and documentation cleanup

- [X] T026 [P] Verify code coverage across `InventoryService` via `./mvnw test`
- [X] T027 [P] Execute quickstart validation guide scenarios in `specs/20260728084808-inventory-services/quickstart.md`

---

## Dependencies & Execution Order

### Phase Dependencies
- **Phase 1 (Setup)**: No dependencies - starts immediately.
- **Phase 2 (Foundational)**: Depends on Phase 1 - BLOCKS all User Stories.
- **Phase 3 (User Story 1)**: Depends on Phase 2.
- **Phase 4 (User Story 2)**: Depends on Phase 2 (can run parallel to US1 or sequential).
- **Phase 5 (User Story 3)**: Depends on Phase 2 (can run parallel to US1/US2 or sequential).
- **Phase 6 (Polish)**: Depends on all User Stories completion.

### Parallel Opportunities
- Foundational mappers (T010, T011, T012) and repositories (T004, T005, T006) can be created in parallel.
- Test tasks within each User Story (marked `[P]`) can be written in parallel before service implementation.

---

## Implementation Strategy (MVP First)

1. Complete Phase 1 & Phase 2.
2. Implement Phase 3 (User Story 1 - Stock Movement & Balance Auto-update).
3. Validate US1 via `./mvnw test -Dtest=InventoryServiceTest`.
4. Implement Phase 4 (Stock Lot Management) and Phase 5 (Query Endpoints).
5. Run full integration suite (`./mvnw test`).
