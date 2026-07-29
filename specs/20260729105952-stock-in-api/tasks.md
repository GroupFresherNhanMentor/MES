# Tasks: Stock-In API & Stock Lot Querying

**Input**: Design documents from `/specs/20260729105952-stock-in-api/`

**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g. US1, US2)

---

## Phase 1: Setup (Shared Infrastructure)

- [X] T001 Create StockInRequest DTO in `be/src/main/java/fpt/qn/mes/inventory/application/dto/request/StockInRequest.java`
- [X] T002 Create StockLotSearchRequest DTO in `be/src/main/java/fpt/qn/mes/inventory/application/dto/request/StockLotSearchRequest.java`

---

## Phase 2: Foundational (Blocking Prerequisites)

- [X] T003 Ensure MovementTypeConstants and StockStatusConstants are present in `be/src/main/java/fpt/qn/mes/inventory/domain/constants/`
- [X] T004 Add findByLotNumber and count methods to StockLotRepository interface in `be/src/main/java/fpt/qn/mes/inventory/domain/repository/StockLotRepository.java`
- [X] T005 Implement findByLotNumber, count, and buildCondition in `be/src/main/java/fpt/qn/mes/inventory/infrastructure/persistence/StockLotPersistenceAdapter.java`

---

## Phase 3: User Story 1 - Record Stock Receipt into Warehouse (Priority: P1) 🎯 MVP

**Goal**: Implement `POST /api/stock-in` to receive goods, validate/link lot number, update balance, and log `PURCHASE_IN` movement.

**Independent Test**: Post a valid stock-in request and verify stock balance quantity increment and `PURCHASE_IN` movement log.

### Tests for User Story 1

- [X] T006 [P] [US1] Unit test for recordStockIn and createStockLot in `be/src/test/java/fpt/qn/mes/inventory/service/InventoryServiceTest.java`
- [X] T007 [P] [US1] Integration test for POST /api/stock-in in `be/src/test/java/fpt/qn/mes/inventory/integration/InventoryIntegrationTest.java`

### Implementation for User Story 1

- [X] T008 [US1] Add recordStockIn signature to InventoryUseCase in `be/src/main/java/fpt/qn/mes/inventory/application/port/in/InventoryUseCase.java`
- [X] T009 [US1] Implement recordStockIn and lot linking logic in `be/src/main/java/fpt/qn/mes/inventory/application/service/InventoryService.java`
- [X] T010 [US1] Add POST /api/stock-in endpoint in `be/src/main/java/fpt/qn/mes/inventory/presentation/InventoryController.java`
- [X] T011 [US1] Permit /api/stock-in endpoint in `be/src/main/java/fpt/qn/mes/auth/infrastructure/config/SecurityConfig.java`

---

## Phase 4: User Story 2 - Lot Product Validation & Paginated Stock Lot Search (Priority: P2)

**Goal**: Validate lot product ownership and update `GET /api/stock-lots` to support filtering by `productId`.

**Independent Test**: Query `GET /api/stock-lots?productId={id}` and test rejecting stock-in when lot belongs to another product.

### Tests for User Story 2

- [X] T012 [P] [US2] Unit test for lot product ownership validation in `be/src/test/java/fpt/qn/mes/inventory/service/InventoryServiceTest.java`
- [X] T013 [P] [US2] Integration test for GET /api/stock-lots?productId=... in `be/src/test/java/fpt/qn/mes/inventory/integration/InventoryIntegrationTest.java`

### Implementation for User Story 2

- [X] T014 [US2] Update getStockLots signature in `be/src/main/java/fpt/qn/mes/inventory/application/port/in/InventoryUseCase.java`
- [X] T015 [US2] Map StockLotSearchRequest fields (productId, lotTypeId, lotNumber, expiryBefore, page, size, sort) to StockLotSearchCriteria and implement getStockLots + lot ownership validation in `be/src/main/java/fpt/qn/mes/inventory/application/service/InventoryService.java`
- [X] T016 [US2] Update GET /api/stock-lots endpoint in `be/src/main/java/fpt/qn/mes/inventory/presentation/InventoryController.java`

---

## Phase 5: Polish & Cross-Cutting Concerns

- [X] T017 [P] Verify OpenAPI annotations on stock-in and stock-lots endpoints in `be/src/main/java/fpt/qn/mes/inventory/presentation/InventoryController.java`
- [X] T018 Run test suite `./mvnw test` to ensure 100% test suite pass

---

## Dependencies & Execution Order

### Phase Dependencies
- Setup (Phase 1) → Foundational (Phase 2) → User Story 1 (Phase 3) → User Story 2 (Phase 4) → Polish (Phase 5)

### Parallel Opportunities
- Setup: T001, T002 can run in parallel.
- US1 Tests: T006, T007 can run in parallel.
- US2 Tests: T012, T013 can run in parallel.
