# Research: Idempotency Key Mechanism

## Decisions & Rationale

### 1. HTTP Interception Layer
- **Decision**: Implement `IdempotencyFilter` extending Spring's `OncePerRequestFilter` with `ContentCachingRequestWrapper` and `ContentCachingResponseWrapper`.
- **Rationale**: `OncePerRequestFilter` guarantees single execution per HTTP request, runs before Spring MVC controllers, and allows reading the request body (for SHA-256 payload hashing) and capturing the controller response status and JSON payload body.
- **Alternatives Considered**: 
  - Spring AOP Aspect (`@Idempotent` annotation on controllers): Rejected because it requires manually annotating every controller method and complicates request/response stream body caching.
  - HandlerInterceptor: Rejected because reading and caching the raw HTTP response output stream is cumbersome in HandlerInterceptor compared to Servlet Filter wrappers.

### 2. Database Persistence & Race Condition Handling
- **Decision**: PostgreSQL `idempotency_keys` table with jOOQ persistence adapter (`IdempotencyKeyPersistenceAdapter`) extending `BaseRepository<IdempotencyKeysRecord>`. Enforce composite unique index `idx_idemp_unauthen ON idempotency_keys(key, COALESCE(user_id::text, client_identifier))`.
- **Rationale**: Atomic DB insert under the unique index provides strong concurrency protection. If two concurrent requests arrive with the same key and user/client identifier, PostgreSQL throws a unique key violation (DataIntegrityViolationException), allowing the filter to immediately catch it and return HTTP 409 Conflict.
- **Alternatives Considered**: 
  - Redis distributed lock (`redisson` / `SETNX`): Rejected because the project constitution specifies PostgreSQL + jOOQ as the storage standard without adding Redis infrastructure dependencies.

### 3. Expiration & Cleanup Strategy
- **Decision**: Spring `@Scheduled` job (`IdempotencyCleanupScheduler`) running daily/hourly executing `DELETE FROM idempotency_keys WHERE created_at < NOW() - INTERVAL '1 day'`.
- **Rationale**: Clean, lightweight, self-contained within Spring Boot without requiring external cron schedulers or pg_cron extensions.

---

## Technical Context Summary

- **Language/Version**: Java 25
- **Framework**: Spring Boot 4.1.0
- **Database/ORM**: PostgreSQL 18, jOOQ 3.21 (`DSLContext`)
- **Security Context**: `SecurityContextHolder` (AppUserPrincipal / AppUser)
- **Testing**: JUnit 5, `@ExtendWith(MockitoExtension.class)`, `AbstractIntegrationTest`, `CountDownLatch` concurrency tests
