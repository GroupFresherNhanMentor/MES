# Tasks: Audit Log for Important Actions (FR-AUD-001)

**Input**: Design documents from `/specs/004-audit-log-actions/`

**Prerequisites**: plan.md (required), spec.md (required for user stories), research.md, data-model.md, contracts/

**Tests**: Tests are REQUIRED for every feature. Every user story includes unit tests for service logic and integration tests for the full HTTP → DB flow. See `docs/rules/backend-testing.md`.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

## Path Conventions

- Backend: `be/src/main/java/fpt/qn/mes/`
- Backend Tests: `be/src/test/java/fpt/qn/mes/`
- Migrations: `be/src/main/resources/db/migration/`

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Database schema creation and migration setup

- [X] T001 Create Flyway migration script in `be/src/main/resources/db/migration/V20260731143000__create_audit_logs_table.sql`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core entity, repository, and persistence layer setup required by all user stories

- [X] T002 Execute jOOQ codegen to generate `AuditLogsRecord` and table mapping in `be/target/generated-sources/jooq/`
- [X] T003 [P] Create `AuditAction` enum in `be/src/main/java/fpt/qn/mes/audit/domain/entities/AuditAction.java`
- [X] T004 [P] Create `AuditLog` domain entity in `be/src/main/java/fpt/qn/mes/audit/domain/entities/AuditLog.java`
- [X] T005 Create `AuditLogRepository` interface in `be/src/main/java/fpt/qn/mes/audit/domain/repository/AuditLogRepository.java` and criteria in `be/src/main/java/fpt/qn/mes/audit/domain/repository/criteria/AuditLogSearchCriteria.java`
- [X] T006 Create `AuditLogRecordMapper` in `be/src/main/java/fpt/qn/mes/audit/infrastructure/persistence/audit/AuditLogRecordMapper.java`
- [X] T007 Implement `AuditLogPersistenceAdapter` in `be/src/main/java/fpt/qn/mes/audit/infrastructure/persistence/audit/AuditLogPersistenceAdapter.java`

**Checkpoint**: Foundation ready — user story implementation can now begin

---

## Phase 3: User Story 1 - Automated Audit Trail Recording for Critical Actions (Priority: P1) 🎯 MVP

**Goal**: Record audit logs automatically across all 17 critical actions in manufacturing, quality, maintenance, inventory, and BOM operations.

**Independent Test**: Execute critical actions (e.g. `CREATE_WORK_ORDER` or `ADJUST_STOCK`) and verify audit log record creation with accurate actorId, action, entityId, oldValue, newValue, and timestamp.

### Tests for User Story 1 (REQUIRED)

- [X] T008 [P] [US1] Unit test: `AuditLogServiceTest` — verify recording logic for all 17 critical actions in `be/src/test/java/fpt/qn/mes/audit/service/AuditLogServiceTest.java`

### Implementation for User Story 1

- [X] T009 [P] [US1] Create `AuditLogPort` interface in `be/src/main/java/fpt/qn/mes/audit/application/port/out/AuditLogPort.java`
- [X] T010 [P] [US1] Create `AuditLogUseCase` interface in `be/src/main/java/fpt/qn/mes/audit/application/port/in/AuditLogUseCase.java`
- [X] T011 [US1] Implement `AuditLogService` in `be/src/main/java/fpt/qn/mes/audit/application/service/AuditLogService.java`
- [X] T012 [US1] Integrate `AuditLogPort` calls into `InventoryService`, `WorkOrderService`, `QualityService`, `MaintenanceService`, and `BomService`

**Checkpoint**: User Story 1 is fully functional and audit logs are recorded transactionally.

---

## Phase 4: User Story 2 - Audit Log Immutability Enforcement (Priority: P2)

**Goal**: Guarantee that audit log entries cannot be modified or deleted.

**Independent Test**: Attempt update or delete operations on audit records via SQL/repository and verify rejection.

### Tests for User Story 2 (REQUIRED)

- [X] T013 [P] [US2] Integration test: `AuditLogImmutabilityIntegrationTest` — verify update/delete rejection in `be/src/test/java/fpt/qn/mes/audit/integration/AuditLogImmutabilityIntegrationTest.java`

### Implementation for User Story 2

- [X] T014 [US2] Enforce DB-level immutability trigger in `be/src/main/resources/db/migration/V20260731143000__create_audit_logs_table.sql` and throws UnsupportedOperationException in `be/src/main/java/fpt/qn/mes/audit/infrastructure/persistence/audit/AuditLogPersistenceAdapter.java`

**Checkpoint**: Audit log immutability is strictly enforced at database and adapter levels.

---

## Phase 5: User Story 3 - Audit Log Query and Traceability (Priority: P3)

**Goal**: Provide search and lookup REST endpoints for compliance officers and auditors.

**Independent Test**: Perform GET queries on `/api/audit-logs` filtering by entity, actor, action, and date range.

### Tests for User Story 3 (REQUIRED)

- [X] T015 [P] [US3] Unit test: `AuditLogControllerTest` in `be/src/test/java/fpt/qn/mes/audit/presentation/AuditLogControllerTest.java`
- [X] T016 [P] [US3] Integration test: `AuditLogIntegrationTest` — verify full HTTP → DB query flow, pagination, 401 unauthenticated, 403 wrong role in `be/src/test/java/fpt/qn/mes/audit/integration/AuditLogIntegrationTest.java`

### Implementation for User Story 3

- [X] T017 [P] [US3] Create `AuditLogResponse` DTO and `AuditLogSearchRequest` in `be/src/main/java/fpt/qn/mes/audit/application/dto/AuditLogResponse.java` and `be/src/main/java/fpt/qn/mes/audit/application/dto/AuditLogSearchRequest.java`
- [X] T018 [P] [US3] Create `AuditLogDtoMapper` in `be/src/main/java/fpt/qn/mes/audit/application/mapper/AuditLogDtoMapper.java`
- [X] T019 [US3] Implement `AuditLogController` in `be/src/main/java/fpt/qn/mes/audit/presentation/AuditLogController.java`

**Checkpoint**: Audit log search and lookup endpoints fully operational.

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Final verification, OpenAPI registration, and documentation

- [X] T020 [P] Register `AuditLogController` OpenAPI group in `be/src/main/java/fpt/qn/mes/common/config/OpenApiConfig.java`
- [X] T021 Run `./mvnw test` to execute full test suite and validate all 280+ tests pass

---

## Dependencies & Execution Order

### User Story Completion Order
- **User Story 1 (P1)**: Independent after Foundational phase (Phase 2)
- **User Story 2 (P2)**: Independent after Foundational phase (Phase 2)
- **User Story 3 (P3)**: Independent after Foundational phase (Phase 2)

### Implementation Strategy
1. MVP Scope: Setup → Foundational → User Story 1 (Audit Log Recording)
2. Incremental Scope: User Story 2 (Immutability Protection) → User Story 3 (Query API) → Polish
