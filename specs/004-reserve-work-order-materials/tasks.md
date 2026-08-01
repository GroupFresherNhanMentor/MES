---

description: "Implementation tasks for the Reserve Work Order Materials endpoint"
---

# Tasks: Reserve Work Order Materials

**Input**: Design documents from `/specs/004-reserve-work-order-materials/`

**Prerequisites**: `plan.md`, `spec.md`, `research.md`, `data-model.md`, `contracts/reserve-work-order-materials-api.json`, `quickstart.md`

**Tests**: Required for every user story. Follow `docs/rules/backend-testing.md`; tests must be written first and include unit, HTTP-to-DB integration, authorization, and concurrency coverage where applicable.

**Implementation target**: Java 25, Spring Boot 4.1.0, jOOQ 3.21, PostgreSQL 18, MapStruct 1.6.3, JUnit 5, Mockito, and Testcontainers.

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Confirm the feature contract and prepare configuration/documentation touchpoints.

- [X] T001 Verify the implementation scope and acceptance rules against `specs/004-reserve-work-order-materials/spec.md`, `specs/004-reserve-work-order-materials/plan.md`, and `specs/004-reserve-work-order-materials/contracts/reserve-work-order-materials-api.json`.
<<<<<<< HEAD
- [X] T002 [P] Add the configurable raw-material warehouse code with default `RAW_MATERIAL_WAREHOUSE` under `app` in `be/src/main/resources/application.yaml`.
- [X] T003 [P] Reconcile the versioned endpoint, Planner role, request body, response message, and reservation rules in `docs/api-endpoints.md` with `specs/004-reserve-work-order-materials/contracts/reserve-work-order-materials-api.json`.
=======
- [X] T002 [P] Remove the obsolete configured raw-material warehouse setting from `be/src/main/resources/application.yaml`; reservation selects stock from all active warehouses.
- [X] T003 [P] Reconcile the Work Order action endpoint, Planner role, empty request body, response message, and active-warehouse FIFO rules in `docs/api-endpoints.md` with `specs/004-reserve-work-order-materials/contracts/reserve-work-order-materials-api.json`.
>>>>>>> origin/develop

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Build shared contracts and persistence boundaries required by all user stories. No user-story implementation starts until this phase is complete.

