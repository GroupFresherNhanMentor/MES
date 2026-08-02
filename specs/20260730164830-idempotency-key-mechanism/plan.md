# Implementation Plan: Idempotency Key Mechanism

**Branch**: `20260730164830-idempotency-key-mechanism` | **Date**: 2026-07-30 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `/specs/20260730164830-idempotency-key-mechanism/spec.md`

## Summary

Implement a transparent, DB-backed idempotency key mechanism in Spring Boot using jOOQ, Servlet `OncePerRequestFilter`, and PostgreSQL database table `idempotency_keys`. The mechanism intercepts requests with an `X-Idempotency-Key` header, guarantees single execution of state-mutating operations per authenticated user / guest client, returns cached 2xx responses on duplicate submissions in <50ms, blocks in-progress duplicates with HTTP 409 Conflict, detects payload tampering with SHA-256 request body hashing (HTTP 400 Bad Request), and automatically purges 1-day old records via a `@Scheduled` background job.

## Technical Context

**Language/Version**: Java 25

**Primary Dependencies**: Spring Boot 4.1.0, jOOQ 3.21 (`DSLContext`), Spring Security

**Storage**: PostgreSQL 18 (`idempotency_keys` table with unique index `idx_idemp_unauthen`)

**Testing**: JUnit 5, `@ExtendWith(MockitoExtension.class)`, `AbstractIntegrationTest`, `CountDownLatch` concurrency tests

**Target Platform**: Linux server / JVM

**Project Type**: Web application REST API backend (`be/src/main/java/fpt/qn/mes/`)

**Performance Goals**: <50ms response time for cached idempotency requests without touching domain services or DB locks.

**Constraints**: Clean Architecture layers, jOOQ-only DB access, MapStruct DTO mapping, no Spring Data JPA.

**Scale/Scope**: All mutation endpoints (POST/PUT/PATCH) configured for idempotency key verification.

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

- [x] **Clean Architecture Layering**: All code follows `domain/`, `application/`, `infrastructure/`, `presentation/` in `common/idempotency/` package.
- [x] **jOOQ Storage Only**: Persistence uses `IdempotencyKeyPersistenceAdapter` extending `BaseRepository<IdempotencyKeysRecord>` with `DSLContext`.
- [x] **MapStruct Only**: Mappings use MapStruct mappers (`IdempotencyKeyRecordMapper`, `IdempotencyKeyDtoMapper`).
- [x] **Exception Pattern**: Domain exceptions extend `AppException` (e.g. `IdempotencyConflictException` HTTP 409, `IdempotencyPayloadMismatchException` HTTP 400).
- [x] **API Response Format**: Returns standard `ResponseEntity<ApiResponse<T>>`.
- [x] **Mandatory Testing**: Full unit, integration, and `CountDownLatch` concurrency test coverage.

## Project Structure

### Documentation (this feature)

```text
specs/20260730164830-idempotency-key-mechanism/
├── plan.md              # This file
├── research.md          # Phase 0 output
├── data-model.md        # Phase 1 output
├── quickstart.md        # Phase 1 output
└── contracts/           # Phase 1 output
    └── idempotency-api.md
```

### Source Code Layout

```text
be/
├── src/main/resources/db/migration/
│   └── V20260730193000__create_idempotency_keys.sql
└── src/main/java/fpt/qn/mes/
    └── common/
        └── idempotency/
            ├── domain/
            │   ├── entities/
            │   │   └── IdempotencyKey.java
            │   └── repository/
            │       └── IdempotencyKeyRepository.java
            ├── application/
            │   ├── service/
            │   │   ├── IdempotencyService.java
            │   │   └── IdempotencyCleanupScheduler.java
            │   ├── exception/
            │   │   ├── IdempotencyConflictException.java
            │   │   └── IdempotencyPayloadMismatchException.java
            │   └── mapper/
            │       └── IdempotencyKeyRecordMapper.java
            ├── infrastructure/
            │   ├── filter/
            │   │   └── IdempotencyFilter.java
            │   └── persistence/
            │       └── IdempotencyKeyPersistenceAdapter.java
            └── presentation/
```

**Structure Decision**: Standard 4-layer Clean Architecture module inside `common/idempotency/`.

## Complexity Tracking

> **Fill ONLY if Constitution Check has violations that must be justified**

*No constitution violations. Architecture adheres 100% to project constitution.*
