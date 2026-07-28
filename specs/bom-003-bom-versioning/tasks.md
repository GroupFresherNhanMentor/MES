# Tasks: Bill of Materials Versioning (BOM Versioning)

**Input**: Design documents from `specs/bom-003-bom-versioning/`  
**Prerequisites**: `plan.md`, `spec.md`, `research.md`, `data-model.md`, `contracts/bom-versioning-api.json`, `quickstart.md`

## Format: `- [ ] [ID] [P?] [Story?] Description with file path`

- **[P]**: Parallelizable (different files, no blocking dependencies)
- **[US1]**: User Story 1 - Create a New BOM Version from Existing BOM (Priority: P1)
- **[US2]**: User Story 2 - Prevent Direct Editing of NON-DRAFT BOMs (Priority: P2)

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Verify exception classes readiness for BOM Versioning

- [x] T001 [P] Verify `InvalidBomStatusException.java` in `be/src/main/java/fpt/qn/mes/bom/application/exception/InvalidBomStatusException.java`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Repository contract extensions for max version lookup & batch item cloning required before service/controller implementation

- [x] T002 [P] Update `BomRepository.java` interface adding `findMaxVersionByFinishedProductId` method in `be/src/main/java/fpt/qn/mes/bom/domain/repository/BomRepository.java`
- [x] T003 Implement `findMaxVersionByFinishedProductId` jOOQ query in `be/src/main/java/fpt/qn/mes/bom/infrastructure/persistence/BomPersistenceAdapter.java`

---

## Phase 3: User Story 1 - Create a New BOM Version from Existing BOM (Priority: P1) 🎯 MVP

**Goal**: Enable Planners to create a new draft BOM version cloned from an existing BOM with `version = maxVersion + 1` and all items deep-copied.

**Independent Test**: Send `POST /api/boms/{id}/new-version` for an existing BOM; verify HTTP 201 response with `version = maxVersion + 1`, `DRAFT` status, and cloned component items.

### Tests for User Story 1 (REQUIRED)

- [x] T004 [P] [US1] Unit test: `createNewVersion_HappyPath_Success` and `createNewVersion_SourceNotFound_ThrowsBomNotFoundException` in `be/src/test/java/fpt/qn/mes/bom/application/service/BomServiceTest.java`

### Implementation for User Story 1

- [x] T005 [US1] Add `createNewVersion(UUID id)` method to `BomUseCase.java` interface in `be/src/main/java/fpt/qn/mes/bom/application/port/in/BomUseCase.java`
- [x] T006 [US1] Implement `createNewVersion(UUID id)` in `be/src/main/java/fpt/qn/mes/bom/application/service/BomService.java` with `@Transactional` cloning logic
- [x] T007 [US1] Implement `createNewVersion` `@PostMapping("/{id}/new-version")` endpoint with `@PreAuthorize("hasAnyRole('ADMIN', 'PLANNER')")` in `be/src/main/java/fpt/qn/mes/bom/presentation/BomController.java`

---

## Phase 4: User Story 2 - Prevent Direct Editing of NON-DRAFT BOMs (Priority: P2)

**Goal**: Reject attempts to add or delete items on BOMs that are in `ACTIVE` or `INACTIVE` status with a 400 Bad Request response (`InvalidBomStatusException`).

**Independent Test**: Send item addition or item deletion request to an `ACTIVE` or `INACTIVE` BOM; verify 400 Bad Request response.

### Tests for User Story 2 (REQUIRED)

- [x] T008 [P] [US2] Unit test: `addBomItem_NonDraftBom_ThrowsInvalidBomStatusException` and `deleteBomItem_NonDraftBom_ThrowsInvalidBomStatusException` in `be/src/test/java/fpt/qn/mes/bom/application/service/BomServiceTest.java`

### Implementation for User Story 2

- [x] T009 [US2] Add status validation check in `addBomItem` method in `be/src/main/java/fpt/qn/mes/bom/application/service/BomService.java`
- [x] T010 [US2] Add status validation check in `deleteBomItem` method in `be/src/main/java/fpt/qn/mes/bom/application/service/BomService.java`

---

## Phase 5: Polish & Cross-Cutting Concerns

**Purpose**: Verification & clean build

- [x] T011 [P] Run quickstart validation scenarios from `specs/bom-003-bom-versioning/quickstart.md`
- [x] T012 [P] Run full Maven test suite (`./mvnw test -Dtest=BomServiceTest`) and confirm clean build

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)** & **Foundational (Phase 2)**: Block all User Stories. Must complete T001–T003 first.
- **User Story 1 (Phase 3)**: Can proceed immediately after Phase 2.
- **User Story 2 (Phase 4)**: Extends Service item modification methods; depends on US1.
- **Polish (Phase 5)**: Depends on completion of US1 & US2.

### Parallel Opportunities

- T001 & T002 (Setup & Repository interface) can be executed in parallel.
- T004 & T008 (US1 & US2 Unit tests) can be written in parallel.
- T011 & T012 (Quickstart validation & Maven test suite) can be executed in parallel.