- [X] T004 [P] Add `INSUFFICIENT_STOCK` to `be/src/main/java/fpt/qn/mes/common/exception/ErrorCode.java` and extend `be/src/main/java/fpt/qn/mes/common/exception/AppException.java` plus `be/src/main/java/fpt/qn/mes/common/exception/GlobalExceptionHandler.java` so structured shortage details are returned in the standard error envelope.
<<<<<<< HEAD
- [X] T005 [P] Add `findByCode(String code)` to `be/src/main/java/fpt/qn/mes/master/warehouse/domain/repository/WarehouseRepository.java`, expose the lookup through `be/src/main/java/fpt/qn/mes/master/warehouse/application/port/in/WarehouseUseCase.java`, and implement the jOOQ query in `be/src/main/java/fpt/qn/mes/master/warehouse/infrastructure/persistence/WarehousePersistenceAdapter.java`.
- [X] T006 [P] Add a reservation-safe machine availability lookup/lock contract to `be/src/main/java/fpt/qn/mes/master/machine/domain/repository/MachineRepository.java` and `be/src/main/java/fpt/qn/mes/master/machine/application/port/in/MachineUseCase.java`, then implement the `SELECT ... FOR UPDATE` status check in `be/src/main/java/fpt/qn/mes/master/machine/infrastructure/persistence/MachinePersistenceAdapter.java`.
- [X] T007 [P] Add `MATERIAL_SHORTAGE` to `be/src/main/java/fpt/qn/mes/workorder/domain/constants/WorkOrderStatusConstants.java` and add `findForUpdate(UUID id)` plus the required status transition lookup contract to `be/src/main/java/fpt/qn/mes/workorder/domain/repository/WorkOrderRepository.java` and `be/src/main/java/fpt/qn/mes/workorder/infrastructure/persistence/WorkOrderPersistenceAdapter.java`.
- [X] T008 [P] Create the validated `ReserveWorkOrderMaterialsRequest` and `ReserveWorkOrderMaterialsResponse` DTOs in `be/src/main/java/fpt/qn/mes/workorder/application/dto/reservation/reserve/`, with only required `machineId` in the request and `workOrderId`/`status` in the success response.
- [X] T009 [P] Add `reserveMaterials(UUID workOrderId, ReserveWorkOrderMaterialsRequest request)` to `be/src/main/java/fpt/qn/mes/workorder/application/port/in/WorkOrderUseCase.java` and define the reservation and audit output ports in `be/src/main/java/fpt/qn/mes/workorder/application/port/out/WorkOrderReservationPort.java` and `be/src/main/java/fpt/qn/mes/workorder/application/port/out/AuditLogPort.java`.
- [X] T010 [P] Extend `workOrderId` through `be/src/main/java/fpt/qn/mes/inventory/domain/entities/StockMovement.java`, `be/src/main/java/fpt/qn/mes/inventory/infrastructure/persistence/InventoryRecordMapper.java`, and `be/src/main/java/fpt/qn/mes/inventory/infrastructure/persistence/StockMovementPersistenceAdapter.java` so reservation movements are linked to the Work Order.
- [X] T011 Create the reservation persistence and audit adapters in `be/src/main/java/fpt/qn/mes/workorder/infrastructure/persistence/WorkOrderReservationPersistenceAdapter.java` and `be/src/main/java/fpt/qn/mes/workorder/infrastructure/persistence/AuditLogPersistenceAdapter.java`, using generated jOOQ tables and no raw SQL strings.
- [X] T012 [P] Add module-scoped reservation exceptions and shortage detail types under `be/src/main/java/fpt/qn/mes/workorder/application/exception/`, covering invalid Work Order state, unavailable machine, unresolved warehouse configuration, and insufficient material details.
=======
- [X] T005 [P] Keep warehouse selection within the reservation persistence query, filtering to `ACTIVE` warehouses without a feature-specific warehouse lookup.
- [X] T006 [P] Exclude machine assignment and availability checks from material reservation; production start owns those validations.
- [X] T007 [P] Add `MATERIAL_SHORTAGE` to `be/src/main/java/fpt/qn/mes/workorder/domain/constants/WorkOrderStatusConstants.java` and add `findForUpdate(UUID id)` plus the required status transition lookup contract to `be/src/main/java/fpt/qn/mes/workorder/domain/repository/WorkOrderRepository.java` and `be/src/main/java/fpt/qn/mes/workorder/infrastructure/persistence/WorkOrderPersistenceAdapter.java`.
- [X] T008 [P] Create `ReserveWorkOrderMaterialsResponse` in `be/src/main/java/fpt/qn/mes/workorder/application/dto/response/` with `workOrderId` and `status`; the action has no request body.
- [X] T009 [P] Add `reserveMaterials(UUID workOrderId)` to `be/src/main/java/fpt/qn/mes/workorder/application/port/in/WorkOrderUseCase.java` and define reservation and audit output ports in `be/src/main/java/fpt/qn/mes/workorder/application/port/out/`.
- [X] T010 [P] Extend `workOrderId` through `be/src/main/java/fpt/qn/mes/inventory/domain/entities/StockMovement.java`, `be/src/main/java/fpt/qn/mes/inventory/infrastructure/persistence/InventoryRecordMapper.java`, and `be/src/main/java/fpt/qn/mes/inventory/infrastructure/persistence/StockMovementPersistenceAdapter.java` so reservation movements are linked to the Work Order.
- [X] T011 Create reservation and audit adapters in `be/src/main/java/fpt/qn/mes/workorder/infrastructure/persistence/adapter/`, using generated jOOQ tables and no raw SQL strings.
- [X] T012 [P] Add module-scoped reservation exceptions and shortage detail types under `be/src/main/java/fpt/qn/mes/workorder/application/exception/`, covering invalid Work Order state and insufficient material details.
>>>>>>> origin/develop
- [X] T013 Add the shared reservation test fixtures and database assertion helpers in `be/src/test/java/fpt/qn/mes/workorder/integration/WorkOrderIntegrationTest.java` without adding a separate concurrency test file.

