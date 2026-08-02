---

description: "Task list for Complete Work Order"
---

# Tasks: Complete Work Order

**Input**: Design documents from `/specs/007-complete-work-order/`

**Prerequisites**: `plan.md`, `spec.md`, `research.md`, `data-model.md`, `contracts/complete-work-order.md`, `quickstart.md`

**Tests**: Required. Write every unit and integration test before the production implementation it covers, verify it fails first, then make it pass. The shared-state completion flow also requires an integration concurrency test.

**Organization**: Tasks are grouped by user story. All source paths are relative to the repository root.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel with other tasks that do not modify the same file.
- **[USn]**: User story association.

## Phase 1: Setup

**Purpose**: Prepare the completion feature's test and API documentation entry points.

- [X] T001 [P] Add the complete endpoint request/response shape to `docs/api-endpoints.md` from `specs/007-complete-work-order/contracts/complete-work-order.md`.
- [X] T002 [P] Create the completion test fixture helper for committed in-progress Work Orders, active runs, multi-lot reservations, output locations, and role users in `be/src/test/java/fpt/qn/mes/workorder/support/WorkOrderCompletionFixture.java`.

---

## Phase 2: Foundational

**Purpose**: Add schema and shared application contracts that block all completion user stories.

**⚠️ CRITICAL**: Complete this phase before implementation work in any user story.

- [X] T003 Create a Flyway migration that changes `quality_inspections.quantity` from strictly positive to non-negative in `be/src/main/resources/db/migration/V20260801130000__allow_zero_quantity_quality_inspections.sql`.
- [X] T004 Run jOOQ code generation and verify generated sources compile after `be/src/main/resources/db/migration/V20260801130000__allow_zero_quantity_quality_inspections.sql`.
- [X] T005 [P] Add `CompleteWorkOrderRequest` with required output destination, non-negative quantity validation, and note length validation in `be/src/main/java/fpt/qn/mes/workorder/application/dto/workorder/complete/CompleteWorkOrderRequest.java`.
- [X] T006 [P] Add Lombok completion value classes for active run, reservation allocation, output destination, and completion references in `be/src/main/java/fpt/qn/mes/workorder/application/port/out/ActiveProductionRun.java`, `be/src/main/java/fpt/qn/mes/workorder/application/port/out/CompletionReservationAllocation.java`, `be/src/main/java/fpt/qn/mes/workorder/application/port/out/CompletionOutputDestination.java`, and `be/src/main/java/fpt/qn/mes/workorder/application/port/out/CompletionReferences.java`.
- [X] T007 Extend `be/src/main/java/fpt/qn/mes/workorder/application/port/in/WorkOrderUseCase.java`, `be/src/main/java/fpt/qn/mes/workorder/application/port/out/ProductionRunPort.java`, and `be/src/main/java/fpt/qn/mes/workorder/domain/repository/WorkOrderRepository.java` with completion, active-run, and active-transition contracts.
- [X] T008 Implement active transition lookup in `be/src/main/java/fpt/qn/mes/workorder/infrastructure/persistence/adapter/WorkOrderPersistenceAdapter.java` and active-run lock/close/event-note support in `be/src/main/java/fpt/qn/mes/workorder/infrastructure/persistence/adapter/ProductionRunPersistenceAdapter.java`.
- [X] T009 Create `WorkOrderCompletionPort` and its jOOQ adapter skeleton for destination validation, lot-level reservation retrieval, output creation, and atomic finalization in `be/src/main/java/fpt/qn/mes/workorder/application/port/out/WorkOrderCompletionPort.java` and `be/src/main/java/fpt/qn/mes/workorder/infrastructure/persistence/adapter/WorkOrderCompletionPersistenceAdapter.java`.

**Checkpoint**: Schema supports zero-quantity inspections; shared request, port, transition, run, and persistence contracts compile.

---

## Phase 3: User Story 1 - Complete Production and Record Classified Output (Priority: P1) 🎯 MVP

**Goal**: An Operator can close an eligible production run and create independently traceable good and defective output in quality isolation.

**Independent Test**: Complete an in-progress Work Order with no outstanding raw-material reservations and verify the run closes, the machine becomes available, the Work Order becomes completed, and separate good/defect output lots, balances, inspections, events, and audit records exist.

### Tests for User Story 1

> **Write these FIRST and confirm they fail before implementation.**

