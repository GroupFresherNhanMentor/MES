# Tasks: Activate Bill of Materials (Activate BOM)

**Input**: Design documents from `specs/bom-002-activate-bom/`  
**Prerequisites**: `plan.md`, `spec.md`, `research.md`, `data-model.md`, `contracts/activate-bom-api.json`, `quickstart.md`

## Format: `- [ ] [ID] [P?] [Story?] Description with file path`

- **[P]**: Parallelizable (different files, no blocking dependencies)
- **[US1]**: User Story 1 - Activate a Draft BOM (Priority: P1)
- **[US2]**: User Story 2 - Prevent Invalid Activation State Transitions (Priority: P2)

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Module exception classes initialization

- [x] T001 [P] Create `EmptyBomException.java` in `be/src/main/java/fpt/qn/mes/bom/application/exception/EmptyBomException.java`
- [x] T002 [P] Create `InvalidBomStatusException.java` in `be/src/main/java/fpt/qn/mes/bom/application/exception/InvalidBomStatusException.java`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Domain entity state methods & repository query contract extensions required before service/controller implementation

- [x] T003 Implement `activate()` state transition validation method in `be/src/main/java/fpt/qn/mes/bom/domain/entities/Bom.java`
- [x] T004 [P] Update `BomRepository.java` interface adding `deactivateActiveBomsForProduct` and `countItemsByBomId` methods in `be/src/main/java/fpt/qn/mes/bom/domain/repository/BomRepository.java`
- [x] T005 Implement `deactivateActiveBomsForProduct` and `countItemsByBomId` jOOQ queries in `be/src/main/java/fpt/qn/mes/bom/infrastructure/persistence/BomPersistenceAdapter.java`

---

## Phase 3: User Story 1 - Activate a Draft BOM (Priority: P1) 🎯 MVP

**Goal**: Enable Planners to activate a draft BOM header with at least 1 component item and automatically transition any existing active version to `INACTIVE`.

**Independent Test**: Send `POST /api/boms/{id}/activate` for a valid draft BOM with items; verify HTTP 200 response with `ACTIVE` status and that existing active version is set to `INACTIVE`.

### Tests for User Story 1 (REQUIRED)

- [x] T006 [P] [US1] Unit test: `activateBom_HappyPath_Success` and `activateBom_AutoDeactivatesPreviousActiveVersion` in `be/src/test/java/fpt/qn/mes/bom/application/service/BomServiceTest.java`

### Implementation for User Story 1

- [x] T007 [US1] Implement `activateBom(UUID id)` in `be/src/main/java/fpt/qn/mes/bom/application/service/BomService.java` with `@Transactional` single-transaction status update
- [x] T008 [US1] Implement `activateBom` `@PostMapping("/{id}/activate")` endpoint with `@PreAuthorize("hasAnyRole('ADMIN', 'PLANNER')")` in `be/src/main/java/fpt/qn/mes/bom/presentation/BomController.java`

---

## Phase 4: User Story 2 - Prevent Invalid Activation State Transitions (Priority: P2)

**Goal**: Reject activation attempts for empty draft BOMs (0 items), already `ACTIVE` BOMs, or historical `INACTIVE` BOMs with appropriate error responses.

**Independent Test**: Send activation request for empty BOM or already active BOM; verify 400 Bad Request response.

### Tests for User Story 2 (REQUIRED)

- [x] T009 [P] [US2] Unit test: `activateBom_EmptyBom_ThrowsEmptyBomException` and `activateBom_NonDraftStatus_ThrowsInvalidBomStatusException` in `be/src/test/java/fpt/qn/mes/bom/application/service/BomServiceTest.java`

### Implementation for User Story 2

- [x] T010 [US2] Add empty BOM validation (`countItemsByBomId == 0`) in `be/src/main/java/fpt/qn/mes/bom/application/service/BomService.java`
- [x] T011 [US2] Add status state validation (must be `DRAFT`) in `be/src/main/java/fpt/qn/mes/bom/application/service/BomService.java`

---

## Phase 5: Polish & Cross-Cutting Concerns

**Purpose**: Verification & clean build

- [x] T012 [P] Run quickstart validation scenarios from `specs/bom-002-activate-bom/quickstart.md`
- [x] T013 [P] Run full Maven test suite (`./mvnw test -Dtest=BomServiceTest`) and confirm clean build

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)** & **Foundational (Phase 2)**: Block all User Stories. Must complete T001–T005 first.
- **User Story 1 (Phase 3)**: Can proceed immediately after Phase 2.
- **User Story 2 (Phase 4)**: Extends Service validation logic; depends on US1 service implementation.
- **Polish (Phase 5)**: Depends on completion of US1 & US2.

### Parallel Opportunities

- T001 & T002 (Exception classes) can be created in parallel.
- T004 & T006 (Repository interface & Unit tests) can be written in parallel.
- T009 & T012 (US2 Unit tests & Quickstart validation) can be executed in parallel.
