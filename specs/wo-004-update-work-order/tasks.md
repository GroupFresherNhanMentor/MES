# Tasks: Update Work Order

**Input**: Design documents from `specs/wo-004-update-work-order/`

**Prerequisites**: plan.md (required), spec.md (required for user stories), research.md, data-model.md, contracts/

**Tests**: Tests are REQUIRED for every feature. Every user story must include unit tests for service logic and integration tests for the full HTTP → DB flow. See `docs/rules/backend-testing.md`.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Seed data updates and new exception/error code additions shared across all user stories

- [X] T001 Add `PLANNED → DRAFT` reverse transition to `be/src/main/resources/seed/work-order-status-transitions.json`
- [X] T002 [P] Add error code `INVALID_STATUS_TRANSITION` to `be/src/main/java/fpt/qn/mes/common/exception/ErrorCode.java`
- [X] T003 [P] Create `InvalidWorkOrderStateException` extending `AppException` with HTTP 400 and error code `BAD_REQUEST` in `be/src/main/java/fpt/qn/mes/workorder/application/exception/InvalidWorkOrderStateException.java`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Repository layer methods and domain infrastructure that MUST be complete before ANY user story can be implemented

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

- [X] T004 Add `findStatusNameById(UUID id)` method signature returning `Optional<String>` to `be/src/main/java/fpt/qn/mes/workorder/domain/repository/WorkOrderRepository.java`
- [X] T005 Add `existsByCodeAndIdNot(String code, UUID excludeId)` method signature returning `boolean` to `be/src/main/java/fpt/qn/mes/workorder/domain/repository/WorkOrderRepository.java`
- [X] T006 Add `updateMaterial(WorkOrderMaterial material)` method signature returning `WorkOrderMaterial` to `be/src/main/java/fpt/qn/mes/workorder/domain/repository/WorkOrderRepository.java`
- [X] T007 Implement `findStatusNameById(UUID)` in `be/src/main/java/fpt/qn/mes/workorder/infrastructure/persistence/WorkOrderPersistenceAdapter.java` — query `WORK_ORDER_STATUSES` table by UUID, return status name
- [X] T008 [P] Implement `existsByCodeAndIdNot(String, UUID)` in `be/src/main/java/fpt/qn/mes/workorder/infrastructure/persistence/WorkOrderPersistenceAdapter.java` — query `WORK_ORDERS` table with `code = ? AND id != ?`
- [X] T009 [P] Implement `updateMaterial(WorkOrderMaterial)` in `be/src/main/java/fpt/qn/mes/workorder/infrastructure/persistence/WorkOrderPersistenceAdapter.java` — update existing `work_order_materials` record using jOOQ
- [X] T010 Implement `update(WorkOrder)` in `be/src/main/java/fpt/qn/mes/workorder/infrastructure/persistence/WorkOrderPersistenceAdapter.java` — replace `throw UnsupportedOperationException` with jOOQ update query on `WORK_ORDERS` table using record mapper
- [X] T011 Add `@Setter` annotation to `be/src/main/java/fpt/qn/mes/workorder/application/dto/request/UpdateWorkOrderRequest.java` for JSON deserialization support

**Checkpoint**: Foundation ready — all repository methods and DTOs available for service layer implementation

---

## Phase 3: User Story 1 — Update Planning Fields of a Work Order (Priority: P1) 🎯 MVP

**Goal**: Planner can modify code, quantity, dates, and priority of a DRAFT or PLANNED Work Order. When quantity changes, materials are recalculated.

**Independent Test**: Send a PUT request with updated fields for a Work Order in DRAFT status and verify the response contains the updated values with recalculated materials.

### Tests for User Story 1 (REQUIRED)

> **Write these FIRST — ensure they FAIL before starting implementation**

- [X] T012 [P] [US1] Unit test: `WorkOrderServiceTest` — add test methods for `updateWorkOrder` happy path: update fields on DRAFT WO, verify repository `update()` called with merged entity, verify response DTO in `be/src/test/java/fpt/qn/mes/workorder/application/service/WorkOrderServiceTest.java`
- [X] T013 [P] [US1] Unit test: `WorkOrderServiceTest` — add test method for material recalculation when `plannedQuantity` changes: mock BOM items, verify `updateMaterial()` called for each material with correct `requiredQuantity = newQty × quantityPerUnit × (1 + scrapRate)` in `be/src/test/java/fpt/qn/mes/workorder/application/service/WorkOrderServiceTest.java`
- [X] T014 [P] [US1] Unit test: `WorkOrderServiceTest` — add test method for partial update: send request with only `code` field non-null, verify other fields unchanged in `be/src/test/java/fpt/qn/mes/workorder/application/service/WorkOrderServiceTest.java`

