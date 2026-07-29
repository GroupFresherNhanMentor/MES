# Tasks: Inventory Summary Report

**Input**: Design documents from `specs/report-001-inventory-summary/`  
**Prerequisites**: `plan.md`, `spec.md`, `research.md`, `data-model.md`, `contracts/inventory-summary-report-api.json`, `quickstart.md`

## Format: `- [ ] [ID] [P?] [Story?] Description with file path`

- **[P]**: Parallelizable (different files, no blocking dependencies)
- **[US1]**: User Story 1 - View Inventory Summary Report (Priority: P1)

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Create package structure and response DTO

- [x] T001 [P] Create `InventorySummaryReportDto.java` in `be/src/main/java/fpt/qn/mes/report/application/dto/response/InventorySummaryReportDto.java`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Application UseCase and Domain Repository interfaces

- [x] T002 [P] Create `ReportRepository.java` interface in `be/src/main/java/fpt/qn/mes/report/domain/repository/ReportRepository.java`
- [x] T003 [P] Create `ReportUseCase.java` interface in `be/src/main/java/fpt/qn/mes/report/application/port/in/ReportUseCase.java`

---

## Phase 3: User Story 1 - View Inventory Summary Report (Priority: P1) 🎯 MVP

**Goal**: Implement jOOQ aggregate query in `ReportPersistenceAdapter`, service logic in `ReportService`, and `@GetMapping("/inventory-summary")` in `ReportController`.

**Independent Test**: Send `GET /api/reports/inventory-summary` with valid authorization; verify HTTP 200 OK with aggregated summary rows.

### Tests for User Story 1 (REQUIRED)

- [x] T004 [P] [US1] Unit test: `getInventorySummary_HappyPath_ReturnsSummaryList` in `be/src/test/java/fpt/qn/mes/report/application/service/ReportServiceTest.java`

### Implementation for User Story 1

- [x] T005 [US1] Implement jOOQ `getInventorySummary` aggregation query in `be/src/main/java/fpt/qn/mes/report/infrastructure/persistence/ReportPersistenceAdapter.java`
- [x] T006 [US1] Implement `getInventorySummary` in `be/src/main/java/fpt/qn/mes/report/application/service/ReportService.java`
- [x] T007 [US1] Implement `@GetMapping("/inventory-summary")` with `@PreAuthorize("hasAnyRole('ADMIN', 'FACTORY_MANAGER', 'AUDITOR')")` in `be/src/main/java/fpt/qn/mes/report/presentation/ReportController.java`

---

## Phase 4: Polish & Cross-Cutting Concerns

**Purpose**: API Documentation & clean build validation

- [x] T008 [P] Add Section 15 Reporting to `docs/api-endpoints.md`
- [x] T009 [P] Run Maven test suite (`./mvnw test -Dtest=ReportServiceTest`) and confirm clean build

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)** & **Foundational (Phase 2)**: Block User Story 1.
- **User Story 1 (Phase 3)**: Can proceed immediately after Phase 2.
- **Polish (Phase 4)**: Depends on US1 completion.

### Parallel Opportunities

- T001, T002, T003 can run in parallel.
- T004 (Unit test) can be written alongside implementation.
