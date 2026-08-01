# Implementation Tasks: Idempotency Key Mechanism

**Feature Branch**: `20260730164830-idempotency-key-mechanism`
**Spec**: [spec.md](spec.md) | **Plan**: [plan.md](plan.md)

## Phase 1: Database Setup & Migration

- [x] **T-001**: Create Flyway DB migration script `V20260730193000__create_idempotency_keys.sql` in `be/src/main/resources/db/migration/` containing `DROP TABLE IF EXISTS idempotency_keys CASCADE;`, table creation DDL, and unique index `idx_idemp_unauthen`.
- [x] **T-002**: Run `./mvnw clean compile` in `be/` to execute Flyway migration and jOOQ code generation for `idempotency_keys`.

---

## Phase 2: Domain Entities, Repositories & Exceptions

- [x] **T-003**: Create `IdempotencyKey` domain entity class in `fpt.qn.mes.common.idempotency.domain.entities.IdempotencyKey`.
- [x] **T-004**: Create `IdempotencyKeyRepository` interface in `fpt.qn.mes.common.idempotency.domain.repository.IdempotencyKeyRepository` extending `BaseDomainRepository<IdempotencyKey, UUID>`.
- [x] **T-005**: Create `IdempotencyConflictException` (HTTP 409) and `IdempotencyPayloadMismatchException` (HTTP 400) extending `AppException` in `fpt.qn.mes.common.idempotency.application.exception`.

---

## Phase 3: Persistence Adapter, Service Layer & Scheduled Purge

- [x] **T-006**: Create `IdempotencyKeyRecordMapper` MapStruct mapper in `fpt.qn.mes.common.idempotency.application.mapper`.
- [x] **T-007**: Create `IdempotencyKeyPersistenceAdapter` extending `BaseRepository<IdempotencyKeysRecord>` implementing `IdempotencyKeyRepository` in `fpt.qn.mes.common.idempotency.infrastructure.persistence`.
- [x] **T-008**: Create `IdempotencyService` in `fpt.qn.mes.common.idempotency.application.service` to handle key lookup, locking claim, SHA-256 payload verification, and response completion.
- [x] **T-009**: Create `IdempotencyCleanupScheduler` in `fpt.qn.mes.common.idempotency.application.service` with `@Scheduled` task to purge records older than 1 day.

---

## Phase 4: HTTP Interceptor Filter & Security Registration

- [x] **T-0010**: Create `IdempotencyFilter` extending `OncePerRequestFilter` in `fpt.qn.mes.common.idempotency.infrastructure.filter` to intercept `X-Idempotency-Key` headers, wrap request/response streams, resolve `user_id` or `client_identifier`, and replay cached responses.
- [x] **T-011**: Register `IdempotencyFilter` in `SecurityConfig.java` via `.addFilterAfter(idempotencyFilter, BearerTokenAuthenticationFilter.class)`.

---

## Phase 5: Verification & Mandatory Tests

- [x] **T-012**: Write unit tests in `be/src/test/java/fpt/qn/mes/common/idempotency/IdempotencyServiceTest.java`.
- [x] **T-013**: Write integration and `CountDownLatch` concurrency tests in `be/src/test/java/fpt/qn/mes/common/idempotency/IdempotencyFilterIntegrationTest.java`.
- [x] **T-014**: Run full `./mvnw test` suite to verify compilation and test passes cleanly.