### Implementation for User Story 1

- [X] T015 [US1] Implement `updateWorkOrder(UUID id, UpdateWorkOrderRequest req)` in `be/src/main/java/fpt/qn/mes/workorder/application/service/WorkOrderService.java` — replace `throw UnsupportedOperationException` with: (1) fetch WO by id or throw `WorkOrderNotFoundException`, (2) fetch current status name via `findStatusNameById`, (3) guard: only DRAFT/PLANNED allowed, (4) merge non-null request fields with existing entity using `WorkOrder.builder()`, (5) call `repository.update(mergedWorkOrder)`, (6) if `plannedQuantity` changed: fetch BOM items and recalculate materials, (7) return full `WorkOrderDto` with materials and events
- [X] T016 [US1] Update `WorkOrderController.update()` in `be/src/main/java/fpt/qn/mes/workorder/presentation/WorkOrderController.java` — replace `throw UnsupportedOperationException` with: add `@PreAuthorize("hasAnyRole('ADMIN', 'PLANNER')")`, add `@Valid`, delegate to `workOrderUseCase.updateWorkOrder(id, req)`, return `ResponseEntity.ok(ApiResponse.success(result, "Work Order updated successfully"))`

**Checkpoint**: At this point, Planners can update planning fields on DRAFT/PLANNED Work Orders with material recalculation

---

## Phase 4: User Story 2 — Simple Status Transition via PUT (Priority: P1)

**Goal**: Planner can transition a Work Order between DRAFT and PLANNED states using the PUT endpoint.

**Independent Test**: Send a PUT request with `workOrderStatusId` set to PLANNED for a DRAFT Work Order and verify the status change in the response.

### Tests for User Story 2 (REQUIRED)

> **Write these FIRST — ensure they FAIL before starting implementation**

- [X] T017 [P] [US2] Unit test: `WorkOrderServiceTest` — add test method for DRAFT → PLANNED transition: mock `findStatusNameById` to return "PLANNED", verify status updated in `be/src/test/java/fpt/qn/mes/workorder/application/service/WorkOrderServiceTest.java`
- [X] T018 [P] [US2] Unit test: `WorkOrderServiceTest` — add test method for PLANNED → DRAFT transition: mock `findStatusNameById` to return "DRAFT", verify status reverted in `be/src/test/java/fpt/qn/mes/workorder/application/service/WorkOrderServiceTest.java`
- [X] T019 [P] [US2] Unit test: `WorkOrderServiceTest` — add test method for rejected transition DRAFT → IN_PROGRESS: mock `findStatusNameById` to return "IN_PROGRESS", verify `InvalidWorkOrderStateException` thrown with message about using action endpoints in `be/src/test/java/fpt/qn/mes/workorder/application/service/WorkOrderServiceTest.java`

### Implementation for User Story 2

- [X] T020 [US2] Add status transition validation logic to `updateWorkOrder()` in `be/src/main/java/fpt/qn/mes/workorder/application/service/WorkOrderService.java` — when `req.getWorkOrderStatusId()` is non-null and different from current: (1) look up requested status name via `findStatusNameById`, (2) validate target status is either "DRAFT" or "PLANNED", (3) throw `InvalidWorkOrderStateException` if not, with message indicating dedicated action endpoints should be used

**Checkpoint**: Status transitions DRAFT ⇄ PLANNED work via PUT; non-planning transitions are rejected

---

## Phase 5: User Story 3 — Input Validation Guards (Priority: P2)

**Goal**: The system enforces strict validation: `plannedQuantity > 0` and `plannedStartDate < plannedEndDate`.

**Independent Test**: Send PUT requests with invalid values (zero quantity, reversed dates) and verify 400 Bad Request responses.

### Tests for User Story 3 (REQUIRED)

> **Write these FIRST — ensure they FAIL before starting implementation**

- [X] T021 [P] [US3] Unit test: `WorkOrderServiceTest` — add test methods for validation failures: (1) `plannedQuantity = 0` throws `AppException` with `INVALID_INPUT`, (2) `plannedQuantity = -5` throws `AppException` with `INVALID_INPUT`, (3) `plannedStartDate > plannedEndDate` throws `AppException` with `INVALID_INPUT` in `be/src/test/java/fpt/qn/mes/workorder/application/service/WorkOrderServiceTest.java`

### Implementation for User Story 3

