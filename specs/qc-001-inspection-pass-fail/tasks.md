# Tasks: QC Inspection — Pass / Fail

**Input**: Design documents from `specs/001-qc-inspection-pass-fail/`

**Prerequisites**: plan.md (required), spec.md (required for user stories), research.md, data-model.md, contracts/

**Tests**: Tests are REQUIRED for every feature. Every user story must include unit tests for service logic and integration tests for the full HTTP → DB flow. See `docs/rules/backend-testing.md`.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

## Path Conventions

- Java source: `be/src/main/java/fpt/qn/mes/quality/`
- Test source: `be/src/test/java/fpt/qn/mes/quality/`
- DB migrations: `be/src/main/resources/db/migration/`
- Seed data: `be/src/main/resources/seed/`

---

## Phase 1: Setup

**Purpose**: Verify project structure and ensure build compiles

- [X] T001 [P] Verify quality module file structure exists with 4-layer architecture (domain, application, infrastructure, presentation)
- [X] T002 [P] Verify Flyway migrations applied — qc_statuses, defect_types, quality_inspections, quality_inspection_results tables exist
- [X] T003 [P] Verify seed data files exist for qc-statuses.json, qc-actions.json, defect-types.json
- [X] T004 Verify project compiles with `cd be && ./mvnw compile -q`

**Checkpoint**: Build compiles clean — Phase 1 complete

---

## Phase 2: Foundational — DTOs, Exceptions, Domain, Repository Interfaces

**Purpose**: Core artifacts that ALL user stories depend on

- [X] T005 [P] Add missing fields to CreateQualityInspectionRequest in `be/src/main/java/fpt/qn/mes/quality/application/dto/request/CreateQualityInspectionRequest.java` — add `@NotNull` to workOrderId, lotId, qcStatusId
- [X] T006 [P] Create PassQcRequest in `be/src/main/java/fpt/qn/mes/quality/application/dto/request/PassQcRequest.java` with passedQuantity (BigDecimal, @NotNull, @Min), note fields
- [X] T007 [P] Create FailQcRequest in `be/src/main/java/fpt/qn/mes/quality/application/dto/request/FailQcRequest.java` with failedQuantity, actionId, defectTypeId, reason, note fields
- [X] T008 [P] Create PassQcResponse in `be/src/main/java/fpt/qn/mes/quality/application/dto/response/PassQcResponse.java` with resultId, qcStatusName, stockMovementId, message
- [X] T009 [P] Create FailQcResponse in `be/src/main/java/fpt/qn/mes/quality/application/dto/response/FailQcResponse.java` with resultId, qcStatusName, stockMovementId, message
- [X] T010 [P] Create QcStatusDto, QcActionDto, DefectTypeDto in `be/src/main/java/fpt/qn/mes/quality/application/dto/response/`
- [X] T011 [P] Add remainingQuantity field and computed methods to QualityInspectionDto in `be/src/main/java/fpt/qn/mes/quality/application/dto/response/QualityInspectionDto.java`
- [X] T012 [P] Create exceptions: InsufficientRemainingQuantityException, InvalidQcActionException, InspectionAlreadyClosedException, DefectTypeRequiredException, ReasonRequiredException in `be/src/main/java/fpt/qn/mes/quality/application/exception/`
- [X] T013 [P] Create QcStatusRepository in `be/src/main/java/fpt/qn/mes/quality/domain/repository/QcStatusRepository.java`
- [X] T014 [P] Create QcActionRepository in `be/src/main/java/fpt/qn/mes/quality/domain/repository/QcActionRepository.java`
- [X] T015 [P] Create DefectTypeRepository in `be/src/main/java/fpt/qn/mes/quality/domain/repository/DefectTypeRepository.java`
- [X] T016 [P] Add stock-related methods to QualityInspectionRepository — sumResultQuantities, updateStatus, countResults
- [X] T017 [P] Create QcStockPort in `be/src/main/java/fpt/qn/mes/quality/application/port/out/QcStockPort.java` for stock transitions
- [X] T018 [P] Create QcStatusDtoMapper, QcActionDtoMapper, DefectTypeDtoMapper in `be/src/main/java/fpt/qn/mes/quality/application/mapper/`
- [X] T019 [P] Create DefectType domain entity in `be/src/main/java/fpt/qn/mes/quality/domain/entities/DefectType.java`
- [X] T020 [P] Create QcAction domain entity in `be/src/main/java/fpt/qn/mes/quality/domain/entities/QcAction.java`
- [X] T021 [P] Create QcStatus domain entity in `be/src/main/java/fpt/qn/mes/quality/domain/entities/QcStatus.java`

