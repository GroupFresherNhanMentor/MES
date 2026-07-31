# Tasks: Stock Transfers API

**Input**: Design documents from `/specs/20260730132400-stock-transfers-api/`

**Prerequisites**: plan.md (required), spec.md (required), research.md, data-model.md, contracts/

**Tests**: Tests are REQUIRED for every feature. Every user story must include unit tests for service logic, controller slice tests, and integration tests with concurrency coverage. See `docs/rules/backend-testing.md`.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `- [ ] [ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1)
- Include exact file paths in descriptions

## Path Conventions

- Spring Boot Backend: `be/src/main/java/fpt/qn/mes/inventory/`
- Test files: `be/src/test/java/fpt/qn/mes/inventory/`

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Project initialization and basic structure (inventory module pre-exists)

- [x] T001 Verify existing inventory domain models and repositories are up to date in `be/src/main/java/fpt/qn/mes/inventory/`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core DTO models that MUST be complete before user story logic can be implemented

- [x] T002 [P] Create `StockTransferRequest` in `be/src/main/java/fpt/qn/mes/inventory/application/dto/request/StockTransferRequest.java`
- [x] T003 [P] Create `StockTransferResponse` in `be/src/main/java/fpt/qn/mes/inventory/application/dto/response/StockTransferResponse.java`

**Checkpoint**: Foundational DTOs ready - user story implementation can begin.

---

## Phase 3: User Story 1 - Transfer Available Inventory Between Warehouse Locations (Priority: P1) 🎯 MVP

**Goal**: Transfer an AVAILABLE quantity of a product lot from a source warehouse location to a destination warehouse location, update balances atomically, and log paired `TRANSFER_OUT` and `TRANSFER_IN` movements with reference number formatted as `TRF-YYYYMMDD-{from_location_code}->{to_location_code}`.

**Independent Test**: Execute `POST /api/stock-transfers` with a valid transfer request, verifying source balance reduces by quantity, destination balance increases by quantity, and two movement logs (`TRANSFER_OUT` and `TRANSFER_IN`) are saved with reference number `TRF-YYYYMMDD-{from_location_code}->{to_location_code}`.

### Tests for User Story 1 (REQUIRED)

> **Write these FIRST — ensure they FAIL before starting implementation**

- [x] T004 [P] [US1] Unit test: `InventoryServiceTest` — test `transferStock` happy path, validation failures (`quantity <= 0`, `fromLocationId == toLocationId`), `InsufficientStockException`, and missing location in `be/src/test/java/fpt/qn/mes/inventory/service/InventoryServiceTest.java`
- [x] T005 [P] [US1] Controller slice test: `InventoryControllerTest` — test `POST /api/stock-transfers` returning HTTP 200 OK and validation error 400 Bad Request in `be/src/test/java/fpt/qn/mes/inventory/presentation/InventoryControllerTest.java`
- [x] T006 [P] [US1] Integration test: `StockTransferIntegrationTest` — test end-to-end HTTP → DB stock transfer and concurrency race condition testing using `CountDownLatch` + `ExecutorService` in `be/src/test/java/fpt/qn/mes/inventory/StockTransferIntegrationTest.java`

### Implementation for User Story 1

- [x] T007 [US1] Add `StockTransferResponse transferStock(StockTransferRequest request, UUID currentUserId)` to port interface `be/src/main/java/fpt/qn/mes/inventory/application/port/in/InventoryUseCase.java`
- [x] T008 [US1] Implement `transferStock` in service `be/src/main/java/fpt/qn/mes/inventory/application/service/InventoryService.java` (validates `quantity > 0`, `fromLocationId != toLocationId`, updates balances, creates `TRANSFER_OUT` and `TRANSFER_IN` movements with `TRF-YYYYMMDD-{from_location_code}->{to_location_code}`)
- [x] T009 [US1] Expose `POST /api/stock-transfers` endpoint in controller `be/src/main/java/fpt/qn/mes/inventory/presentation/InventoryController.java`
- [x] T010 [US1] Configure Security permissions for `/api/stock-transfers` if needed in `be/src/main/java/fpt/qn/mes/auth/infrastructure/config/SecurityConfig.java`

**Checkpoint**: User Story 1 is fully implemented, testable, and verified against tests.

---

## Phase 4: Polish & Cross-Cutting Concerns

**Purpose**: Verification and final validation

- [x] T011 [P] Run unit and controller test suites `./mvnw test -Dtest="InventoryServiceTest,InventoryControllerTest"`
- [x] T012 Verify quickstart validation guide in `specs/20260730132400-stock-transfers-api/quickstart.md`

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies
- **Foundational (Phase 2)**: Depends on Phase 1 - BLOCKS User Story 1
- **User Story 1 (Phase 3)**: Depends on Phase 2 DTOs
- **Polish (Phase 4)**: Depends on User Story 1 completion

### Parallel Opportunities

- T002 and T003 can be written in parallel
- Test tasks T004, T005, T006 can be written in parallel once DTOs exist
