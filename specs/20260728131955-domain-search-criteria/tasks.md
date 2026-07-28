# Tasks: Inventory Domain Search Criteria & PageResponse Refactoring

**Input**: Design documents from `/specs/20260728131955-domain-search-criteria/`

**Prerequisites**: plan.md (required), spec.md (required for user stories), research.md, data-model.md, contracts/

**Tests**: Tests are required for every user story covering criteria parameter mapping, jOOQ dynamic filtering, and PageResponse metadata output.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- File paths are relative to repository root (`be/src/main/java/...`, `be/src/test/java/...`)

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Shared infrastructure and base criteria classes

- [X] T001 Create `BaseSearchCriteria` pure Java abstract base class in `be/src/main/java/fpt/qn/mes/common/dto/BaseSearchCriteria.java`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core infrastructure that MUST be complete before ANY user story can be implemented

- [X] T002 Verify `PageResponse<T>` metadata and `.map(...)` helper in `be/src/main/java/fpt/qn/mes/common/dto/response/PageResponse.java`

**Checkpoint**: Base criteria and PageResponse ready - user story implementation can now begin

---

## Phase 3: User Story 1 - Paginated Search for StockBalances returning PageResponse (Priority: P1) 🎯 MVP

**Goal**: Implement `StockBalanceSearchRequest`, `StockBalanceSearchCriteria`, and refactor `StockBalanceRepository` & `StockBalancePersistenceAdapter` to return `PageResponse<StockBalance>`.

**Independent Test**: Can be verified via `StockBalanceSearchTest` by passing `StockBalanceSearchCriteria` with filtering fields and verifying `PageResponse<StockBalance>` items and pagination metadata.

### Implementation for User Story 1

- [X] T003 [P] [US1] Create `StockBalanceSearchRequest` DTO extending `PageRequest` in `be/src/main/java/fpt/qn/mes/inventory/application/dto/request/StockBalanceSearchRequest.java`
- [X] T004 [P] [US1] Create `StockBalanceSearchCriteria` extending `BaseSearchCriteria` in `be/src/main/java/fpt/qn/mes/inventory/domain/repository/StockBalanceSearchCriteria.java`
- [X] T005 [US1] Refactor `StockBalanceRepository` interface signature to return `PageResponse<StockBalance>` in `be/src/main/java/fpt/qn/mes/inventory/domain/repository/StockBalanceRepository.java`
- [X] T006 [US1] Refactor `StockBalancePersistenceAdapter` to build dynamic jOOQ query and return `PageResponse<StockBalance>` in `be/src/main/java/fpt/qn/mes/inventory/infrastructure/persistence/StockBalancePersistenceAdapter.java`
- [X] T007 [P] [US1] Write unit test `StockBalanceSearchTest` in `be/src/test/java/fpt/qn/mes/inventory/persistence/StockBalanceSearchTest.java`

**Checkpoint**: StockBalance search refactoring is fully functional and testable independently.

---

## Phase 4: User Story 2 - Paginated Search for StockLots returning PageResponse (Priority: P2)

**Goal**: Implement `StockLotSearchRequest`, `StockLotSearchCriteria`, and refactor `StockLotRepository` & `StockLotPersistenceAdapter` to return `PageResponse<StockLot>`.

**Independent Test**: Verified via `StockLotSearchTest` executing paginated lot searches with filters.

### Implementation for User Story 2

- [X] T008 [P] [US2] Create `StockLotSearchRequest` DTO extending `PageRequest` in `be/src/main/java/fpt/qn/mes/inventory/application/dto/request/StockLotSearchRequest.java`
- [X] T009 [P] [US2] Create `StockLotSearchCriteria` extending `BaseSearchCriteria` in `be/src/main/java/fpt/qn/mes/inventory/domain/repository/StockLotSearchCriteria.java`
- [X] T010 [US2] Refactor `StockLotRepository` interface signature to return `PageResponse<StockLot>` in `be/src/main/java/fpt/qn/mes/inventory/domain/repository/StockLotRepository.java`
- [X] T011 [US2] Refactor `StockLotPersistenceAdapter` to build dynamic jOOQ query and return `PageResponse<StockLot>` in `be/src/main/java/fpt/qn/mes/inventory/infrastructure/persistence/StockLotPersistenceAdapter.java`
- [X] T012 [P] [US2] Write unit test `StockLotSearchTest` in `be/src/test/java/fpt/qn/mes/inventory/persistence/StockLotSearchTest.java`

**Checkpoint**: StockLot search refactoring is fully functional and testable independently.

---

## Phase 5: User Story 3 - Paginated Search for StockMovements returning PageResponse (Priority: P3)

**Goal**: Implement `StockMovementSearchRequest`, `StockMovementSearchCriteria`, and refactor `StockMovementRepository` & `StockMovementPersistenceAdapter` to return `PageResponse<StockMovement>`.

**Independent Test**: Verified via `StockMovementSearchTest` querying transaction log ledgers.

### Implementation for User Story 3

- [X] T013 [P] [US3] Create `StockMovementSearchRequest` DTO extending `PageRequest` in `be/src/main/java/fpt/qn/mes/inventory/application/dto/request/StockMovementSearchRequest.java`
- [X] T014 [P] [US3] Create `StockMovementSearchCriteria` extending `BaseSearchCriteria` in `be/src/main/java/fpt/qn/mes/inventory/domain/repository/StockMovementSearchCriteria.java`
- [X] T015 [US3] Refactor `StockMovementRepository` interface signature to return `PageResponse<StockMovement>` in `be/src/main/java/fpt/qn/mes/inventory/domain/repository/StockMovementRepository.java`
- [X] T016 [US3] Refactor `StockMovementPersistenceAdapter` to build dynamic jOOQ query and return `PageResponse<StockMovement>` in `be/src/main/java/fpt/qn/mes/inventory/infrastructure/persistence/StockMovementPersistenceAdapter.java`
- [X] T017 [P] [US3] Write unit test `StockMovementSearchTest` in `be/src/test/java/fpt/qn/mes/inventory/persistence/StockMovementSearchTest.java`

**Checkpoint**: All three search criteria user stories are complete and independently testable.

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Service layer alignment and full regression test execution

- [X] T018 [P] Update inventory application service and controller callers to accept request DTOs and pass criteria objects in `be/src/main/java/fpt/qn/mes/inventory/`
- [X] T019 Execute complete test suite via `./mvnw test` from `be/` to verify zero regression

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies - creates `BaseSearchCriteria`
- **Foundational (Phase 2)**: Depends on Setup completion - BLOCKS all user stories
- **User Stories (Phase 3+)**: All depend on Foundational phase completion
  - User stories can proceed in parallel or sequentially (P1 → P2 → P3)
- **Polish (Phase 6)**: Depends on all user stories being complete

### User Story Dependencies

- **User Story 1 (P1)**: Can start after Foundational (Phase 2)
- **User Story 2 (P2)**: Can start after Foundational (Phase 2)
- **User Story 3 (P3)**: Can start after Foundational (Phase 2)

### Parallel Opportunities

- T003, T004, T007 (US1 DTO, Criteria, Test) can run in parallel
- T008, T009, T012 (US2 DTO, Criteria, Test) can run in parallel
- T013, T014, T017 (US3 DTO, Criteria, Test) can run in parallel
- US1, US2, and US3 can be implemented in parallel once Phase 1 & 2 are complete

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1 & Phase 2
2. Complete Phase 3 (User Story 1 - StockBalance)
3. **STOP and VALIDATE**: Verify `StockBalanceSearchTest` passes cleanly
4. Proceed with User Story 2 and User Story 3
