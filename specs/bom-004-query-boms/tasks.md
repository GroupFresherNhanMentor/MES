# Tasks: Get BOM List and BOM Detail (Query BOMs)

**Input**: Design documents from `specs/bom-004-query-boms/`  
**Prerequisites**: `plan.md`, `spec.md`, `research.md`, `data-model.md`, `contracts/query-boms-api.json`, `quickstart.md`

## Format: `- [ ] [ID] [P?] [Story?] Description with file path`

- **[P]**: Parallelizable (different files, no blocking dependencies)
- **[US1]**: User Story 1 - Get Paginated BOM List with Filters (Priority: P1)
- **[US2]**: User Story 2 - Get Full BOM Detail by ID (Priority: P1)

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Verify readiness of PageResponse and Exception models

- [x] T001 [P] Verify `PageResponse.java` in `be/src/main/java/fpt/qn/mes/common/dto/response/PageResponse.java`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Repository interface & jOOQ persistence adapter extensions for filtered query execution

- [x] T002 [P] Update `BomRepository.java` interface adding overloaded `findAll(int page, int size, UUID finishedProductId, UUID bomStatusId)` method in `be/src/main/java/fpt/qn/mes/bom/domain/repository/BomRepository.java`
- [x] T003 Implement dynamic jOOQ `Condition` filtering in `findAll` in `be/src/main/java/fpt/qn/mes/bom/infrastructure/persistence/BomPersistenceAdapter.java`

---

## Phase 3: User Story 1 - Get Paginated BOM List with Optional Filters (Priority: P1) 🎯 MVP

**Goal**: Support paginated query API with optional product and status filtering.

**Independent Test**: Send `GET /api/boms?page=0&size=10&finishedProductId=<UUID>&bomStatusId=<UUID>` and verify filtered paginated results.

### Tests for User Story 1 (REQUIRED)

- [x] T004 [P] [US1] Unit test: `getBoms_WithFilters_ReturnsFilteredPageResponse` in `be/src/test/java/fpt/qn/mes/bom/application/service/BomServiceTest.java`

### Implementation for User Story 1

- [x] T005 [US1] Update `getBoms` method signature in `BomUseCase.java` in `be/src/main/java/fpt/qn/mes/bom/application/port/in/BomUseCase.java`
- [x] T006 [US1] Implement filtered `getBoms(int page, int size, UUID finishedProductId, UUID bomStatusId)` in `be/src/main/java/fpt/qn/mes/bom/application/service/BomService.java`
- [x] T007 [US1] Update `getBoms` `@GetMapping` in `be/src/main/java/fpt/qn/mes/bom/presentation/BomController.java` to accept `@RequestParam(required = false) UUID finishedProductId` and `bomStatusId`

---

## Phase 4: User Story 2 - Get Full BOM Detail by ID (Priority: P1)

**Goal**: Ensure `GET /api/boms/{id}` returns complete BOM header details along with all component line items (`items`).

**Independent Test**: Send `GET /api/boms/{id}` for a BOM with 3 items; verify HTTP 200 response with full header metadata and 3 items in the `items` array.

### Tests for User Story 2 (REQUIRED)

- [x] T008 [P] [US2] Unit test: `getBomById_HappyPath_ReturnsBomWithItems` and `getBomById_NotFound_ThrowsBomNotFoundException` in `be/src/test/java/fpt/qn/mes/bom/application/service/BomServiceTest.java`

### Implementation for User Story 2

- [x] T009 [US2] Verify `getBomById(UUID id)` in `be/src/main/java/fpt/qn/mes/bom/application/service/BomService.java` loads items via `bomRepository.findById(id)`
- [x] T010 [US2] Verify `@GetMapping("/{id}")` endpoint in `be/src/main/java/fpt/qn/mes/bom/presentation/BomController.java`

---

## Phase 5: Polish & Cross-Cutting Concerns

**Purpose**: Verification & clean build

- [x] T011 [P] Run quickstart validation scenarios from `specs/bom-004-query-boms/quickstart.md`
- [x] T012 [P] Run full Maven test suite (`./mvnw test -Dtest=BomServiceTest`) and confirm clean build

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)** & **Foundational (Phase 2)**: Block User Story 1. Must complete T001–T003 first.
- **User Story 1 (Phase 3)**: Can proceed immediately after Phase 2.
- **User Story 2 (Phase 4)**: Can run in parallel with Phase 3 (independent endpoint).
- **Polish (Phase 5)**: Depends on completion of US1 & US2.

### Parallel Opportunities

- T001 & T002 (Setup & Repository interface) can be executed in parallel.
- T004 & T008 (US1 & US2 Unit tests) can be written in parallel.
- T011 & T012 (Quickstart validation & Maven test suite) can be executed in parallel.
