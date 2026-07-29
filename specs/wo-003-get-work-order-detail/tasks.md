# Tasks: Get Work Order Detail (`GET /api/v1/work-orders/{id}`)

**Input**: Design documents from `specs/wo-003-get-work-order-detail/`

**Prerequisites**: plan.md (required), spec.md (required for user stories), research.md, data-model.md, contracts/

**Tests**: Tests are REQUIRED for every feature. Every user story must include unit tests for service logic and controller slice/integration tests. See `docs/rules/backend-testing.md`.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Project initialization and basic structure verification

- [X] T001 Verify project structure and Maven build configuration in `be/pom.xml`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core DTO and domain interface extensions required before implementing user stories

- [X] T002 [P] Update WorkOrderDto response model to include `materials` and `events` fields in `be/src/main/java/fpt/qn/mes/workorder/application/dto/response/WorkOrderDto.java`
- [X] T003 [P] Add WorkOrderNotFoundException in `be/src/main/java/fpt/qn/mes/workorder/application/exception/WorkOrderNotFoundException.java`
- [X] T004 Add material and event query methods to repository port in `be/src/main/java/fpt/qn/mes/workorder/domain/repository/WorkOrderRepository.java`

**Checkpoint**: Foundation ready - user story implementation can now begin

---

## Phase 3: User Story 1 - Get Detailed Work Order Information with Embedded Materials and Events (Priority: P1) 🎯 MVP

**Goal**: Implement `getWorkOrderById` retrieving Work Order metadata along with embedded material requirements (`materials`) and history events (`events`).

**Independent Test**: Execute `WorkOrderServiceTest` and `WorkOrderControllerTest` verifying successful lookup returning HTTP 200 OK with embedded materials and events arrays.

### Tests for User Story 1 (REQUIRED)

- [X] T005 [P] [US1] Unit test: `WorkOrderServiceTest` happy path for `getWorkOrderById` in `be/src/test/java/fpt/qn/mes/workorder/application/service/WorkOrderServiceTest.java`
- [X] T006 [P] [US1] Controller slice test: `WorkOrderControllerTest` HTTP 200 response for `GET /api/v1/work-orders/{id}` in `be/src/test/java/fpt/qn/mes/workorder/presentation/WorkOrderControllerTest.java`

### Implementation for User Story 1

- [X] T007 [US1] Implement `findById`, `findMaterialsByWorkOrderId`, and `findEventsByWorkOrderId` in `be/src/main/java/fpt/qn/mes/workorder/infrastructure/persistence/WorkOrderPersistenceAdapter.java`
- [X] T008 [US1] Implement `getWorkOrderById` service logic mapping WorkOrder, materials, and events to WorkOrderDto in `be/src/main/java/fpt/qn/mes/workorder/application/service/WorkOrderService.java`
- [X] T009 [US1] Expose `GET /api/v1/work-orders/{id}` endpoint with `@PreAuthorize` authorization in `be/src/main/java/fpt/qn/mes/workorder/presentation/WorkOrderController.java`

**Checkpoint**: At this point, User Story 1 should be fully functional and testable independently

---

## Phase 4: User Story 2 - Work Order Not Found Handling (Priority: P2)

**Goal**: Handle missing Work Order IDs by throwing `WorkOrderNotFoundException` and returning HTTP 404 Not Found.

**Independent Test**: Execute unit test for `WorkOrderServiceTest` and controller test for `WorkOrderControllerTest` with non-existent UUID verifying HTTP 404 `NOT_FOUND`.

### Tests for User Story 2 (REQUIRED)

- [X] T010 [P] [US2] Unit test: `WorkOrderServiceTest` exception path throwing `WorkOrderNotFoundException` in `be/src/test/java/fpt/qn/mes/workorder/application/service/WorkOrderServiceTest.java`
- [X] T011 [P] [US2] Controller slice test: `WorkOrderControllerTest` HTTP 404 response in `be/src/test/java/fpt/qn/mes/workorder/presentation/WorkOrderControllerTest.java`

### Implementation for User Story 2

- [X] T012 [US2] Verify `WorkOrderService` throws `WorkOrderNotFoundException` when `findById` returns empty `Optional` in `be/src/main/java/fpt/qn/mes/workorder/application/service/WorkOrderService.java`

**Checkpoint**: At this point, User Stories 1 AND 2 should both work independently

---

## Phase 5: User Story 3 - Role-Based Access Control Enforcement (Priority: P3)

**Goal**: Enforce authorization restrictions allowing only roles `ADMIN`, `PLANNER`, `OPERATOR`, `FACTORY_MANAGER`, and `AUDITOR`.

**Independent Test**: Execute security tests verifying access control rules.

### Tests for User Story 3 (REQUIRED)

- [X] T013 [P] [US3] Security test: `WorkOrderControllerTest` verify `@PreAuthorize` allowed roles in `be/src/test/java/fpt/qn/mes/workorder/presentation/WorkOrderControllerTest.java`

### Implementation for User Story 3

- [X] T014 [US3] Verify `@PreAuthorize("hasAnyRole('ADMIN', 'PLANNER', 'OPERATOR', 'FACTORY_MANAGER', 'AUDITOR')")` annotation on `@GetMapping("/{id}")` in `be/src/main/java/fpt/qn/mes/workorder/presentation/WorkOrderController.java`

**Checkpoint**: All user stories should now be independently functional

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Verification and documentation polish

- [X] T015 [P] Run full test suite `./mvnw test "-Dtest=WorkOrderServiceTest,WorkOrderControllerTest"` to verify clean build
- [X] T016 Execute manual quickstart verification scenarios per `specs/wo-003-get-work-order-detail/quickstart.md`

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: Can start immediately
- **Foundational (Phase 2)**: Depends on Setup completion - BLOCKS all user stories
- **User Stories (Phase 3+)**: Depend on Foundational phase completion (P1 → P2 → P3)
- **Polish (Phase 6)**: Depends on all user stories being complete

### Parallel Opportunities

- T002, T003 (Foundational) can be implemented in parallel.
- T005, T006 (US1 Tests) can be written in parallel.
- T010, T011 (US2 Tests) can be written in parallel.

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup & Phase 2: Foundational
2. Complete Phase 3: User Story 1
3. **STOP and VALIDATE**: Run `./mvnw test "-Dtest=WorkOrderServiceTest,WorkOrderControllerTest"`
4. Complete Phase 4 & 5 for complete error handling and security verification.
