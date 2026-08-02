# Tasks: Release and Cancel Work Orders

**Input**: Design documents from `/specs/005-release-cancel-work-orders/`

**Prerequisites**: plan.md (required), spec.md (required), data-model.md, contracts/api.md, quickstart.md

**Tests**: Required for every user story per `docs/rules/backend-testing.md`.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: User story label (US1, US2, US3)
- File paths are explicitly specified for every task.

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Project initialization and basic structure

- [x] T001 Verify project structure and imports for workorder module

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core interfaces and port contracts that MUST be complete before user stories

- [x] T002 Update `WorkOrderUseCase` input port interface with `releaseMaterials` and `cancelWorkOrder` method signatures in `be/src/main/java/fpt/qn/mes/workorder/application/port/in/WorkOrderUseCase.java`
- [x] T003 [P] Update `WorkOrderReservationPort` output port interface with `releaseReservation` method signature in `be/src/main/java/fpt/qn/mes/workorder/application/port/out/WorkOrderReservationPort.java`

**Checkpoint**: Foundation ready — user story implementation can begin in parallel.

---

## Phase 3: User Story 1 - Release Reserved Materials (Priority: P1) 🎯 MVP

**Goal**: Allow planners to release reserved materials back to available stock status and record RELEASE_RESERVATION stock movements.

**Independent Test**: Execute `POST /api/work-orders/{id}/release-materials` on a `READY_TO_PRODUCE` Work Order, verify `RESERVED` stock decreases, `AVAILABLE` stock increases, and `RELEASE_RESERVATION` stock movement log is created.

### Tests for User Story 1 (REQUIRED)

- [x] T004 [P] [US1] Unit test: `WorkOrderServiceReleaseMaterialsTest` happy path + invalid status paths in `be/src/test/java/fpt/qn/mes/workorder/application/service/WorkOrderServiceReleaseAndCancelTest.java`
- [x] T005 [P] [US1] Integration test: `ReleaseMaterialsIntegrationTest` HTTP 200 + DB stock movements verify in `be/src/test/java/fpt/qn/mes/workorder/application/service/WorkOrderServiceReleaseAndCancelTest.java`

### Implementation for User Story 1

- [x] T006 [US1] Implement `releaseReservation` in `be/src/main/java/fpt/qn/mes/workorder/infrastructure/persistence/adapter/WorkOrderReservationPersistenceAdapter.java`
- [x] T007 [US1] Implement `releaseMaterials` service logic in `be/src/main/java/fpt/qn/mes/workorder/application/service/WorkOrderService.java`
- [x] T008 [US1] Add `POST /{id}/release-materials` REST endpoint in `be/src/main/java/fpt/qn/mes/workorder/presentation/WorkOrderController.java`

**Checkpoint**: User Story 1 fully functional and testable independently.

---

## Phase 4: User Story 2 - Cancel Work Order and Auto-Release Materials (Priority: P1)

**Goal**: Allow planners to cancel an unstarted Work Order and automatically release any reserved materials.

**Independent Test**: Execute `POST /api/work-orders/{id}/cancel` on a `READY_TO_PRODUCE` Work Order, verify status changes to `CANCELLED` and materials are released.

### Tests for User Story 2 (REQUIRED)

- [x] T009 [P] [US2] Unit test: `WorkOrderServiceCancelTest` happy path + in-progress block in `be/src/test/java/fpt/qn/mes/workorder/application/service/WorkOrderServiceReleaseAndCancelTest.java`
- [x] T010 [P] [US2] Integration test: `CancelWorkOrderIntegrationTest` HTTP 200 + status CANCELLED verify in `be/src/test/java/fpt/qn/mes/workorder/application/service/WorkOrderServiceReleaseAndCancelTest.java`

### Implementation for User Story 2

- [x] T011 [US2] Implement `cancelWorkOrder` service logic in `be/src/main/java/fpt/qn/mes/workorder/application/service/WorkOrderService.java`
- [x] T012 [US2] Add `POST /{id}/cancel` REST endpoint in `be/src/main/java/fpt/qn/mes/workorder/presentation/WorkOrderController.java`

**Checkpoint**: User Stories 1 AND 2 both work independently.

---

## Phase 5: User Story 3 - Role-Based Access and Concurrency Protection (Priority: P2)

**Goal**: Enforce RBAC security (`@PreAuthorize`) and pessimistic database locking (`SELECT FOR UPDATE`) under concurrent requests.

**Independent Test**: Run security role tests and concurrent execution tests on release and cancel endpoints.

### Tests for User Story 3 (REQUIRED)

- [x] T013 [P] [US3] Unit test: `WorkOrderSecurityAndLockingTest` 403 Forbidden + concurrent locking verification in `be/src/test/java/fpt/qn/mes/workorder/application/service/WorkOrderServiceReleaseAndCancelTest.java`

### Implementation for User Story 3

- [x] T014 [US3] Verify `@PreAuthorize("hasAnyRole('ADMIN', 'PLANNER')")` and pessimistic locking in `be/src/main/java/fpt/qn/mes/workorder/presentation/WorkOrderController.java` and `be/src/main/java/fpt/qn/mes/workorder/infrastructure/persistence/adapter/WorkOrderPersistenceAdapter.java`

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Verification and documentation

- [x] T015 [P] Run integration tests and execute validation steps in `specs/005-release-cancel-work-orders/quickstart.md`

---

## Dependencies & Execution Order

### Phase Dependencies
- **Setup (Phase 1)** → **Foundational (Phase 2)** → **User Stories (Phase 3+)** → **Polish (Phase 6)**

### User Story Breakdown
- **User Story 1 (MVP)**: Tasks T004 - T008 (Release Materials)
- **User Story 2**: Tasks T009 - T012 (Cancel Work Order)
- **User Story 3**: Tasks T013 - T014 (Role Security & Locking)