**Checkpoint**: Shared contracts, lookup boundaries, error envelope, traceability mapping, and test fixtures are ready for user-story implementation.

---

## Phase 3: User Story 1 - Reserve Materials for Production (Priority: P1) 🎯 MVP

<<<<<<< HEAD
**Goal**: Let a Planner reserve every required material from `RAW_MATERIAL_WAREHOUSE` using FIFO allocation and move a valid Work Order to `READY_TO_PRODUCE` only when its designated machine is `AVAILABLE`.

**Independent Test**: With a `PLANNED` Work Order, sufficient `AVAILABLE` stock in the configured warehouse, and an `AVAILABLE` machine, call the versioned endpoint and verify HTTP 200, all stock/material updates, FIFO movement rows, and `READY_TO_PRODUCE` response.
=======
**Goal**: Let a Planner reserve every required material from active warehouses using FIFO allocation and move a valid Work Order to `READY_TO_PRODUCE` when all materials are available.

**Independent Test**: With a `PLANNED` Work Order and sufficient `AVAILABLE` stock split across active warehouses, call the action endpoint and verify HTTP 200, all stock/material updates, FIFO movement rows, and `READY_TO_PRODUCE` response.
>>>>>>> origin/develop

### Tests for User Story 1 (REQUIRED)

> Write these tests first and verify they fail before implementation.

<<<<<<< HEAD
- [X] T014 [P] [US1] Add service unit tests for successful reservation, required-quantity calculation, machine availability, status response, and multi-lot FIFO allocation in `be/src/test/java/fpt/qn/mes/workorder/application/service/WorkOrderServiceTest.java`.
- [X] T015 [P] [US1] Add controller contract tests for valid `machineId`, HTTP 200 response envelope, exact success message, and rejection of an unexpected `sourceWarehouseId` field in `be/src/test/java/fpt/qn/mes/workorder/presentation/WorkOrderControllerTest.java`.
=======
- [X] T014 [P] [US1] Add service unit tests for successful reservation, remaining-quantity calculation, status response, multi-lot FIFO allocation, and active multi-warehouse allocation in `be/src/test/java/fpt/qn/mes/workorder/application/service/ReserveWorkOrderMaterialsServiceTest.java`.
- [X] T015 [P] [US1] Add controller contract tests for the empty-body action, HTTP 200 response envelope, and exact success message in `be/src/test/java/fpt/qn/mes/workorder/presentation/WorkOrderControllerTest.java`.
>>>>>>> origin/develop
- [X] T016 [P] [US1] Add HTTP-to-DB integration tests for successful reservation, `AVAILABLE` to `RESERVED` quantities, Work Order material updates, FIFO lot selection, and `RESERVE` movements in `be/src/test/java/fpt/qn/mes/workorder/integration/WorkOrderIntegrationTest.java`.

### Implementation for User Story 1

