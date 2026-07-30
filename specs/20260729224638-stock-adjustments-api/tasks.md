# Tasks: Stock Adjustments API & Threshold Approval Workflow

**Input**: Design documents from `/specs/20260729224638-stock-adjustments-api/`

**Prerequisites**: plan.md (required), spec.md (required), research.md, data-model.md, contracts/

**Tests**: Tests are MANDATORY per Constitution VI. Every user story includes unit tests for service logic and integration tests for the full HTTP → DB flow, including concurrency tests.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- File paths are relative to `be/` backend root

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Database migration and domain entity setup

- [x] T001 Create Flyway migration script `V20260729224500__add_stock_adjustment_approvals.sql` in `be/src/main/resources/db/migration/`
- [x] T002 [P] Create domain entity `StockAdjustmentApproval.java` in `be/src/main/java/fpt/qn/mes/inventory/domain/entities/`
- [x] T003 [P] Create repository interface `StockAdjustmentApprovalRepository.java` in `be/src/main/java/fpt/qn/mes/inventory/domain/repository/`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core jOOQ persistence adapter, DTOs, and MapStruct mappers

- [x] T004 Create `StockAdjustmentApprovalRecordMapper.java` in `be/src/main/java/fpt/qn/mes/inventory/infrastructure/persistence/`
- [x] T005 Implement `StockAdjustmentApprovalPersistenceAdapter.java` in `be/src/main/java/fpt/qn/mes/inventory/infrastructure/persistence/`
- [x] T006 [P] Create DTOs `StockAdjustmentRequest.java`, `StockAdjustmentResponse.java`, `StockAdjustmentApprovalDto.java` in `be/src/main/java/fpt/qn/mes/inventory/application/dto/`
- [x] T007 [P] Create MapStruct mapper `StockAdjustmentApprovalDtoMapper.java` in `be/src/main/java/fpt/qn/mes/inventory/application/mapper/`
- [x] T008 Update `InventoryUseCase.java` and `InventoryService.java` port definitions for stock adjustment execution

---

## Phase 3: User Story 1 - Standard Stock Adjustment Execution (Priority: P1) 🎯 MVP

**Goal**: Allow warehouse users to submit stock adjustments (`POST /api/stock-adjustments`) with mandatory reason, updating stock balance and recording immutable `ADJUSTMENT` movement.

**Independent Test**: Execute `POST /api/stock-adjustments` within threshold with valid reason; verify `StockBalance` quantity is updated and a `StockMovement` of type `ADJUSTMENT` is recorded in `stock_movements`.

### Tests for User Story 1 (REQUIRED)

- [x] T009 [P] [US1] Unit test: `StockAdjustmentServiceTest.java` (happy path stock adjustment within threshold) in `be/src/test/java/fpt/qn/mes/inventory/service/`
- [x] T010 [P] [US1] Controller slice test: `StockAdjustmentControllerTest.java` (`POST /api/stock-adjustments`) in `be/src/test/java/fpt/qn/mes/inventory/presentation/`
- [x] T011 [P] [US1] Integration test: `StockAdjustmentIntegrationTest.java` (standard adjustment flow HTTP → DB) in `be/src/test/java/fpt/qn/mes/inventory/integration/`

### Implementation for User Story 1

- [x] T012 [US1] Implement `adjustStock` business logic in `be/src/main/java/fpt/qn/mes/inventory/application/service/InventoryService.java` (within-threshold path)
- [x] T013 [US1] Implement `POST /api/stock-adjustments` endpoint in `be/src/main/java/fpt/qn/mes/inventory/presentation/InventoryController.java`

---

## Phase 4: User Story 2 - Negative Balance Guard & Reason Validation (Priority: P1)

**Goal**: Block stock adjustments missing a reason or attempting to reduce `StockBalance` below zero (`resultingQuantity < 0`), returning HTTP 400 Bad Request.

**Independent Test**: Submit negative adjustment exceeding current balance or adjustment with blank reason; verify system returns HTTP 400 Bad Request without modifying database state.

### Tests for User Story 2 (REQUIRED)

- [x] T014 [P] [US2] Unit test: Exception branches in `StockAdjustmentServiceTest.java` (blank reason, resulting negative quantity) in `be/src/test/java/fpt/qn/mes/inventory/service/`
- [x] T015 [P] [US2] Concurrency test methods in `StockAdjustmentIntegrationTest.java` (race conditions on concurrent negative adjustments) in `be/src/test/java/fpt/qn/mes/inventory/integration/`

### Implementation for User Story 2

- [x] T016 [US2] Add mandatory non-blank `@NotBlank` validation on `reason` in `StockAdjustmentRequest.java`
- [x] T017 [US2] Implement negative balance check (`resultingQuantity < 0`) throwing `InvalidStockAdjustmentException` in `InventoryService.java`

---

## Phase 5: User Story 3 - Threshold Approval Workflow for Large Adjustments (Priority: P2)

**Goal**: Adjustments exceeding threshold are saved into `stock_adjustment_approvals`. Factory Manager calling `/approve` or `/reject` updates balance (on approve) and deletes the pending record from DB.

**Independent Test**: Submit adjustment exceeding threshold, verify stored in `stock_adjustment_approvals`. Call `/approve` as Factory Manager, verify balance updated, movement logged, and record deleted from `stock_adjustment_approvals`. Call `/reject`, verify record deleted without balance change.

### Tests for User Story 3 (REQUIRED)

- [x] T018 [P] [US3] Unit test: `StockAdjustmentServiceTest.java` (threshold exceeded path, approve path, reject path) in `be/src/test/java/fpt/qn/mes/inventory/service/`
- [x] T019 [P] [US3] Controller slice test: `StockAdjustmentControllerTest.java` (`/approve`, `/reject`, `/pending`) in `be/src/test/java/fpt/qn/mes/inventory/presentation/`
- [x] T020 [P] [US3] Integration test: `StockAdjustmentIntegrationTest.java` (threshold approval flow & record deletion) in `be/src/test/java/fpt/qn/mes/inventory/integration/`

### Implementation for User Story 3

- [x] T021 [US3] Add static threshold evaluation logic (`private static final BigDecimal ADJUSTMENT_THRESHOLD = new BigDecimal("100.00");`) in `InventoryService.java`
- [x] T022 [US3] Implement pending approval save logic in `InventoryService.java`
- [x] T023 [US3] Implement `approveAdjustment` and `rejectAdjustment` service methods with row deletion in `InventoryService.java`
- [x] T024 [US3] Add `GET /api/stock-adjustments/pending`, `POST /api/stock-adjustments/{id}/approve`, `POST /api/stock-adjustments/{id}/reject` endpoints in `InventoryController.java`

---

## Phase 6: Polish & Verification

- [x] T025 [P] Run full test suite `./mvnw test -Djooq.codegen.skip=true` and confirm all tests pass
- [x] T026 Run quickstart validation scenarios from `specs/20260729224638-stock-adjustments-api/quickstart.md`