- [X] T022 [US3] Add input validation to `updateWorkOrder()` in `be/src/main/java/fpt/qn/mes/workorder/application/service/WorkOrderService.java` — before merge: (1) if `req.getPlannedQuantity() != null && req.getPlannedQuantity().compareTo(BigDecimal.ZERO) <= 0` throw `AppException(400, INVALID_INPUT, "Planned quantity must be greater than 0")`, (2) resolve effective start/end dates (use request value if non-null, else existing), (3) if both effective dates non-null and `startDate >= endDate` throw `AppException(400, INVALID_INPUT, "Planned start date must be before planned end date")`
- [X] T023 [US3] Create `InvalidInputException` extending `AppException` with HTTP 400 and error code `INVALID_INPUT` in `be/src/main/java/fpt/qn/mes/workorder/application/exception/InvalidInputException.java` — to be used for validation errors

**Checkpoint**: All input validation guards active and producing clear error messages

---

## Phase 6: User Story 4 — Reject Updates for Non-Planning States (Priority: P2)

**Goal**: Work Orders in READY_TO_PRODUCE, IN_PROGRESS, PAUSED, COMPLETED, or CANCELLED reject all PUT updates.

**Independent Test**: Attempt PUT-update on a Work Order in each non-planning status and verify 400 Bad Request.

### Tests for User Story 4 (REQUIRED)

> **Write these FIRST — ensure they FAIL before starting implementation**

- [X] T024 [P] [US4] Unit test: `WorkOrderServiceTest` — add test methods for rejected states: (1) WO in READY_TO_PRODUCE throws `InvalidWorkOrderStateException`, (2) WO in IN_PROGRESS throws, (3) WO in COMPLETED throws, (4) WO in CANCELLED throws in `be/src/test/java/fpt/qn/mes/workorder/application/service/WorkOrderServiceTest.java`

### Implementation for User Story 4

- [X] T025 [US4] Verify the state guard in `updateWorkOrder()` in `be/src/main/java/fpt/qn/mes/workorder/application/service/WorkOrderService.java` already covers this (from T015 step 3). If not, ensure the guard checks current status name is "DRAFT" or "PLANNED" and throws `InvalidWorkOrderStateException("Work Order cannot be modified in its current state. Only DRAFT and PLANNED Work Orders can be updated.")` for all other statuses

**Checkpoint**: Non-planning states fully guarded against PUT modification

---

## Phase 7: User Story 5 — Unique Code Enforcement (Priority: P2)

**Goal**: When a Planner updates the `code` field, the system ensures no other Work Order uses that code.

**Independent Test**: Update a Work Order's code to match another existing WO's code and verify conflict error.

### Tests for User Story 5 (REQUIRED)

> **Write these FIRST — ensure they FAIL before starting implementation**

- [X] T026 [P] [US5] Unit test: `WorkOrderServiceTest` — add test methods: (1) duplicate code throws exception with `WORK_ORDER_CODE_EXISTS`, (2) keeping same code unchanged does NOT throw (self-exclusion) in `be/src/test/java/fpt/qn/mes/workorder/application/service/WorkOrderServiceTest.java`

### Implementation for User Story 5

- [X] T027 [US5] Add code uniqueness check to `updateWorkOrder()` in `be/src/main/java/fpt/qn/mes/workorder/application/service/WorkOrderService.java` — after validation, before merge: if `req.getCode() != null && repository.existsByCodeAndIdNot(req.getCode(), id)` throw `AppException(400, WORK_ORDER_CODE_EXISTS, "Work Order code '" + req.getCode() + "' already exists")`
- [X] T028 [US5] Create `WorkOrderCodeExistsException` extending `AppException` with HTTP 400 and error code `WORK_ORDER_CODE_EXISTS` in `be/src/main/java/fpt/qn/mes/workorder/application/exception/WorkOrderCodeExistsException.java`

**Checkpoint**: Code uniqueness enforced with self-exclusion

---

## Phase 8: User Story 6 — Authorization Enforcement (Priority: P2)

**Goal**: Only ADMIN and PLANNER roles can access the PUT endpoint. Other roles get 403; unauthenticated requests get 401.

**Independent Test**: Send PUT requests with JWT tokens for OPERATOR, ADMIN, PLANNER roles and verify access control.

### Tests for User Story 6 (REQUIRED)

> **Write these FIRST — ensure they FAIL before starting implementation**

- [X] T029 [P] [US6] Unit test: `WorkOrderControllerTest` — add test method verifying `@PreAuthorize` security role enforcement and status 200 response in `be/src/test/java/fpt/qn/mes/workorder/presentation/WorkOrderControllerTest.java`