<<<<<<< HEAD
- [X] T017 [US1] Implement one bounded jOOQ query that selects all eligible `AVAILABLE` stock balances for the Work Order material product IDs within the configured warehouse, joins `stock_lots`, orders FIFO by `created_at` with deterministic UUID/location tie-breakers, and applies `FOR UPDATE` in `be/src/main/java/fpt/qn/mes/workorder/infrastructure/persistence/WorkOrderReservationPersistenceAdapter.java`.
- [X] T018 [US1] Implement locked stock allocation writes that decrement source `AVAILABLE` balances, increment or insert same-location `RESERVED` balances, update `work_order_materials.reserved_quantity`, and insert one linked `RESERVE` movement per lot in `be/src/main/java/fpt/qn/mes/workorder/infrastructure/persistence/WorkOrderReservationPersistenceAdapter.java`.
- [X] T019 [US1] Implement the successful reservation orchestration with `@Transactional`, Work Order row locking, warehouse-by-code resolution, machine availability validation, FIFO allocation validation, status transition, response creation, and delegation to reservation/audit ports in `be/src/main/java/fpt/qn/mes/workorder/application/service/WorkOrderService.java`.
- [X] T020 [US1] Add the Planner-only versioned action mapping `POST /api/v1/work-orders/{id}/reserve-materials` with `@Valid` request handling and `ResponseEntity<ApiResponse<ReserveWorkOrderMaterialsResponse>>` delegation in `be/src/main/java/fpt/qn/mes/workorder/presentation/ReserveWorkOrderMaterialsController.java`.
- [X] T021 [US1] Add the `RESERVE_MATERIAL` success transition audit insert with actor ID, Work Order ID, old status, new status, and immutable timestamp in `be/src/main/java/fpt/qn/mes/workorder/infrastructure/persistence/AuditLogPersistenceAdapter.java`.
- [X] T022 [US1] Run the focused unit, controller, and integration tests from `be` and fix all success-path failures before marking User Story 1 complete in `specs/004-reserve-work-order-materials/quickstart.md`.

**Checkpoint**: A fully stocked planned Work Order can be reserved independently and returns `READY_TO_PRODUCE` without using stock outside the configured warehouse.
=======
- [X] T017 [US1] Implement one bounded jOOQ query that selects all eligible `AVAILABLE` stock balances for the Work Order material product IDs in active warehouses, joins `stock_lots`, orders FIFO by `created_at` with deterministic UUID/location tie-breakers, and applies `FOR UPDATE` in `be/src/main/java/fpt/qn/mes/workorder/infrastructure/persistence/adapter/WorkOrderReservationPersistenceAdapter.java`.
- [X] T018 [US1] Implement locked stock allocation writes that decrement source `AVAILABLE` balances, increment or insert same-location `RESERVED` balances, update `work_order_materials.reserved_quantity`, and insert one linked `RESERVE` movement per lot in `be/src/main/java/fpt/qn/mes/workorder/infrastructure/persistence/WorkOrderReservationPersistenceAdapter.java`.
- [X] T019 [US1] Implement successful reservation orchestration with `@Transactional`, Work Order row locking, FIFO allocation validation across active warehouses, status transition, response creation, and delegation to reservation/audit ports in `be/src/main/java/fpt/qn/mes/workorder/application/service/WorkOrderService.java`.
- [X] T020 [US1] Add the Planner-only action mapping `POST /api/work-orders/{id}/reserve-materials` with `ResponseEntity<ApiResponse<ReserveWorkOrderMaterialsResponse>>` delegation in `be/src/main/java/fpt/qn/mes/workorder/presentation/WorkOrderController.java`.
- [X] T021 [US1] Add the `RESERVE_MATERIAL` success transition audit insert with actor ID, Work Order ID, old status, new status, and immutable timestamp in `be/src/main/java/fpt/qn/mes/workorder/infrastructure/persistence/AuditLogPersistenceAdapter.java`.
- [X] T022 [US1] Run the focused unit, controller, and integration tests from `be` and fix all success-path failures before marking User Story 1 complete in `specs/004-reserve-work-order-materials/quickstart.md`.

**Checkpoint**: A fully stocked planned Work Order can be reserved independently and returns `READY_TO_PRODUCE` using FIFO stock from active warehouses only.
>>>>>>> origin/develop

---

## Phase 4: User Story 2 - Prevent Partial Reservation on Shortage (Priority: P1)

**Goal**: Report every material shortage, persist `MATERIAL_SHORTAGE`, and guarantee that a failed reservation does not partially mutate stock, Work Order materials, or reservation movements.

**Independent Test**: Use a Work Order with one insufficient material and one sufficient material, plus a replenishment/retry case, and verify the structured error, unchanged quantities, shortage status, and later successful retry.

### Tests for User Story 2 (REQUIRED)

> Write these tests first and verify they fail before implementation.

