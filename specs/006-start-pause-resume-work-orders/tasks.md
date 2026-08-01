# Tasks: Start, Pause, and Resume Work Orders

**Input**: Design documents from `/specs/006-start-pause-resume-work-orders/`

**Prerequisites**: plan.md (required), spec.md (required), data-model.md, contracts/api.md, quickstart.md

**Tests**: Required for every user story per `docs/rules/backend-testing.md`.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: User story label (US1, US2, US3)
- File paths are explicitly specified for every task.

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Project initialization and DTO structure

- [x] T001 Verify project structure and DTOs for start/pause/resume endpoints

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core DTOs and port contracts that MUST be complete before user stories

- [x] T002 Create `StartWorkOrderRequest.java` DTO in `be/src/main/java/fpt/qn/mes/workorder/application/dto/request/StartWorkOrderRequest.java`
- [x] T003 [P] Update `WorkOrderUseCase` input port interface with `startWorkOrder`, `pauseWorkOrder`, `resumeWorkOrder` method signatures in `be/src/main/java/fpt/qn/mes/workorder/application/port/in/WorkOrderUseCase.java`
- [x] T004 [P] Create `ProductionRunPort` output port interface in `be/src/main/java/fpt/qn/mes/workorder/application/port/out/ProductionRunPort.java`

**Checkpoint**: Foundation ready — user story implementation can begin in parallel.

---

## Phase 3: User Story 1 - Start Production Run (Priority: P1) 🎯 MVP

**Goal**: Allow operators to start production on a READY_TO_PRODUCE Work Order, updating Machine status to RUNNING, creating a production_runs record, and logging a START event.

**Independent Test**: Execute `POST /api/work-orders/{id}/start` with valid `machineId`, verify Work Order status is `IN_PROGRESS`, Machine is `RUNNING`, and a `production_runs` entry is created.

### Tests for User Story 1 (REQUIRED)

- [x] T005 [P] [US1] Unit test: `WorkOrderServiceStartTest` happy path + machine unavailable validation in `be/src/test/java/fpt/qn/mes/workorder/application/service/WorkOrderServiceStartPauseResumeTest.java`
- [x] T006 [P] [US1] Integration test: `StartWorkOrderIntegrationTest` HTTP 200 + production_runs DB verify in `be/src/test/java/fpt/qn/mes/workorder/application/service/WorkOrderServiceStartPauseResumeTest.java`

### Implementation for User Story 1

- [x] T007 [US1] Implement `ProductionRunPersistenceAdapter` in `be/src/main/java/fpt/qn/mes/workorder/infrastructure/persistence/adapter/ProductionRunPersistenceAdapter.java`
- [x] T008 [US1] Implement `startWorkOrder` service logic in `be/src/main/java/fpt/qn/mes/workorder/application/service/WorkOrderService.java`
- [x] T009 [US1] Add `POST /{id}/start` REST endpoint in `be/src/main/java/fpt/qn/mes/workorder/presentation/WorkOrderController.java`

**Checkpoint**: User Story 1 fully functional and testable independently.

---

## Phase 4: User Story 2 - Pause and Resume Production (Priority: P1)

**Goal**: Allow operators to pause an IN_PROGRESS order (transition to PAUSED) and resume a PAUSED order (transition back to IN_PROGRESS), logging PAUSE and RESUME events.

**Independent Test**: Execute `POST /api/work-orders/{id}/pause` (verify PAUSED state) and then `POST /api/work-orders/{id}/resume` (verify IN_PROGRESS state).

### Tests for User Story 2 (REQUIRED)

- [x] T010 [P] [US2] Unit test: `WorkOrderServicePauseResumeTest` happy path + invalid status paths in `be/src/test/java/fpt/qn/mes/workorder/application/service/WorkOrderServiceStartPauseResumeTest.java`
- [x] T011 [P] [US2] Integration test: `PauseResumeIntegrationTest` HTTP 200 + event logs verify in `be/src/test/java/fpt/qn/mes/workorder/application/service/WorkOrderServiceStartPauseResumeTest.java`

### Implementation for User Story 2

- [x] T012 [US2] Implement `pauseWorkOrder` and `resumeWorkOrder` service logic in `be/src/main/java/fpt/qn/mes/workorder/application/service/WorkOrderService.java`
- [x] T013 [US2] Add `POST /{id}/pause` and `POST /{id}/resume` REST endpoints in `be/src/main/java/fpt/qn/mes/workorder/presentation/WorkOrderController.java`

**Checkpoint**: User Stories 1 AND 2 both work independently.

---

## Phase 5: User Story 3 - Machine Concurrency Safeguards & Access Control (Priority: P2)

**Goal**: Enforce that 1 machine cannot run 2 Work Orders concurrently, and restrict endpoints to OPERATOR, PLANNER, ADMIN.

**Independent Test**: Attempt to start a second Work Order on a RUNNING machine (expect HTTP 400 rejection).

### Tests for User Story 3 (REQUIRED)

- [x] T014 [P] [US3] Unit test: `MachineConcurrencyTest` multi-workorder on same machine check in `be/src/test/java/fpt/qn/mes/workorder/application/service/WorkOrderServiceStartPauseResumeTest.java`

### Implementation for User Story 3

- [x] T015 [US3] Add machine concurrency check and `@PreAuthorize("hasAnyRole('ADMIN', 'PLANNER', 'OPERATOR')")` in `be/src/main/java/fpt/qn/mes/workorder/presentation/WorkOrderController.java` and `be/src/main/java/fpt/qn/mes/workorder/application/service/WorkOrderService.java`

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Verification and documentation

- [x] T016 [P] Execute validation scenarios in `specs/006-start-pause-resume-work-orders/quickstart.md`