- [X] T010 [P] [US1] Add service happy-path and reported-quantity validation tests in `be/src/test/java/fpt/qn/mes/workorder/application/service/WorkOrderServiceCompleteTest.java`.
- [X] T011 [P] [US1] Add controller contract test for the Operator complete request and success envelope in `be/src/test/java/fpt/qn/mes/workorder/presentation/WorkOrderControllerTest.java`.
- [X] T012 [P] [US1] Add HTTP-to-database happy-path tests for run closure, output classification, QC inspection creation, event note, and audit action in `be/src/test/java/fpt/qn/mes/workorder/integration/WorkOrderCompletionIntegrationTest.java`.

### Implementation for User Story 1

- [X] T013 [US1] Implement output destination validation, unique good/defect production lot generation, positive output movement creation, zero-quantity balances, and pending inspection creation in `be/src/main/java/fpt/qn/mes/workorder/infrastructure/persistence/adapter/WorkOrderCompletionPersistenceAdapter.java`.
- [X] T014 [US1] Implement `completeWorkOrder` quantity validation, active run close, `COMPLETE` event with note, Work Order completion transition, machine release, and `COMPLETE_PRODUCTION` audit orchestration in `be/src/main/java/fpt/qn/mes/workorder/application/service/WorkOrderService.java`.
- [X] T015 [US1] Add the Operator-only `POST /api/work-orders/{id}/complete` endpoint and success response in `be/src/main/java/fpt/qn/mes/workorder/presentation/WorkOrderController.java`.
- [X] T016 [US1] Update quality-inspection request/domain validation to accept zero inspection quantity in `be/src/main/java/fpt/qn/mes/quality/application/dto/inspection/create/CreateQualityInspectionRequest.java` and `be/src/main/java/fpt/qn/mes/quality/domain/entities/QualityInspection.java`.

**Checkpoint**: An Operator can complete a no-reservation Work Order and the result is independently verifiable through HTTP and persisted records.

---

## Phase 4: User Story 2 - Consume and Release Reserved Materials Accurately (Priority: P1)

**Goal**: Completion reconciles each reserved raw-material lot into normal consumption, material scrap, or release without double-deducting stock.

**Independent Test**: Complete an in-progress Work Order whose one material is reserved in two lots, then verify per-lot normal consume plus scrap equals consumed quantity, released stock returns to its original balance, and consumed plus released equals the original reservation.

### Tests for User Story 2

> **Write these FIRST and confirm they fail before implementation.**

- [X] T017 [P] [US2] Add unit tests for proportional capped consumption, per-allocation scrap split, actual-zero release, and actual-above-planned behavior in `be/src/test/java/fpt/qn/mes/workorder/application/service/WorkOrderServiceCompleteTest.java`.
- [X] T018 [P] [US2] Add integration tests that verify `CONSUME_IN_PRODUCTION`, `SCRAP`, and `RELEASE_RESERVATION` reconciliation for original reserved lots in `be/src/test/java/fpt/qn/mes/workorder/integration/WorkOrderCompletionIntegrationTest.java`.

### Implementation for User Story 2

- [X] T019 [US2] Query original reservation allocations net of prior release by warehouse, location, material product, and lot in `be/src/main/java/fpt/qn/mes/workorder/infrastructure/persistence/adapter/WorkOrderCompletionPersistenceAdapter.java`.
- [X] T020 [US2] Implement locked per-lot reserved-balance deduction, normal-consumption and scrap movement creation, release of the remaining reservation, and `work_order_materials` quantity updates in `be/src/main/java/fpt/qn/mes/workorder/infrastructure/persistence/adapter/WorkOrderCompletionPersistenceAdapter.java`.
- [X] T021 [US2] Implement capped proportional consumption and scrap-allocation commands in `be/src/main/java/fpt/qn/mes/workorder/application/service/WorkOrderService.java`.
- [X] T022 [US2] Extend release accounting to exclude completed consumption and scrap movements from any future net-reservation calculation in `be/src/main/java/fpt/qn/mes/workorder/infrastructure/persistence/adapter/WorkOrderReservationPersistenceAdapter.java`.

**Checkpoint**: A completion with reserved raw material fully reconciles every original lot and cannot create a negative or double-deducted balance.

---

## Phase 5: User Story 3 - Protect Completion Authorization and Integrity (Priority: P1)

**Goal**: Only Operators can finalize once, and invalid, failing, or competing requests leave operational data consistent.

**Independent Test**: Submit requests as unauthenticated, non-Operator, invalid-state, inactive-transition, missing-run, and concurrent Operators; verify authorization/lifecycle behavior and exactly one winner for concurrent completion.

### Tests for User Story 3

> **Write these FIRST and confirm they fail before implementation.**