<<<<<<< HEAD
- [X] T023 [P] [US2] Add service unit tests for insufficient material aggregation, structured shortage details, invalid Work Order statuses, unavailable machines, missing machine IDs, unresolved warehouse configuration, and retry from `MATERIAL_SHORTAGE` in `be/src/test/java/fpt/qn/mes/workorder/application/service/WorkOrderServiceTest.java`.
- [ ] T024 [P] [US2] Add HTTP-to-DB integration tests proving all-or-nothing shortage behavior, `MATERIAL_SHORTAGE` persistence, `INSUFFICIENT_STOCK` response details, warehouse isolation, retry success, and invalid-state rejection in `be/src/test/java/fpt/qn/mes/workorder/integration/WorkOrderIntegrationTest.java`.
=======
- [X] T023 [P] [US2] Add service unit tests for insufficient material aggregation, structured shortage details, invalid Work Order statuses, and retry from `MATERIAL_SHORTAGE` in `be/src/test/java/fpt/qn/mes/workorder/application/service/ReserveWorkOrderMaterialsServiceTest.java`.
- [X] T024 [P] [US2] Add HTTP-to-DB integration tests proving all-or-nothing shortage behavior, `MATERIAL_SHORTAGE` persistence, `INSUFFICIENT_STOCK` response details, active/inactive warehouse handling, retry success, and invalid-state rejection in `be/src/test/java/fpt/qn/mes/workorder/integration/WorkOrderIntegrationTest.java`.
>>>>>>> origin/develop

### Implementation for User Story 2

- [X] T025 [US2] Implement complete pre-validation and per-material shortage aggregation before any stock write, including `requiredQuantity - reservedQuantity` and FIFO available totals, in `be/src/main/java/fpt/qn/mes/workorder/application/service/WorkOrderService.java`.
- [X] T026 [US2] Implement the expected shortage transaction path that updates only the Work Order to `MATERIAL_SHORTAGE`, writes its audit record, and returns `INSUFFICIENT_STOCK` details without rolling back the shortage status in `be/src/main/java/fpt/qn/mes/workorder/application/service/WorkOrderService.java` and `be/src/main/java/fpt/qn/mes/workorder/application/exception/InsufficientMaterialException.java`.
<<<<<<< HEAD
- [X] T027 [US2] Enforce warehouse scoping and `AVAILABLE` status filtering in the locked allocation query, and verify the configured warehouse lookup failure maps to a clear application error in `be/src/main/java/fpt/qn/mes/workorder/infrastructure/persistence/WorkOrderReservationPersistenceAdapter.java` and `be/src/main/java/fpt/qn/mes/master/warehouse/infrastructure/persistence/WarehousePersistenceAdapter.java`.
- [X] T028 [US2] Enforce valid source statuses (`PLANNED` and `MATERIAL_SHORTAGE`) and machine status `AVAILABLE` before any stock mutation, returning the documented error codes from `be/src/main/java/fpt/qn/mes/workorder/application/service/WorkOrderService.java`.
- [ ] T029 [US2] Run the shortage, retry, warehouse-isolation, and invalid-state tests and update `specs/004-reserve-work-order-materials/quickstart.md` if observed response/error details differ from the contract.
=======
- [X] T027 [US2] Enforce active-warehouse scoping and `AVAILABLE` status filtering in the locked allocation query in `be/src/main/java/fpt/qn/mes/workorder/infrastructure/persistence/adapter/WorkOrderReservationPersistenceAdapter.java`.
- [X] T028 [US2] Enforce valid source statuses (`PLANNED` and `MATERIAL_SHORTAGE`) before any stock mutation, returning the documented error codes from `be/src/main/java/fpt/qn/mes/workorder/application/service/WorkOrderService.java`.
- [X] T029 [US2] Run the shortage, retry, active-warehouse, and invalid-state tests and update `specs/004-reserve-work-order-materials/quickstart.md` to match the contract.
>>>>>>> origin/develop

**Checkpoint**: Shortage requests are safe to retry, never partially reserve material, and clearly identify the reason for failure.

---

## Phase 5: User Story 3 - Preserve Reservation Integrity and Traceability (Priority: P1)

