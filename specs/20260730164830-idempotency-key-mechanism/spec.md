# Feature Specification: Idempotency Key Mechanism

**Feature Branch**: `20260730164830-idempotency-key-mechanism`

**Created**: 2026-07-30

**Status**: Draft

**Input**: User description: "help me create idempotency key (in db have a table handle that) mechanism"

## Clarifications

### Session 2026-07-30
- Q: What exact DB schema and index constraint should be used for idempotency keys, and how should expired keys be purged? → A: The migration script MUST include `DROP TABLE IF EXISTS idempotency_keys CASCADE;` prior to recreating table schema `idempotency_keys` with `id UUID PRIMARY KEY`, `key VARCHAR(255) NOT NULL`, `user_id UUID NULL`, `client_identifier VARCHAR(255) NOT NULL`, `request_path VARCHAR(255) NOT NULL`, `request_hash VARCHAR(64) NOT NULL`, `status_code INTEGER NULL`, `response_body TEXT NULL`, `created_at TIMESTAMPTZ DEFAULT NOW()`, `updated_at TIMESTAMPTZ DEFAULT NOW()`, unique index `idx_idemp_unauthen ON idempotency_keys(key, COALESCE(user_id::text, client_identifier))`, and a Spring scheduled job deleting rows older than 1 day (`created_at < NOW() - INTERVAL '1 day'`).
- Q: How is JWT user authentication resolved in the filter chain for idempotency evaluation? → A: `IdempotencyFilter` MUST be ordered AFTER `BearerTokenAuthenticationFilter` in `SecurityConfig` (`http.addFilterAfter(idempotencyFilter, BearerTokenAuthenticationFilter.class)`). When executed, the filter inspects `SecurityContextHolder.getContext().getAuthentication()`; if `auth.getPrincipal()` is an instance of `AppUserPrincipal`, `user_id` is resolved via `principal.getId()`. If unauthenticated or guest, `user_id` remains `null` and `client_identifier` is computed as Remote IP + User-Agent header.

---

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Prevent Duplicate Execution of Mutation Requests (Priority: P1) 🎯 MVP

As an API client or frontend system performing state-mutating requests (such as stock transfers, stock-in, work order creations, or payments), I want to include an `X-Idempotency-Key` header with my request so that if network issues cause retries or duplicate submissions, the server processes the operation exactly once and returns the cached response without re-executing business logic.

**Why this priority**: Preventing duplicate state mutations (such as double stock deductions or duplicate movements) is critical for system data integrity and reliability.

**Independent Test**: Send two identical POST requests with the same `X-Idempotency-Key` header. The first request processes and returns a 200/201 response. The second request returns the identical cached status code and body without executing underlying service logic or mutating state twice.

**Acceptance Scenarios**:

1. **Given** a new unique `X-Idempotency-Key` header in a POST request, **When** the request succeeds, **Then** the system executes the business logic, stores the response (status code and payload body) linked to the key in the database table, and returns the response.
2. **Given** a request sent with an `X-Idempotency-Key` that was already processed successfully, **When** the request arrives, **Then** the system bypasses business logic execution and immediately returns the cached response status code and body from the database.
3. **Given** a request with an `X-Idempotency-Key` currently being processed by another concurrent request, **When** the second request arrives, **Then** the system rejects the concurrent duplicate request with HTTP 409 Conflict (or waits until completion).

---

### User Story 2 - Idempotency Key Expiration and Cleanup (Priority: P2)

As a system administrator, I want idempotency key records in the database to expire after a configurable duration (e.g. 24 hours) so that old key records do not accumulate indefinitely.

**Why this priority**: Retaining idempotency records forever would continuously grow the database size and slow down index lookups.

**Independent Test**: Submit a request with an idempotency key, verify it is cached, wait/mock time past the TTL (expiration), and submit the same key again to verify it is treated as a new valid request.

**Acceptance Scenarios**:

1. **Given** an expired idempotency key record in the database, **When** a request with the same key arrives after the expiration window, **Then** the key is treated as new, allowing request execution.

---

### Edge Cases

- What happens when a request fails with a 4xx validation error or 5xx server error? Is the failed result cached or cleared? (Assumption: Only successful 2xx responses are cached for idempotency; errors permit retries).
- What happens when two requests arrive simultaneously with the exact same `X-Idempotency-Key` before the first completes? (Handling: Unique index constraint on `idempotency_key` ensures atomic locking / conflict detection).
- What happens when the request body differs for the same `X-Idempotency-Key`? (Handling: Hash request payload with key; return 400 Bad Request if key is reused with a different payload).

