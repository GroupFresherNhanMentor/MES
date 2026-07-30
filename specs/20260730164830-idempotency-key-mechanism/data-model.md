# Data Model: Idempotency Key Mechanism

## Database Schema (PostgreSQL)

### Migration Script: `be/src/main/resources/db/migration/V20260730193000__create_idempotency_keys.sql`

```sql
DROP TABLE IF EXISTS idempotency_keys CASCADE;

CREATE TABLE idempotency_keys (
    id                 UUID PRIMARY KEY,
    key                VARCHAR(255) NOT NULL,
    user_id            UUID NULL,
    client_identifier  VARCHAR(255) NOT NULL,
    request_path       VARCHAR(255) NOT NULL,
    request_method     VARCHAR(10) NOT NULL,
    request_hash       VARCHAR(64) NOT NULL,
    status_code        INTEGER NULL,
    response_body      TEXT NULL,
    created_at         TIMESTAMPTZ DEFAULT NOW(),
    updated_at         TIMESTAMPTZ DEFAULT NOW()
);

CREATE UNIQUE INDEX idx_idemp_unauthen 
ON idempotency_keys(key, COALESCE(user_id::text, client_identifier));
```

---

## Domain Entity: `IdempotencyKey`

Located at: `fpt.qn.mes.common.idempotency.domain.entities.IdempotencyKey` (or module specific)

### Fields

| Field | Type | Constraint | Description |
|-------|------|------------|-------------|
| `id` | `UUID` | Primary Key | Application-generated UUID (UuidV7) |
| `key` | `String` | NOT NULL, Max 255 | Value passed in `X-Idempotency-Key` header |
| `userId` | `UUID` | Nullable | Authenticated user ID (or null for guests) |
| `clientIdentifier` | `String` | NOT NULL, Max 255 | IP address + User-Agent string / device fingerprint |
| `requestPath` | `String` | NOT NULL, Max 255 | HTTP request URI path (e.g. `/api/stock-in`) |
| `requestMethod` | `String` | NOT NULL, Max 10 | HTTP request method (e.g. `POST`, `PUT`, `PATCH`) |
| `requestHash` | `String` | NOT NULL, 64 chars | SHA-256 hex string of the request payload body |
| `statusCode` | `Integer` | Nullable | HTTP status code (NULL = processing, NOT NULL = finished) |
| `responseBody` | `String` | Nullable | Cached JSON response body text |
| `createdAt` | `OffsetDateTime` | NOT NULL | Creation timestamp |
| `updatedAt` | `Instant` | NOT NULL | Last modification timestamp |

---

## Processing State Lifecycle

```
[ Incoming Request with X-Idempotency-Key ]
                   │
                   ▼
  Query idempotency_keys by key + user/client
                   │
         ┌─────────┴─────────┐
         ▼                   ▼
    Record Found      Record Not Found
         │                   │
  ┌──────┴──────┐            ▼
  ▼             ▼       Insert record with status_code = NULL
status_code   status_code    (claims lock via idx_idemp_unauthen)
NOT NULL       == NULL       │
  │             │            ▼
  ▼             ▼       Execute Controller / Service Logic
Return        Return         │
Cached 2xx   409 Conflict    ▼
Response                Update record with status_code & response_body
```
