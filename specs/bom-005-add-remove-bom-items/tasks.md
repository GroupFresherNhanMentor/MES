# Tasks: Add and Remove BOM Items

**Input**: Design documents from `specs/bom-005-add-remove-bom-items/`  
**Prerequisites**: `plan.md`, `spec.md`, `research.md`, `data-model.md`, `contracts/add-remove-bom-items-api.json`, `quickstart.md`

## Format: `- [ ] [ID] [P?] [Story?] Description with file path`

- **[P]**: Parallelizable (different files, no blocking dependencies)
- **[US1]**: User Story 1 - Add Component Item to DRAFT BOM (Priority: P1)
- **[US2]**: User Story 2 - Remove Component Item from DRAFT BOM (Priority: P1)

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Verify DTO validation rules and Mapper mappings for BOM items

- [x] T001 [P] Verify `@Positive` validation for `quantityPerUnit` in `be/src/main/java/fpt/qn/mes/bom/application/dto/request/CreateBomItemRequest.java`
- [x] T002 [P] Verify `BomDtoMapper.java` mapping for `BomItem` to `BomItemDto` in `be/src/main/java/fpt/qn/mes/bom/application/mapper/BomDtoMapper.java`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Immutability guard validation & Repository item persistence checks

- [x] T003 [P] Verify `saveItem` and `deleteItemById` methods in `be/src/main/java/fpt/qn/mes/bom/domain/repository/BomRepository.java`
- [x] T004 Verify jOOQ item persistence implementation in `be/src/main/java/fpt/qn/mes/bom/infrastructure/persistence/BomPersistenceAdapter.java`

---

## Phase 3: User Story 1 - Add Component Item to DRAFT BOM (Priority: P1) 🎯 MVP

**Goal**: Allow Planners and Admins to add component line items (`POST /api/boms/{bomId}/items`) to a DRAFT BOM header with strict immutability checks.

**Independent Test**: Send `POST /api/boms/{bomId}/items` with valid material product ID, quantity, unit, and scrap rate on a DRAFT BOM; verify HTTP 201 Created response.

### Tests for User Story 1 (REQUIRED)

- [x] T005 [P] [US1] Unit test: `addBomItem_HappyPath_Success` in `be/src/test/java/fpt/qn/mes/bom/application/service/BomServiceTest.java`
- [x] T006 [P] [US1] Unit test: `addBomItem_NonDraftBom_ThrowsInvalidBomStatusException` in `be/src/test/java/fpt/qn/mes/bom/application/service/BomServiceTest.java`
- [x] T007 [P] [US1] Unit test: `addBomItem_BomNotFound_ThrowsBomNotFoundException` in `be/src/test/java/fpt/qn/mes/bom/application/service/BomServiceTest.java`

### Implementation for User Story 1

- [x] T008 [US1] Verify `addBomItem` method signature in `be/src/main/java/fpt/qn/mes/bom/application/port/in/BomUseCase.java`
- [x] T009 [US1] Verify `@Transactional addBomItem(UUID bomId, CreateBomItemRequest request)` implementation with DRAFT status check in `be/src/main/java/fpt/qn/mes/bom/application/service/BomService.java`
- [x] T010 [US1] Verify `@PostMapping("/{bomId}/items")` with `@PreAuthorize("hasAnyRole('ADMIN', 'PLANNER')")` in `be/src/main/java/fpt/qn/mes/bom/presentation/BomController.java`

---

## Phase 4: User Story 2 - Remove Component Item from DRAFT BOM (Priority: P1)

**Goal**: Allow Planners and Admins to delete component items (`DELETE /api/boms/{bomId}/items/{itemId}`) from a DRAFT BOM.

**Independent Test**: Send `DELETE /api/boms/{bomId}/items/{itemId}` on a DRAFT BOM; verify HTTP 204 No Content response and item removal.

### Tests for User Story 2 (REQUIRED)

- [x] T011 [P] [US2] Unit test: `deleteBomItem_HappyPath_Success` in `be/src/test/java/fpt/qn/mes/bom/application/service/BomServiceTest.java`
- [x] T012 [P] [US2] Unit test: `deleteBomItem_NonDraftBom_ThrowsInvalidBomStatusException` in `be/src/test/java/fpt/qn/mes/bom/application/service/BomServiceTest.java`
- [x] T013 [P] [US2] Unit test: `deleteBomItem_BomNotFound_ThrowsBomNotFoundException` in `be/src/test/java/fpt/qn/mes/bom/application/service/BomServiceTest.java`

### Implementation for User Story 2

- [x] T014 [US2] Verify `deleteBomItem` method signature in `be/src/main/java/fpt/qn/mes/bom/application/port/in/BomUseCase.java`
- [x] T015 [US2] Verify `@Transactional deleteBomItem(UUID bomId, UUID itemId)` implementation with DRAFT status check in `be/src/main/java/fpt/qn/mes/bom/application/service/BomService.java`
- [x] T016 [US2] Verify `@DeleteMapping("/{bomId}/items/{itemId}")` with `@PreAuthorize("hasAnyRole('ADMIN', 'PLANNER')")` in `be/src/main/java/fpt/qn/mes/bom/presentation/BomController.java`

---

## Phase 5: Polish & Cross-Cutting Concerns

**Purpose**: Validation & clean build

- [x] T017 [P] Run quickstart validation scenarios from `specs/bom-005-add-remove-bom-items/quickstart.md`
- [x] T018 [P] Run full Maven test suite (`./mvnw test -Dtest=BomServiceTest`) and confirm clean build

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)** & **Foundational (Phase 2)**: Block User Story 1 and User Story 2.
- **User Story 1 (Phase 3)** & **User Story 2 (Phase 4)**: Can proceed in parallel once Phase 2 is verified.
- **Polish (Phase 5)**: Depends on US1 & US2 completion.

### Parallel Opportunities

- T001 & T002 (Setup DTO and Mapper verification) can run in parallel.
- T005–T007 (US1 unit tests) and T011–T013 (US2 unit tests) can be executed in parallel.