**Goal**: Protect shared stock and Work Order state under concurrent requests, enforce Planner authorization, and provide auditable status/movement traceability.

**Independent Test**: Run concurrent reservations against shared stock and verify exact success/failure counts, non-negative quantities, one movement per allocation, status-transition audit records, and 401/403 behavior.

### Tests for User Story 3 (REQUIRED)

> Write these tests first and verify they fail before implementation.

- [ ] T030 [P] [US3] Add unit tests for audit port invocation on success and shortage, Work Order lock ordering, duplicate reservation rejection, and linked `workOrderId` movement creation in `be/src/test/java/fpt/qn/mes/workorder/application/service/WorkOrderServiceTest.java`.
- [X] T031 [P] [US3] Add HTTP authorization tests for unauthenticated, non-Planner, and Planner callers and verify no state mutation on denied requests in `be/src/test/java/fpt/qn/mes/workorder/presentation/WorkOrderControllerTest.java`.
<<<<<<< HEAD
- [ ] T032 [P] [US3] Add integration assertions for immutable audit entries, old/new status values, actor ID, linked `stock_movements.work_order_id`, and duplicate-request rejection in `be/src/test/java/fpt/qn/mes/workorder/integration/WorkOrderIntegrationTest.java`.
- [ ] T033 [P] [US3] Add `ExecutorService` and `CountDownLatch` concurrency cases inside `be/src/test/java/fpt/qn/mes/workorder/integration/WorkOrderIntegrationTest.java` for 20 distinct Work Orders reserving 1 unit against 10 available units, asserting exactly 10 successes, 10 `INSUFFICIENT_STOCK` failures, zero negative stock, and no duplicate movements.

### Implementation for User Story 3

- [X] T034 [US3] Lock the Work Order row before status validation and lock candidate stock rows in deterministic product/lot/location order to prevent duplicate reservations, deadlocks, and negative quantities in `be/src/main/java/fpt/qn/mes/workorder/infrastructure/persistence/WorkOrderPersistenceAdapter.java` and `be/src/main/java/fpt/qn/mes/workorder/infrastructure/persistence/WorkOrderReservationPersistenceAdapter.java`.
- [X] T035 [US3] Complete audit persistence for both `PLANNED`/`MATERIAL_SHORTAGE` to `READY_TO_PRODUCE` and shortage transitions, ensuring no update/delete API exists for audit rows in `be/src/main/java/fpt/qn/mes/workorder/infrastructure/persistence/AuditLogPersistenceAdapter.java`.
- [X] T036 [US3] Enforce `@PreAuthorize("hasRole('PLANNER')")` on the versioned reservation controller and verify authentication principal handling in `be/src/main/java/fpt/qn/mes/workorder/presentation/ReserveWorkOrderMaterialsController.java`.
- [X] T037 [US3] Complete stock movement traceability and deterministic destination-balance upsert for every selected lot in `be/src/main/java/fpt/qn/mes/inventory/domain/entities/StockMovement.java`, `be/src/main/java/fpt/qn/mes/inventory/infrastructure/persistence/InventoryRecordMapper.java`, and `be/src/main/java/fpt/qn/mes/workorder/infrastructure/persistence/WorkOrderReservationPersistenceAdapter.java`.
- [ ] T038 [US3] Run the full Work Order integration suite with PostgreSQL/Testcontainers and resolve transaction-boundary, lock-timeout, race-condition, duplicate-write, and audit consistency failures in `be/src/test/java/fpt/qn/mes/workorder/integration/WorkOrderIntegrationTest.java`.
=======
- [X] T032 [P] [US3] Add integration assertions for audit actor and old/new status values, linked `stock_movements.work_order_id`, and duplicate-request rejection in `be/src/test/java/fpt/qn/mes/workorder/integration/WorkOrderIntegrationTest.java`.
- [X] T033 [P] [US3] Add `ExecutorService` and `CountDownLatch` concurrency cases inside `be/src/test/java/fpt/qn/mes/workorder/integration/WorkOrderIntegrationTest.java` for 20 distinct Work Orders reserving 1 unit against 10 available units, asserting exactly 10 successes, 10 `INSUFFICIENT_STOCK` failures, zero negative stock, and no duplicate movements.

