# Tasks: Create Work Order (`POST /api/work-orders`)

**Input**: Design documents from `specs/wo-002-create-work-order/`

**Prerequisites**: plan.md, spec.md, data-model.md, contracts/post-work-order.json, research.md, quickstart.md

**Tests**: Unit tests and Controller tests are REQUIRED.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story?] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (US1, US2, US3)
- Explicit file paths included in all task descriptions

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Feature directory verification

- [x] T001 Verify feature specs directory at `specs/wo-002-create-work-order/`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core exception and repository interface prerequisites

- [x] T002 [P] Verify `BomRepository.findActiveByFinishedProductId` signature in `be/src/main/java/fpt/qn/mes/bom/domain/repository/BomRepository.java`
- [x] T003 [P] Create `BomNotActiveException` in `be/src/main/java/fpt/qn/mes/workorder/application/exception/BomNotActiveException.java`
- [x] T004 [P] Add `BOM_NOT_ACTIVE` error code to `ErrorCode` enum in `be/src/main/java/fpt/qn/mes/common/exception/ErrorCode.java`

---

## Phase 3: User Story 1 - Create Work Order with Active BOM & Calculate Requirements (Priority: P1) 🎯 MVP

**Goal**: Allow Planners to create a Work Order, automatically bind the Active BOM `bomId`, and calculate `work_order_materials` using `plannedQuantity * quantityPerUnit * (1 + scrapRate)`.

**Independent Test**: Run `WorkOrderServiceTest` and `WorkOrderControllerTest` verifying Work Order creation with active BOM link and calculated material requirements.

### Tests for User Story 1 (REQUIRED)

- [x] T005 [P] [US1] Unit test: `WorkOrderServiceTest` for `createWorkOrder` happy path in `be/src/test/java/fpt/qn/mes/workorder/application/service/WorkOrderServiceTest.java`
- [x] T006 [P] [US1] Controller test: `WorkOrderControllerTest` for `POST /api/work-orders` HTTP 201 response in `be/src/test/java/fpt/qn/mes/workorder/presentation/WorkOrderControllerTest.java`

### Implementation for User Story 1

- [x] T007 [P] [US1] Create `CreateWorkOrderRequest` DTO in `be/src/main/java/fpt/qn/mes/workorder/application/dto/request/CreateWorkOrderRequest.java`
- [x] T008 [P] [US1] Add `createWorkOrder(CreateWorkOrderRequest request, UUID currentUserId)` signature to `WorkOrderUseCase` in `be/src/main/java/fpt/qn/mes/workorder/application/port/in/WorkOrderUseCase.java`
- [x] T009 [US1] Implement `toRecord(WorkOrder w)` in `WorkOrderRecordMapper` in `be/src/main/java/fpt/qn/mes/workorder/infrastructure/persistence/WorkOrderRecordMapper.java`
- [x] T010 [US1] Implement `save(WorkOrder w)` and `saveMaterial(WorkOrderMaterial m)` in `WorkOrderPersistenceAdapter` in `be/src/main/java/fpt/qn/mes/workorder/infrastructure/persistence/WorkOrderPersistenceAdapter.java`
- [x] T011 [US1] Implement `createWorkOrder` with Active BOM lookup and material calculation formula in `WorkOrderService` in `be/src/main/java/fpt/qn/mes/workorder/application/service/WorkOrderService.java`
- [x] T012 [US1] Implement `@PostMapping` endpoint in `WorkOrderController` in `be/src/main/java/fpt/qn/mes/workorder/presentation/WorkOrderController.java`

**Checkpoint**: User Story 1 fully functional and testable independently.

---

## Phase 4: User Story 2 - Role Access Control & Audit Logging (Priority: P2)

**Goal**: Restrict `POST /api/work-orders` exclusively to `ROLE_PLANNER` and write audit log `CREATE_WORK_ORDER`.

**Independent Test**: Run `WorkOrderControllerTest` verifying HTTP 403 Forbidden for `ADMIN`, `OPERATOR`, `GUEST` and HTTP 201 for `PLANNER`.

### Tests for User Story 2 (REQUIRED)

- [x] T013 [P] [US2] Add security test cases for `ROLE_PLANNER` (201) vs `ROLE_ADMIN`/`ROLE_OPERATOR` (403) to `WorkOrderControllerTest` in `be/src/test/java/fpt/qn/mes/workorder/presentation/WorkOrderControllerTest.java`

### Implementation for User Story 2

- [x] T014 [US2] Configure `@PreAuthorize("hasRole('PLANNER')")` on `@PostMapping` in `WorkOrderController` in `be/src/main/java/fpt/qn/mes/workorder/presentation/WorkOrderController.java`

---

## Phase 5: User Story 3 - Validation & Exception Handling (Priority: P3)

**Goal**: Handle missing active BOM error (`BOM_NOT_ACTIVE`) and input validation exceptions.

**Independent Test**: Run `WorkOrderServiceTest` for `BomNotActiveException`.

### Tests for User Story 3 (REQUIRED)

- [x] T015 [P] [US3] Add unit test method for `BomNotActiveException` when product has no active BOM to `WorkOrderServiceTest` in `be/src/test/java/fpt/qn/mes/workorder/application/service/WorkOrderServiceTest.java`

### Implementation for User Story 3

- [x] T016 [US3] Add exception mapping for `BomNotActiveException` in `GlobalExceptionHandler` in `be/src/main/java/fpt/qn/mes/common/exception/GlobalExceptionHandler.java`

---

## Phase 6: Polish & Cross-Cutting Concerns

- [x] T017 [P] Run Maven build `./mvnw compile -pl be` and execute unit tests `./mvnw test -pl be -Dtest=WorkOrderServiceTest,WorkOrderControllerTest`
- [x] T018 Execute manual verification scenarios from `specs/wo-002-create-work-order/quickstart.md`

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)** ➔ **Foundational (Phase 2)** ➔ **User Story 1 (Phase 3 - MVP)** ➔ **User Story 2 (Phase 4)** ➔ **User Story 3 (Phase 5)** ➔ **Polish (Phase 6)**

### Parallel Opportunities

- `T002`, `T003`, `T004` (Foundational) can run in parallel.
- `T005`, `T006` (US1 Tests) can run in parallel.
- `T007`, `T008` (US1 Request/UseCase) can run in parallel.
