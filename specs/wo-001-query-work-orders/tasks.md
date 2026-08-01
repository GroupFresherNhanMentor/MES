# Tasks: Query Work Orders (`GET /api/work-orders`)

**Input**: Design documents from `specs/wo-001-query-work-orders/`

**Prerequisites**: plan.md, spec.md, data-model.md, contracts/get-work-orders.json, research.md, quickstart.md

**Tests**: Unit tests and Controller slice tests are REQUIRED.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (US1, US2, US3)
- Explicit file paths included in all task descriptions

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Project initialization and workspace verification

- [x] T001 Verify feature specs directory at `specs/wo-001-query-work-orders/`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core infrastructure that MUST be verified before user story implementation

- [x] T002 [P] Verify jOOQ generated schema for `WORK_ORDERS` table in `be/src/main/java/fpt/qn/mes/jooq/tables/records/WorkOrdersRecord.java`
- [x] T003 [P] Verify Spring Security configuration and JWT role converter in `be/src/main/java/fpt/qn/mes/auth/infrastructure/config/SecurityConfig.java`

---

## Phase 3: User Story 1 - Paginated Work Order Search & Filter (Priority: P1) 🎯 MVP

**Goal**: Allow authorized users to search and filter Work Orders by status, product, and fuzzy code string with 0-based pagination.

**Independent Test**: Run `WorkOrderServiceTest` and `WorkOrderControllerTest` verifying filter logic and pagination structure.

### Tests for User Story 1 (REQUIRED)

- [x] T004 [P] [US1] Unit test: `WorkOrderServiceTest` happy path & filters in `be/src/test/java/fpt/qn/mes/workorder/application/service/WorkOrderServiceTest.java`
- [x] T005 [P] [US1] Controller test: `WorkOrderControllerTest` for `GET /api/work-orders` HTTP 200 response in `be/src/test/java/fpt/qn/mes/workorder/presentation/WorkOrderControllerTest.java`

### Implementation for User Story 1

- [x] T006 [P] [US1] Update `WorkOrder` entity fields and builder in `be/src/main/java/fpt/qn/mes/workorder/domain/entities/WorkOrder.java`
- [x] T007 [P] [US1] Add `findAll(page, size, finishedProductId, statusId, code)` method signature to `WorkOrderRepository` in `be/src/main/java/fpt/qn/mes/workorder/domain/repository/WorkOrderRepository.java`
- [x] T008 [US1] Implement `toDomain(WorkOrdersRecord r)` in `WorkOrderRecordMapper` in `be/src/main/java/fpt/qn/mes/workorder/infrastructure/persistence/WorkOrderRecordMapper.java`
- [x] T009 [US1] Implement jOOQ dynamic query `findAll` in `WorkOrderPersistenceAdapter` in `be/src/main/java/fpt/qn/mes/workorder/infrastructure/persistence/WorkOrderPersistenceAdapter.java`
- [x] T010 [P] [US1] Add `getWorkOrders` method signature to `WorkOrderUseCase` in `be/src/main/java/fpt/qn/mes/workorder/application/port/in/WorkOrderUseCase.java`
- [x] T011 [US1] Implement `getWorkOrders` in `WorkOrderService` in `be/src/main/java/fpt/qn/mes/workorder/application/service/WorkOrderService.java`
- [x] T012 [US1] Implement `@GetMapping` endpoint in `WorkOrderController` in `be/src/main/java/fpt/qn/mes/workorder/presentation/WorkOrderController.java`

**Checkpoint**: User Story 1 fully functional and testable independently.

---

## Phase 4: User Story 2 - Role-Based Query Authorization (Priority: P2)

**Goal**: Enforce strict role-based access control for `ADMIN`, `PLANNER`, `OPERATOR`, `FACTORY_MANAGER`, and `AUDITOR`.

**Independent Test**: Run `WorkOrderControllerTest` testing 401 Unauthorized (missing token) and 403 Forbidden (unauthorized role).

### Tests for User Story 2 (REQUIRED)

- [x] T013 [P] [US2] Add security test methods for authorization in `WorkOrderControllerTest` in `be/src/test/java/fpt/qn/mes/workorder/presentation/WorkOrderControllerTest.java`

### Implementation for User Story 2

- [x] T014 [US2] Configure `@PreAuthorize("hasAnyRole('ADMIN', 'PLANNER', 'OPERATOR', 'FACTORY_MANAGER', 'AUDITOR')")` on `WorkOrderController` in `be/src/main/java/fpt/qn/mes/workorder/presentation/WorkOrderController.java`

---

## Phase 5: User Story 3 - Pagination & Large Dataset Retrieval (Priority: P3)

**Goal**: Verify 0-based page calculations and totalElements/totalPages pagination metadata for large datasets.

**Independent Test**: Execute pagination tests in `WorkOrderServiceTest`.

### Tests for User Story 3 (REQUIRED)

- [x] T015 [P] [US3] Add pagination edge-case unit tests to `WorkOrderServiceTest` in `be/src/test/java/fpt/qn/mes/workorder/application/service/WorkOrderServiceTest.java`

### Implementation for User Story 3

- [x] T016 [US3] Verify `PageResponse` calculation logic in `WorkOrderService` in `be/src/main/java/fpt/qn/mes/workorder/application/service/WorkOrderService.java`

---

## Phase 6: Polish & Cross-Cutting Concerns

- [x] T017 [P] Run Maven build `./mvnw compile -pl be` and execute unit tests `./mvnw test -pl be -Dtest=WorkOrderServiceTest,WorkOrderControllerTest`
- [x] T018 Execute manual verification scenarios from `specs/wo-001-query-work-orders/quickstart.md`

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)** ➔ **Foundational (Phase 2)** ➔ **User Story 1 (Phase 3 - MVP)** ➔ **User Story 2 (Phase 4)** ➔ **User Story 3 (Phase 5)** ➔ **Polish (Phase 6)**

### Parallel Opportunities

- `T002`, `T003` (Foundational) can run in parallel.
- `T004`, `T005` (US1 Tests) can run in parallel.
- `T006`, `T007`, `T010` (US1 Interfaces/Entities) can run in parallel.