### Implementation for User Story 3

- [X] T034 [US3] Lock the Work Order row before status validation and lock candidate stock rows in deterministic product/lot/location order to prevent duplicate reservations, deadlocks, and negative quantities in `be/src/main/java/fpt/qn/mes/workorder/infrastructure/persistence/adapter/WorkOrderPersistenceAdapter.java` and `be/src/main/java/fpt/qn/mes/workorder/infrastructure/persistence/adapter/WorkOrderReservationPersistenceAdapter.java`.
- [X] T035 [US3] Complete audit persistence for both `PLANNED`/`MATERIAL_SHORTAGE` to `READY_TO_PRODUCE` and shortage transitions in `be/src/main/java/fpt/qn/mes/workorder/infrastructure/persistence/adapter/AuditLogPersistenceAdapter.java`.
- [X] T036 [US3] Enforce `@PreAuthorize("hasRole('PLANNER')")` on the reservation action in `be/src/main/java/fpt/qn/mes/workorder/presentation/WorkOrderController.java`.
- [X] T037 [US3] Complete stock movement traceability and deterministic destination-balance upsert for every selected lot in `be/src/main/java/fpt/qn/mes/workorder/infrastructure/persistence/adapter/WorkOrderReservationPersistenceAdapter.java`.
- [X] T038 [US3] Run the full Work Order integration suite with PostgreSQL/Testcontainers and resolve transaction-boundary, lock-timeout, race-condition, duplicate-write, and audit consistency failures in `be/src/test/java/fpt/qn/mes/workorder/integration/WorkOrderIntegrationTest.java`.
>>>>>>> origin/develop

**Checkpoint**: Concurrent and unauthorized requests cannot corrupt inventory or Work Order state, and every important transition is traceable to the Planner, Work Order, and stock movement.

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Final verification, documentation consistency, and regression protection.

- [X] T039 [P] Update `docs/api-endpoints.md` and `specs/004-reserve-work-order-materials/contracts/reserve-work-order-materials-api.json` with any finalized error details, route mapping, and response metadata changes.
- [X] T040 [P] Review `be/src/main/java/fpt/qn/mes/workorder/application/service/WorkOrderService.java` and `be/src/main/java/fpt/qn/mes/workorder/infrastructure/persistence/WorkOrderReservationPersistenceAdapter.java` for repository calls inside loops, unbounded queries, non-deterministic ordering, and forbidden cross-layer imports.
- [X] T041 Run `cd be && ./mvnw test` and fix all unit, controller, integration, and concurrency regressions across `be/src/test/java/fpt/qn/mes/`.
<<<<<<< HEAD
- [ ] T042 Run every validation scenario in `specs/004-reserve-work-order-materials/quickstart.md`, including success, shortage, warehouse isolation, machine guard, FIFO, authorization, and concurrency checks.
=======
- [X] T042 Run every validation scenario in `specs/004-reserve-work-order-materials/quickstart.md`, including success, shortage, active-warehouse scope, FIFO, authorization, duplicate rejection, retry, and concurrency checks.
>>>>>>> origin/develop
- [X] T043 Review generated jOOQ compatibility, Flyway state, API response envelope, and security behavior in `be/src/main/resources/db/migration/`, `be/src/main/java/fpt/qn/mes/common/`, and `be/src/main/java/fpt/qn/mes/workorder/presentation/` before handoff.

---

## Dependencies & Execution Order

### Phase Dependencies

- **Phase 1 Setup**: No dependency; T002 and T003 can run in parallel after T001 scope review.
- **Phase 2 Foundational**: Depends on T001; T004-T012 can be parallelized by file/module, while T013 depends on the shared test contracts.
- **Phase 3 User Story 1**: Depends on all Phase 2 tasks; T014-T016 are parallel test-writing tasks, then implementation proceeds T017 → T018 → T019 → T020/T021 → T022.
- **Phase 4 User Story 2**: Depends on the reservation path from Phase 3; T023-T024 are parallel test-writing tasks, then T025 → T026 → T027/T028 → T029.
- **Phase 5 User Story 3**: Depends on the success and shortage paths from Phases 3-4; T030-T033 are parallel test-writing tasks, then T034 → T035/T036/T037 → T038.
- **Phase 6 Polish**: Depends on all desired user stories; T039 and T040 can run in parallel before T041-T043.