### Implementation for User Story 6

- [X] T030 [US6] Verify `@PreAuthorize("hasAnyRole('ADMIN', 'PLANNER')")` is correctly applied on the `update()` method in `be/src/main/java/fpt/qn/mes/workorder/presentation/WorkOrderController.java` — this should already be in place from T016; confirm it works with the existing security configuration

**Checkpoint**: All authorization scenarios tested end-to-end

---

## Phase 9: Polish & Cross-Cutting Concerns

**Purpose**: Improvements that affect multiple user stories

- [X] T031 [P] Run full test suite `cd be && ./mvnw test` — verify all existing tests still pass alongside new tests
- [X] T032 [P] Review test coverage — ensure all service branches and exception paths are covered for `updateWorkOrder()` in `WorkOrderServiceTest`
- [X] T033 Run quickstart.md validation — execute all 7 curl scenarios against running application and verify expected responses
- [X] T034 Code cleanup — review all modified files for consistent code style (no method references, Lombok annotations, lambda syntax per `docs/rules/backend-java-style.md`)

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies — can start immediately
- **Foundational (Phase 2)**: Depends on Phase 1 completion — BLOCKS all user stories
- **User Stories (Phase 3–8)**: All depend on Foundational phase completion
  - US1 (Phase 3) and US2 (Phase 4) are both P1 and can run in parallel after Foundation
  - US3, US4, US5, US6 (Phases 5–8) are P2 and can run in parallel after Foundation
  - US4 depends on US1's state guard logic being in place (T015)
  - US6 integration tests depend on all service logic being implemented (runs last)
- **Polish (Phase 9)**: Depends on all user stories being complete

### User Story Dependencies

- **US1 (P1)**: Can start after Foundational (Phase 2) — No dependencies on other stories
- **US2 (P1)**: Can start after Foundational (Phase 2) — Builds on US1's `updateWorkOrder()` method but logic is additive
- **US3 (P2)**: Can start after US1 — Adds validation to existing method
- **US4 (P2)**: Depends on US1's state guard (T015) — verifies it covers all non-planning states
- **US5 (P2)**: Can start after Foundational (Phase 2) — Adds code uniqueness check to existing method
- **US6 (P2)**: Integration tests depend on US1–US5 all being implemented — runs last

### Within Each User Story

- Tests MUST be written and FAIL before implementation
- Repository methods before service logic
- Service logic before controller changes
- Core implementation before integration testing

### Parallel Opportunities

- T002, T003 (Setup phase) can run in parallel
- T007, T008, T009 (Foundational persistence methods) — T008 and T009 can run in parallel
- T012, T013, T014 (US1 tests) can all run in parallel
- T017, T018, T019 (US2 tests) can all run in parallel
- US3, US4, US5 tests (T021, T024, T026) can all run in parallel

---

## Parallel Example: User Story 1

```bash
# Launch all tests for US1 together:
Task: "T012 — Unit test: updateWorkOrder happy path"
Task: "T013 — Unit test: material recalculation"
Task: "T014 — Unit test: partial update"

# Then implement sequentially:
Task: "T015 — Implement updateWorkOrder() in WorkOrderService"
Task: "T016 — Update WorkOrderController.update()"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup (T001–T003)
2. Complete Phase 2: Foundational (T004–T011)
3. Complete Phase 3: User Story 1 (T012–T016)
4. **STOP and VALIDATE**: Test US1 independently — Planner can update DRAFT WO fields
5. Deploy/demo if ready

### Incremental Delivery

1. Complete Setup + Foundational → Foundation ready
2. Add US1 + US2 → Test independently → Planners can update fields AND transition DRAFT ⇄ PLANNED
3. Add US3 + US4 + US5 → Test independently → Full validation guards active
4. Add US6 → Full integration test coverage → Deploy
5. Each story adds value without breaking previous stories

### Parallel Team Strategy

With multiple developers:

1. Team completes Setup + Foundational together
2. Once Foundational is done:
   - Developer A: US1 (field updates + material recalculation)
   - Developer B: US2 (status transitions)
3. After US1 + US2 merge:
   - Developer A: US3 + US4 (validation + state guards)
   - Developer B: US5 (code uniqueness)
4. Developer A or B: US6 (integration tests covering everything)

---

## Notes

- [P] tasks = different files, no dependencies
- [Story] label maps task to specific user story for traceability
- Each user story should be independently completable and testable
- Verify tests fail before implementing
- Commit after each task or logical group
- Stop at any checkpoint to validate story independently
- Avoid: vague tasks, same file conflicts, cross-story dependencies that break independence