---

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: System MUST intercept incoming HTTP mutation requests containing an `X-Idempotency-Key` header via `IdempotencyFilter` positioned after `BearerTokenAuthenticationFilter` in the SecurityFilterChain (`http.addFilterAfter(idempotencyFilter, BearerTokenAuthenticationFilter.class)`). The filter MUST resolve `user_id` from `SecurityContextHolder` (`AppUserPrincipal.getId()`) if authenticated, or compute `client_identifier` (IP address + User-Agent) if unauthenticated or guest.
- **FR-002**: Database migration script MUST issue `DROP TABLE IF EXISTS idempotency_keys CASCADE;` before creating the dedicated database table (`idempotency_keys`) storing fields: `id` (UUID), `key` (VARCHAR(255)), `user_id` (UUID NULL, for logged-in or guest users), `client_identifier` (VARCHAR(255) NOT NULL, IP + UserAgent or Device Fingerprint / Guest Email), `request_path` (VARCHAR(255)), `request_method` (VARCHAR(10) NOT NULL), `request_hash` (VARCHAR(64)), `status_code` (INTEGER NULL, NULL=running/in-progress, NOT NULL=completed), `response_body` (TEXT NULL), `created_at` (TIMESTAMPTZ), and `updated_at` (TIMESTAMPTZ).
- **FR-003**: System MUST enforce a unique index constraint `idx_idemp_unauthen ON idempotency_keys(key, COALESCE(user_id::text, client_identifier))` in the database to prevent concurrent duplicate key processing per authenticated user or guest client.
- **FR-004**: If an `X-Idempotency-Key` record exists with a non-null `status_code`, the system MUST immediately return the recorded HTTP status code and `response_body` without re-executing business logic.
- **FR-005**: If an `X-Idempotency-Key` record exists with `status_code = NULL` (in-progress execution), incoming duplicate requests from the same user/client MUST be rejected with HTTP 409 Conflict.
- **FR-006**: System MUST compute a request payload hash (SHA-256) for the request body (`request_hash`) and verify that retried requests using the same `X-Idempotency-Key` match the original payload. If the payload differs, the system MUST return HTTP 400 Bad Request.
- **FR-007**: A background scheduled cleanup job MUST run periodically to delete all `idempotency_keys` table rows where `created_at` is older than 1 day (`created_at < NOW() - INTERVAL '1 day'`).

---

### Key Entities *(include if feature involves data)*

- **IdempotencyKey**:
  - `id`: Unique identifier (UUID generated application-side, e.g. UuidV7).
  - `key`: Unique string provided by client in `X-Idempotency-Key` header (`VARCHAR(255)`).
  - `userId`: User ID (`UUID NULL`, nullable for guests).
  - `clientIdentifier`: Client identifier (`VARCHAR(255) NOT NULL`, IP + UserAgent or Device Fingerprint / Guest Email).
  - `requestPath`: Target API URI path (`VARCHAR(255)`).
  - `requestMethod`: HTTP method (`VARCHAR(10) NOT NULL`, e.g. POST, PUT, PATCH).
  - `requestHash`: SHA-256 hash string of request payload body (`VARCHAR(64)`).
  - `statusCode`: HTTP status code returned (`INTEGER NULL`, `NULL` = in progress, `NOT NULL` = completed).
  - `responseBody`: JSON response body string (`TEXT NULL`).
  - `createdAt`: Creation timestamp (`TIMESTAMPTZ`).
  - `updatedAt`: Last update timestamp (`TIMESTAMPTZ`).

---

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Duplicate API requests with identical `X-Idempotency-Key` headers return cached responses in under 50ms without hitting domain business logic or database transaction locks.
- **SC-002**: 100% prevention of duplicate state mutations (e.g. double stock transfers or duplicate movement records) during concurrent duplicate client requests.
- **SC-003**: Zero database deadlocks or data inconsistencies under high-concurrency duplicate submission testing.

---

## Assumptions

- Idempotency key evaluation applies primarily to state-altering operations (POST, PUT, PATCH).
- The default time-to-live (TTL) for an idempotency key is 24 hours.
- If a request fails with an unhandled 5xx internal server error, the idempotency key lock is released so the client can retry.
