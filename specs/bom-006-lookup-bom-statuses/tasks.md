# Tasks: Get BOM Statuses (Lookup)

**Input**: Design documents from `specs/bom-006-lookup-bom-statuses/`  
**Prerequisites**: `plan.md`, `spec.md`, `research.md`, `data-model.md`, `contracts/lookup-bom-statuses-api.json`, `quickstart.md`

## Format: `- [ ] [ID] [P?] [Story?] Description with file path`

- **[P]**: Parallelizable (different files, no blocking dependencies)
- **[US1]**: User Story 1 - Retrieve BOM Status Lookup List (Priority: P1)

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Verify readiness of `LookupRepository` and `LookupEntry` in `fpt.qn.mes.common.service`

- [x] T001 [P] Verify `LookupRepository` and `LookupEntry` in `be/src/main/java/fpt/qn/mes/common/service/LookupRepository.java`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Application UseCase interface updates

- [x] T002 [P] Add `List<LookupEntry> getBomStatuses()` method signature to `BomUseCase.java` in `be/src/main/java/fpt/qn/mes/bom/application/port/in/BomUseCase.java`

---

## Phase 3: User Story 1 - Retrieve BOM Status Lookup List (Priority: P1) 🎯 MVP

**Goal**: Implement `getBomStatuses()` in `BomService` and wire `@GetMapping("/statuses")` endpoint in `BomController` returning `ApiResponse<List<LookupEntry>>`.

**Independent Test**: Send `GET /api/boms/statuses` with a valid JWT token; verify HTTP 200 OK response with array of lookup entries (`id`, `name`, `description`).

### Tests for User Story 1 (REQUIRED)

- [x] T003 [P] [US1] Unit test: `getBomStatuses_HappyPath_ReturnsStatusList` in `be/src/test/java/fpt/qn/mes/bom/application/service/BomServiceTest.java`

### Implementation for User Story 1

- [x] T004 [US1] Inject `LookupRepository` and implement `getBomStatuses()` using `lookupRepository.findAll("bom_statuses")` in `be/src/main/java/fpt/qn/mes/bom/application/service/BomService.java`
- [x] T005 [US1] Replace stub in `@GetMapping("/statuses")` with `ApiResponse.success(bomUseCase.getBomStatuses(), "OK")` in `be/src/main/java/fpt/qn/mes/bom/presentation/BomController.java`

---

## Phase 4: Polish & Cross-Cutting Concerns

**Purpose**: Validation & clean build

- [x] T006 [P] Run quickstart validation scenarios from `specs/bom-006-lookup-bom-statuses/quickstart.md`
- [x] T007 [P] Run full Maven test suite (`./mvnw test -Dtest=BomServiceTest`) and confirm clean build

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)** & **Foundational (Phase 2)**: Block User Story 1.
- **User Story 1 (Phase 3)**: Can proceed immediately after Phase 2.
- **Polish (Phase 4)**: Depends on US1 completion.

### Parallel Opportunities

- T001 & T002 can run in parallel.
- T003 (Unit test) can be written alongside implementation.
