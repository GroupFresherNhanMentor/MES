# Tasks: Inventory Reference Lookup Endpoints

**Input**: Design documents from `/specs/20260730095330-inventory-lookup-endpoints/`

**Prerequisites**: plan.md (required), spec.md (required), data-model.md, contracts/, research.md, quickstart.md

**Tests**: Mandatory per Constitution VI. Unit tests and controller slice tests included for each user story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (US1, US2, US3)
- File paths are relative to `be/` backend root

---

## Phase 1: Setup

- [x] T001 Verify project build configuration and existing reference tables (`LOT_TYPES`, `STOCK_STATUSES`, `MOVEMENT_TYPES`)

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: DTOs, Search Criteria, Mappers, Repositories, and Persistence Adapters

- [x] T002 [P] Create DTO `LotTypeSummaryDto.java` in `be/src/main/java/fpt/qn/mes/inventory/application/dto/response/`
- [x] T003 [P] Create search criteria classes (`LotTypeSearchCriteria.java`, `StockStatusSearchCriteria.java`, `MovementTypeSearchCriteria.java`) in `be/src/main/java/fpt/qn/mes/inventory/domain/repository/criteria/`
- [x] T004 [P] Create search request DTOs (`LotTypeSearchRequest.java`, `StockStatusSearchRequest.java`, `MovementTypeSearchRequest.java`) in `be/src/main/java/fpt/qn/mes/inventory/application/dto/request/`
- [x] T005 [P] Update `InventoryDtoMapper.java` with `LotTypeSummaryDto toSummary(LotType entity)` in `be/src/main/java/fpt/qn/mes/inventory/application/mapper/`
- [x] T006 [P] Update `LotTypeRepository.java`, `StockStatusRepository.java`, `MovementTypeRepository.java` to extend `BaseDomainRepository` defining `search` & `count` in `be/src/main/java/fpt/qn/mes/inventory/domain/repository/`
- [x] T007 Implement jOOQ persistence adapters (`LotTypePersistenceAdapter.java`, `StockStatusPersistenceAdapter.java`, `MovementTypePersistenceAdapter.java`) in `be/src/main/java/fpt/qn/mes/inventory/infrastructure/persistence/`

---

## Phase 3: User Story 1 - Lot Types Lookup API (`GET /api/lot-types`) (Priority: P1) 🎯 MVP

**Goal**: Allow clients to fetch all lot types via criteria search returning `LotTypeSummaryDto` instances.

**Independent Test**: Execute `GET /api/lot-types` and verify HTTP 200 OK with list of `LotTypeSummaryDto` items.

### Tests for User Story 1

- [x] T008 [P] [US1] Unit test: `InventoryServiceTest.java` (`getLotTypes` criteria search) in `be/src/test/java/fpt/qn/mes/inventory/service/`
- [x] T009 [P] [US1] Controller slice test: `InventoryControllerTest.java` (`GET /api/lot-types`) in `be/src/test/java/fpt/qn/mes/inventory/presentation/`

### Implementation for User Story 1

- [x] T010 [US1] Update `InventoryUseCase.java` and implement `getLotTypes` in `be/src/main/java/fpt/qn/mes/inventory/application/service/InventoryService.java`
- [x] T011 [US1] Implement `GET /api/lot-types` endpoint in `be/src/main/java/fpt/qn/mes/inventory/presentation/InventoryController.java`

---

## Phase 4: User Story 2 - Stock Statuses Lookup API (`GET /api/stock-statuses`) (Priority: P1)

**Goal**: Allow clients to fetch all stock statuses via criteria search returning `StockStatusSummaryDto` instances.

**Independent Test**: Execute `GET /api/stock-statuses` and verify HTTP 200 OK with list of `StockStatusSummaryDto` items.

### Tests for User Story 2

- [x] T012 [P] [US2] Unit test: `InventoryServiceTest.java` (`getStockStatuses` criteria search) in `be/src/test/java/fpt/qn/mes/inventory/service/`
- [x] T013 [P] [US2] Controller slice test: `InventoryControllerTest.java` (`GET /api/stock-statuses`) in `be/src/test/java/fpt/qn/mes/inventory/presentation/`

### Implementation for User Story 2

- [x] T014 [US2] Implement `getStockStatuses` in `be/src/main/java/fpt/qn/mes/inventory/application/service/InventoryService.java`
- [x] T015 [US2] Implement `GET /api/stock-statuses` endpoint in `be/src/main/java/fpt/qn/mes/inventory/presentation/InventoryController.java`

---

## Phase 5: User Story 3 - Movement Types Lookup API (`GET /api/movement-types`) (Priority: P1)

**Goal**: Allow clients to fetch all movement types via criteria search returning `MovementTypeSummaryDto` instances.

**Independent Test**: Execute `GET /api/movement-types` and verify HTTP 200 OK with list of `MovementTypeSummaryDto` items.

### Tests for User Story 3

- [x] T016 [P] [US3] Unit test: `InventoryServiceTest.java` (`getMovementTypes` criteria search) in `be/src/test/java/fpt/qn/mes/inventory/service/`
- [x] T017 [P] [US3] Controller slice test: `InventoryControllerTest.java` (`GET /api/movement-types`) in `be/src/test/java/fpt/qn/mes/inventory/presentation/`

### Implementation for User Story 3

- [x] T018 [US3] Implement `getMovementTypes` in `be/src/main/java/fpt/qn/mes/inventory/application/service/InventoryService.java`
- [x] T019 [US3] Implement `GET /api/movement-types` endpoint in `be/src/main/java/fpt/qn/mes/inventory/presentation/InventoryController.java`

---

## Phase 6: Polish & Verification

- [x] T020 [P] Ensure `/api/lot-types/**`, `/api/stock-statuses/**`, `/api/movement-types/**` are permitted in `SecurityConfig.java`
- [x] T021 [P] Run full test suite `./mvnw test -Djooq.codegen.skip=true` and confirm all 100% pass rate