### User Story Dependencies

- **User Story 1 (P1)**: Depends only on the Foundational phase and is the MVP.
- **User Story 2 (P1)**: Depends on the reservation write path from User Story 1, but is independently testable with shortage fixtures.
- **User Story 3 (P1)**: Depends on both success and shortage flows because it verifies their locking, authorization, and audit behavior.

### Parallel Opportunities

- **Setup**: T002 and T003 can run in parallel because they touch separate configuration/documentation files.
- **Foundational**: T004, T005, T006, T007, T008, T009, T010, T011, and T012 can be split by module/file after T001; avoid parallel edits to the same Java file.
- **User Story 1 tests**: T014, T015, and T016 can be written in parallel because they target separate test files or concerns.
- **User Story 2 tests**: T023 and T024 can be written in parallel before the shortage implementation.
- **User Story 3 tests**: T030-T033 can be written in parallel before the locking/audit implementation.
- **Polish**: T039 and T040 can run in parallel; full test execution starts only after both complete.

## Parallel Example: User Story 1

```text
Task T014: Unit tests in be/src/test/java/fpt/qn/mes/workorder/application/service/WorkOrderServiceTest.java
Task T015: Controller contract tests in be/src/test/java/fpt/qn/mes/workorder/presentation/WorkOrderControllerTest.java
Task T016: HTTP-to-DB tests in be/src/test/java/fpt/qn/mes/workorder/integration/WorkOrderIntegrationTest.java
```

## Parallel Example: User Story 2

```text
Task T023: Shortage service tests in be/src/test/java/fpt/qn/mes/workorder/application/service/WorkOrderServiceTest.java
Task T024: Shortage integration tests in be/src/test/java/fpt/qn/mes/workorder/integration/WorkOrderIntegrationTest.java
```

## Parallel Example: User Story 3

```text
Task T030: Audit unit tests in be/src/test/java/fpt/qn/mes/workorder/application/service/WorkOrderServiceTest.java
Task T031: Authorization tests in be/src/test/java/fpt/qn/mes/workorder/presentation/WorkOrderControllerTest.java
Task T032: Audit/traceability integration tests in be/src/test/java/fpt/qn/mes/workorder/integration/WorkOrderIntegrationTest.java
Task T033: Concurrency integration tests in be/src/test/java/fpt/qn/mes/workorder/integration/WorkOrderIntegrationTest.java
```

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1 setup and Phase 2 foundational contracts.
2. Write and fail T014-T016 before implementation.
3. Implement FIFO locked allocation, success orchestration, versioned Planner endpoint, and success audit through T017-T021.
4. Run T022 and the success scenarios in `specs/004-reserve-work-order-materials/quickstart.md`.
5. Stop and validate the MVP before adding shortage and concurrency enhancements.

### Incremental Delivery

1. Add User Story 1 for successful reservation and `READY_TO_PRODUCE`.
2. Add User Story 2 for atomic shortage handling, warehouse isolation, and retry.
3. Add User Story 3 for concurrency, authorization, and audit/ledger traceability.
4. Complete Phase 6 regression and documentation checks.

## Notes

- Every task starts with `- [ ]`, has a sequential ID, and includes a concrete repository-relative file path.
- `[P]` appears only on tasks that can target different files without depending on incomplete work in the same file.
- `[US1]`, `[US2]`, and `[US3]` map directly to the three user stories in `spec.md`.
- Tests are intentionally included and must be written before the corresponding implementation tasks.
- No migration is planned because the existing schema already contains the required warehouse, stock, movement, Work Order, machine, and audit tables; implementation must preserve their constraints and add only missing domain mappings.
