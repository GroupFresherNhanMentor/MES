# Tasks: Create Bill of Materials Header (Create BOM Header)

**Input**: Design documents from `specs/bom-001-create-header/`  
**Prerequisites**: `plan.md`, `spec.md`, `research.md`, `data-model.md`, `contracts/create-bom-api.json`, `quickstart.md`

## Format: `- [ ] [ID] [P?] [Story] Description`

- **[P]**: Parallelizable (different files, no blocking dependencies)
- **[US1]**: User Story 1 - Create BOM Header (P1)
- **[US2]**: User Story 2 - Validation & Edge Cases (P2)

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Module structure verification & Exception classes initialization

- [x] T001 Verify `fpt.qn.mes.bom` Clean Architecture 4-layer package structure in `be/src/main/java/fpt/qn/mes/bom/`
- [x] T002 [P] Create `BomAlreadyExistsException.java` in `be/src/main/java/fpt/qn/mes/bom/application/exception/BomAlreadyExistsException.java`
- [x] T003 [P] Create `InvalidBomProductTypeException.java` in `be/src/main/java/fpt/qn/mes/bom/application/exception/InvalidBomProductTypeException.java`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core domain model & repository interfaces required before service/controller implementation

- [x] T004 Implement `Bom.create(...)` domain factory method in `be/src/main/java/fpt/qn/mes/bom/domain/entities/Bom.java`
- [x] T005 [P] Update `BomRepository.java` interface methods (`findById`, `save`, `existsByFinishedProductIdAndVersion`) in `be/src/main/java/fpt/qn/mes/bom/domain/repository/BomRepository.java`
- [x] T006 [P] Implement `BomRecordMapper.java` (jOOQ `BomsRecord` ↔ `Bom` entity) in `be/src/main/java/fpt/qn/mes/bom/infrastructure/persistence/BomRecordMapper.java`

---

## Phase 3: User Story 1 - Create BOM Header (Priority: P1) 🎯 MVP

**Goal**: Enable Planners to create a new draft BOM header for valid finished/semi-finished goods.

**Independent Test**: Send `POST /api/boms` with valid `finishedProductId` and `version: 1`, verify HTTP 201 Created and `DRAFT` status response.

### Tests for User Story 1 (REQUIRED)

- [x] T007 [P] [US1] Unit test: `BomServiceTest.java` happy path in `be/src/test/java/fpt/qn/mes/bom/application/service/BomServiceTest.java`
- [ ] T008 [P] [US1] Integration test: `BomIntegrationTest.java` HTTP 201 creation flow in `be/src/test/java/fpt/qn/mes/bom/presentation/BomIntegrationTest.java`

### Implementation for User Story 1

- [x] T009 [P] [US1] Implement `CreateBomRequest.java` validation annotations in `be/src/main/java/fpt/qn/mes/bom/application/dto/request/CreateBomRequest.java`
- [x] T010 [P] [US1] Verify `BomDtoMapper.java` MapStruct interface in `be/src/main/java/fpt/qn/mes/bom/application/mapper/BomDtoMapper.java`
- [x] T011 [US1] Implement `save` and `existsByFinishedProductIdAndVersion` in `be/src/main/java/fpt/qn/mes/bom/infrastructure/persistence/BomPersistenceAdapter.java`
- [x] T012 [US1] Implement `createBom` in `be/src/main/java/fpt/qn/mes/bom/application/service/BomService.java` (interacts with `ProductUseCase` & `CurrentUserPort`)
- [x] T013 [US1] Implement `createBom` `@PostMapping` endpoint in `be/src/main/java/fpt/qn/mes/bom/presentation/BomController.java`

---

## Phase 4: User Story 2 - Prevent Invalid & Duplicate BOM Creation (Priority: P2)

**Goal**: Reject invalid product types, missing products, and duplicate versions with appropriate error responses.

**Independent Test**: Send invalid product ID, duplicate version, or Raw Material ID; verify 400, 404, or 409 error responses.

### Tests for User Story 2 (REQUIRED)

- [x] T014 [P] [US2] Unit test: `BomServiceTest.java` exception branches (duplicate version, raw material type, missing product) in `be/src/test/java/fpt/qn/mes/bom/application/service/BomServiceTest.java`
- [ ] T015 [P] [US2] Integration test: `BomIntegrationTest.java` 400 Bad Request & 409 Conflict validation in `be/src/test/java/fpt/qn/mes/bom/presentation/BomIntegrationTest.java`
- [ ] T016 [P] [US2] Concurrency test: `BomIntegrationTest.java` parallel duplicate version creation attempts using `ExecutorService`

### Implementation for User Story 2

- [x] T017 [US2] Add product type validation logic (`FINISHED_GOOD`, `SEMI_FINISHED`) in `be/src/main/java/fpt/qn/mes/bom/application/service/BomService.java`
- [x] T018 [US2] Add version existence check logic in `be/src/main/java/fpt/qn/mes/bom/application/service/BomService.java`

---

## Phase 5: Polish & Cross-Cutting Concerns

**Purpose**: Verification, documentation & test coverage

- [ ] T019 [P] Run quickstart scenarios from `specs/bom-001-create-header/quickstart.md`
- [x] T020 [P] Run full Maven test suite (`./mvnw test -Dtest=BomServiceTest`) and confirm clean build
- [ ] T021 Update `docs/api-endpoints.md` with new `POST /api/boms` request/response shape

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)** & **Foundational (Phase 2)**: Block all User Stories. Must complete T001–T006 first.
- **User Story 1 (Phase 3)**: Can proceed immediately after Phase 2.
- **User Story 2 (Phase 4)**: Extends Service validation; depends on US1 service implementation.
- **Polish (Phase 5)**: Depends on completion of US1 & US2.

### Parallel Opportunities

- T002 & T003 (Exceptions) can be created in parallel.
- T005 & T006 (Repository interface & Record mapper) can be created in parallel.
- T007 & T008 (US1 Unit & Integration tests) can be written in parallel.
- T009 & T010 (DTO request & DTO mapper) can be updated in parallel.
- T014, T015, T016 (US2 Exception & Concurrency tests) can be created in parallel.