**Checkpoint**: All foundational artifacts created — user story implementation can begin

---

## Phase 3: User Story 1 — QC Inspector passes good lots (Priority: P1) 🎯 MVP

**Goal**: QC Inspector passes units → status PASSED, stock moves QUALITY_INSPECTION → AVAILABLE, QC_RELEASE movement recorded

**Independent Test**: Create inspection, call pass with quantity, verify stock moves and QC_RELEASE movement

### Implementation for User Story 1

- [X] T022 [P] [US1] Implement pass() method in `be/src/main/java/fpt/qn/mes/quality/domain/entities/QualityInspection.java` — compute remaining quantity, validate transition is allowed
- [X] T023 [P] [US1] Create QcStatusPersistenceAdapter in `be/src/main/java/fpt/qn/mes/quality/infrastructure/persistence/QcStatusPersistenceAdapter.java`
- [X] T024 [P] [US1] Create QcActionPersistenceAdapter in `be/src/main/java/fpt/qn/mes/quality/infrastructure/persistence/QcActionPersistenceAdapter.java`
- [X] T025 [P] [US1] Create DefectTypePersistenceAdapter in `be/src/main/java/fpt/qn/mes/quality/infrastructure/persistence/DefectTypePersistenceAdapter.java`
- [X] T026 [P] [US1] Create QcStockAdapter in `be/src/main/java/fpt/qn/mes/quality/infrastructure/persistence/QcStockAdapter.java` — implement stock balance update, stock movement creation
- [X] T027 [P] [US1] Implement QualityInspectionRecordMapper fully in `be/src/main/java/fpt/qn/mes/quality/infrastructure/persistence/QualityRecordMapper.java`
- [X] T028 [P] [US1] Implement QualityInspectionPersistenceAdapter fully in `be/src/main/java/fpt/qn/mes/quality/infrastructure/persistence/QualityPersistenceAdapter.java`
- [X] T029 [US1] Implement pass flow service method in `be/src/main/java/fpt/qn/mes/quality/application/service/QualityService.java` — add `passInspection(UUID id, PassQcRequest request)` method
- [X] T030 [US1] Add pass endpoint in `be/src/main/java/fpt/qn/mes/quality/presentation/QualityController.java` — `POST /api/quality-inspections/{inspectionId}/pass`
- [X] T031 [US1] Add getQcStatuses, getQcActions, getDefectTypes lookup endpoints in `be/src/main/java/fpt/qn/mes/quality/presentation/QualityController.java`

### Tests for User Story 1

- [X] T032 [P] [US1] Unit test: `QualityInspectionServiceTest` — pass happy path + pass exceeds remaining throws exception + pass on already closed inspection throws + quantity must be > 0
- [X] T033 [P] [US1] Integration test: `QualityInspectionIntegrationTest` — pass flow creates stock movement, 400 on exceeded quantity, 401 unauthenticated, 403 wrong role

**Checkpoint**: US1 complete — pass flow works end-to-end

---

## Phase 4: User Story 2 — QC Inspector fails defective lots (Priority: P1)

**Goal**: QC Inspector fails units with SCRAP/HOLD/REWORK → appropriate status, stock transitions, movement

**Independent Test**: Create inspection, call fail with SCRAP, verify stock → SCRAPPED and SCRAP movement

### Implementation for User Story 2

- [X] T034 [P] [US2] Add fail() method logic in `be/src/main/java/fpt/qn/mes/quality/domain/entities/QualityInspection.java` — validate fail action parameters
- [X] T035 [US2] Implement fail flow service method in `be/src/main/java/fpt/qn/mes/quality/application/service/QualityService.java` — add `failInspection(UUID id, FailQcRequest request)` method with action routing (SCRAP/HOLD/REWORK)
- [X] T036 [US2] Add fail endpoint in `be/src/main/java/fpt/qn/mes/quality/presentation/QualityController.java` — `POST /api/quality-inspections/{inspectionId}/fail`
- [X] T037 [US2] Validate fail request — defectTypeId + reason required when fail, check actionId valid

### Tests for User Story 2

