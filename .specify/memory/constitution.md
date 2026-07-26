# MES Constitution

## Architecture

This project strictly follows Clean Architecture. Full rules in `docs/rules/backend-architecture.md`.

**Layer dependency:** `presentation → application → domain ← infrastructure`

Each module has exactly 4 layers: `domain/`, `application/`, `infrastructure/`, `presentation/`.

## Core Principles

**I. Clean Architecture is Non-Negotiable**
Every new module replicates the same 4-layer structure. No shortcuts, no mixed-layer imports.
See: `docs/rules/backend-architecture.md`

**II. jOOQ Only for Database Access**
No Spring Data JPA. No EntityManager. No JPQL. All queries use `DSLContext`.
See: `docs/rules/backend-tech-stack.md`

**III. MapStruct Only for Mapping**
No manual field-by-field mapping. One `*DtoMapper` per entity, one `*RecordMapper` per jOOQ record.
See: `docs/rules/backend-tech-stack.md`

**IV. Exceptions from Service Layer**
Throw `AppException(HttpStatus, errorCode, message)` from service. Never from controller.
Each module has its own `*NotFoundException` in `application/exception/`.
See: `docs/rules/backend-api.md`

**V. Consistent API Responses**
Every endpoint returns `ResponseEntity<ApiResponse<T>>`. Use `ApiResponse.success()` / `.error()`.
See: `docs/rules/backend-api.md`

**VI. Tests Are Mandatory — Not Optional**
Every feature planned with SpecKit must include test tasks. Tests are written first (fail before implementation).
- Unit tests: `@ExtendWith(MockitoExtension.class)` — service logic, all exception branches
- Integration tests: `extends AbstractIntegrationTest` — full HTTP → DB flow, 401 unauthenticated
- Concurrency tests: `CountDownLatch` + `ExecutorService` — race conditions, double-writes, quantity integrity

The `/speckit-tasks` command must always produce test tasks for each user story. Never mark tests as optional.
Any feature touching shared mutable state (stock quantity, work order status, reservations, assignments)
**must** include concurrency test cases inside `{Entity}IntegrationTest` — no separate file needed.
See: `docs/rules/backend-testing.md`

## Rules Reference

| Topic | File |
|-------|------|
| Layer structure & dependency rules | `docs/rules/backend-architecture.md` |
| Naming conventions | `docs/rules/backend-naming.md` |
| Code skeletons per layer | `docs/rules/backend-patterns.md` |
| Tech stack & forbidden approaches | `docs/rules/backend-tech-stack.md` |
| REST & API design | `docs/rules/backend-api.md` |
| Testing patterns & pyramid | `docs/rules/backend-testing.md` |

## Governance

This constitution and the rules in `docs/rules/` are the source of truth. All SpecKit specs and
plans must conform to these rules. Use `/speckit-specify` to start any new feature.

**Version**: 1.0.0 | **Ratified**: 2026-07-27 | **Stack**: Java 25 · Spring Boot 4.1.0 · jOOQ 3.21 · PostgreSQL 18