- [X] T023 [P] [US3] Add service tests for missing Work Order, invalid state, inactive transition, missing active run, invalid destination, and persistence-failure propagation in `be/src/test/java/fpt/qn/mes/workorder/application/service/WorkOrderServiceCompleteTest.java`.
- [X] T024 [P] [US3] Add HTTP authorization and invalid-input/state integration tests with database no-change assertions in `be/src/test/java/fpt/qn/mes/workorder/integration/WorkOrderCompletionIntegrationTest.java`.
- [X] T025 [P] [US3] Add a non-transactional two-Operator concurrent-completion integration test with committed fixture data in `be/src/test/java/fpt/qn/mes/workorder/integration/WorkOrderCompletionIntegrationTest.java`.

### Implementation for User Story 3

- [X] T026 [US3] Lock the Work Order, active production run, and reserved balances in deterministic order and make run closure conditional on an unclosed run in `be/src/main/java/fpt/qn/mes/workorder/infrastructure/persistence/adapter/WorkOrderCompletionPersistenceAdapter.java` and `be/src/main/java/fpt/qn/mes/workorder/infrastructure/persistence/adapter/ProductionRunPersistenceAdapter.java`.
- [X] T027 [US3] Enforce active configured `IN_PROGRESS -> COMPLETED` transition, reject invalid completion state/run/destination failures with module exceptions, and ensure final state changes occur only after persistence commands succeed in `be/src/main/java/fpt/qn/mes/workorder/application/service/WorkOrderService.java` and `be/src/main/java/fpt/qn/mes/workorder/application/exception/WorkOrderExceptions.java`.
- [X] T028 [US3] Add explicit `COMPLETE_PRODUCTION` audit payload support in `be/src/main/java/fpt/qn/mes/workorder/application/port/out/AuditLogPort.java` and `be/src/main/java/fpt/qn/mes/workorder/infrastructure/persistence/adapter/AuditLogPersistenceAdapter.java`.

**Checkpoint**: Authorization, lifecycle controls, atomic failure behavior, and concurrent completion integrity are all verified through integration tests.

---

## Phase 6: Polish and Cross-Cutting Validation

**Purpose**: Verify generated code, documentation, and the full test suite.

- [X] T029 [P] Regenerate jOOQ sources and run formatting/compilation checks from `be/pom.xml` after the final migration and adapter changes.
- [X] T030 [P] Review `docs/api-endpoints.md` and `specs/007-complete-work-order/contracts/complete-work-order.md` against `be/src/main/java/fpt/qn/mes/workorder/presentation/WorkOrderController.java` for contract consistency.
- [X] T031 Run the focused and full backend test suite described in `specs/007-complete-work-order/quickstart.md` and resolve all failures.

---

## Dependencies and Execution Order

### Phase Dependencies

- Phase 1 has no dependencies.
- Phase 2 depends on Phase 1 and blocks all user stories.
- US1 depends on Phase 2.
- US2 depends on US1's completion orchestration and extends it with material accounting.
- US3 depends on the completion path from US1 and the persistence behavior from US2.
- Phase 6 depends on all selected user stories.

### User Story Dependencies

- **US1**: MVP for classified completion with no outstanding material reservations.
- **US2**: Adds raw-material consumption, scrap allocation, and release to the US1 completion path.
- **US3**: Hardens the completed workflow with error-path and concurrent-request protection.

### Parallel Opportunities

- T001 and T002 can proceed in parallel.
- T005 and T006 can proceed in parallel after T003.
- T010, T011, and T012 can proceed in parallel after Phase 2.
- T017 and T018 can proceed in parallel after US1.
- T023, T024, and T025 can proceed in parallel after US2.
- T029 and T030 can proceed in parallel before the final test run.

## Parallel Example: User Story 1

```text
Task: T010 Write service happy-path and validation tests.
Task: T011 Write controller contract test.
Task: T012 Write integration happy-path test.
```

## Implementation Strategy

### MVP First

1. Complete setup and foundational contracts.
2. Complete US1 with an in-progress Work Order that has no outstanding material reservations.
3. Verify output classification, run closure, status, event, machine, and audit through HTTP.

### Incremental Delivery

1. Add US2 to reconcile multi-lot material consumption, scrap, and release.
2. Add US3 to prove authorization, rollback behavior, and concurrent completion safety.
3. Run final generated-code, contract, and full-suite validation.

## Notes

- All tasks use the mandatory checklist format and exact file paths.
- Tests must fail before the implementation task that makes them pass.
- Do not create a reverse `workorder -> quality` service dependency; use the workorder-owned completion port.