- [X] T038 [P] [US2] Unit test: `QualityInspectionServiceTest` — fail SCRAP happy path, fail HOLD, fail REWORK, fail without defectTypeId/reason throws, invalid actionId throws
- [X] T039 [P] [US2] Integration test: `QualityInspectionIntegrationTest` — fail SCRAP verifies stock → SCRAPPED, fail HOLD verifies ON_HOLD, fail REWORK no stock movement, 400 validation errors

**Checkpoint**: US2 complete — pass and fail flows work

---

## Phase 5: User Story 3 — Partial inspection (Priority: P2)

**Goal**: Multiple pass/fail rounds on same inspection, accumulates correctly, status changes only after fully processed

**Independent Test**: Pass 30, pass 30, fail 40 → verify final FAILED, stock 60 AVAILABLE, 40 SCRAPPED

### Implementation for User Story 3

- [X] T040 [P] [US3] Ensure pass() and fail() handle partial quantities — remaining quantity computed as `quantity - SUM(results.quantity)`, inspection stays PENDING_INSPECTION until fully processed
- [X] T041 [P] [US3] Update getInspectionById to include computed remainingQuantity in DTO
- [X] T042 [P] [US3] Ensure status transition only happens when remaining = 0 (fully processed)

### Tests for User Story 3

- [X] T043 [P] [US3] Unit test: `QualityInspectionServiceTest` — partial pass partial fail accumulates correctly, status stays pending until full, multiple passes then fail
- [X] T044 [P] [US3] Integration test: `QualityInspectionIntegrationTest` — partial inspection scenario with 3 rounds, verify final state and stock
- [ ] T045 [US3] Concurrency test: concurrent pass/fail calls on same inspection — optimistic locking prevents double-counting, exactly expected state after

**Checkpoint**: US3 complete — partial inspection works, concurrency handled

---

## Phase 6: User Story 4 — Admin manages QC lookup tables (Priority: P3)

**Goal**: Admin CRUD for qc_statuses, qc_actions, defect_types. All authenticated users can view.

**Independent Test**: Create a new QC status, verify it appears in list

- [X] T046 [P] [US4] Implement lookup create endpoints in `QualityController.java` — POST /api/quality-inspections/statuses, POST actions, POST defect-types
- [X] T047 [P] [US4] Add create methods to QualityService — createQcStatus, createQcAction, createDefectType

### Tests for User Story 4

- [X] T048 [P] [US4] Integration test: create QC status returns 201 for ADMIN, 403 for non-admin, 401 for unauthenticated
- [X] T049 [P] [US4] Integration test: lookup lists return 200 for all authenticated users

**Checkpoint**: US4 complete — all user stories implemented

---

## Phase 7: Polish & Cross-Cutting Concerns

- [X] T050 [P] Update `api-endpoints.md` in `docs/api-endpoints.md` with all new QC endpoints
- [X] T051 [P] Add audit logging for pass and fail actions in service layer
- [X] T052 [P] Run full integration test suite and fix failures — `cd be && ./mvnw test -pl .`
- [X] T053 [P] Verify build compiles clean — `cd be && ./mvnw compile -q`

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies — verify existing structure
- **Foundational (Phase 2)**: Depends on Setup — BLOCKS all user stories
- **User Story 1 (Phase 3)**: Depends on Foundational — core pass flow
- **User Story 2 (Phase 4)**: Depends on Foundational — core fail flow, can run parallel to US1 for [P] tasks but sequential tasks depend on same service
- **User Story 3 (Phase 5)**: Depends on US1 + US2 — partial inspection builds on pass/fail logic
- **User Story 4 (Phase 6)**: Depends on Foundational — lookup table management
- **Polish (Phase 7)**: Depends on all user stories

### Within Each Phase

- [P] tasks marked can run in parallel
- Sequential tasks must run in order

---

## Implementation Strategy

### MVP First (US1 Only)

1. Complete Phase 1: Setup
2. Complete Phase 2: Foundational
3. Complete Phase 3: User Story 1
4. **STOP and VALIDATE**: Build compiles, integration tests pass
5. Continue to US2 → US3 → US4 → Polish

### Validation Per Task

After each task:
1. Run `cd be && ./mvnw compile -q` to verify compilation
2. If test task: run specific test with `cd be && ./mvnw test -Dtest=TestClassName`
3. Fix any errors before proceeding to next task