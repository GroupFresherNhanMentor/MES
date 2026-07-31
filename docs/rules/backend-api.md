# Backend API Design Rules

## API Endpoint Reference (`docs/api-endpoints.md`)

`docs/api-endpoints.md` is an **advisory reference, not the source of truth**.

- The real source of truth is the controller code and DTOs in the `be/` source tree.
- During planning (spec/plan phase): consult `api-endpoints.md` as a starting point, but verify against actual controllers before designing new endpoints.
- During coding: if your implementation adds, removes, renames, or changes request/response shape of any endpoint — **update `api-endpoints.md` immediately** to reflect the change.
- Never trust `api-endpoints.md` blindly — always cross-check against the controller `@*Mapping` annotations and DTO classes when in doubt.

---

## Response Envelope

Every endpoint returns `ResponseEntity<ApiResponse<T>>`. Use the static factory methods:

```java
// Service returns data → pass data + message (two-arg overload)
ResponseEntity.ok(ApiResponse.success(data, "OK"))
ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(data, "Created"))

// Service returns void → pass message only (one-arg overload); return type is ApiResponse<Void>
ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Created"))
ResponseEntity.ok(ApiResponse.success("Deleted"))
ResponseEntity.ok(ApiResponse.success("OK"))

// Error (thrown as exception, handled by GlobalExceptionHandler)
throw new AppException(404, ErrorCode.NOT_FOUND, "BOM not found: " + id)
```

### Void-return controller rule

When the use case method is `void`, the controller return type must be `ResponseEntity<ApiResponse<Void>>` and the body uses the **one-arg** `ApiResponse.success(String message)`. **Never pass `null` as the data argument.**

```java
// FORBIDDEN — passing null explicitly
return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(null, "Created"));

// REQUIRED — use the one-arg overload; return type is ApiResponse<Void>
public ResponseEntity<ApiResponse<Void>> createFoo(@Valid @RequestBody CreateFooRequest request) {
    fooUseCase.createFoo(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Created"));
}
```

`ApiResponse<T>` shape:
```json
{
  "success": true,
  "data": { ... },
  "message": "OK",
  "timestamp": "2026-07-27T12:00:00"
}
```

Error shape:
```json
{
  "success": false,
  "errorCode": "BOM_NOT_FOUND",
  "message": "BOM not found: 550e8400-...",
  "timestamp": "2026-07-27T12:00:00"
}
```

## HTTP Status Codes

| Scenario | Status |
|----------|--------|
| Successful read | `200 OK` |
| Successful create | `201 Created` |
| Successful delete / update with no body | `200 OK` |
| Validation error | `400 Bad Request` |
| Unauthenticated | `401 Unauthorized` |
| Forbidden | `403 Forbidden` |
| Resource not found | `404 Not Found` |
| Conflict (duplicate) | `409 Conflict` |
| Server error | `500 Internal Server Error` |

## URL Conventions

```
GET    /api/{resources}              list (paginated unless approved legacy catalog)
GET    /api/{resources}/{id}         single
POST   /api/{resources}              create
PUT    /api/{resources}/{id}         full update
PATCH  /api/{resources}/{id}         partial update
DELETE /api/{resources}/{id}         delete
POST   /api/{resources}/{id}/{sub}   sub-resource action
```

- Resource names: **plural, lowercase, kebab-case** (`/api/work-orders`, `/api/boms`)
- IDs: always `UUID` in path variables
- Filters: query params (`?page=0&size=20&status=ACTIVE`)

## Pagination

Request params: `page` (0-indexed, default `0`), `size` (default `20`)

Response uses `PageResponse<T>`:
```json
{
  "success": true,
  "data": {
    "content": [...],
    "page": 0,
    "size": 20,
    "totalElements": 150,
    "totalPages": 8
  }
}
```

All new list endpoints must be paginated. A legacy endpoint may retain an
unpaginated `List<T>` response only when every condition below is met:

- an existing public contract explicitly requires the response shape to remain
  unchanged;
- the endpoint returns bounded configuration/catalog data rather than an
  operational dataset;
- the feature plan records the exception in `Complexity Tracking`;
- the query has deterministic database ordering;
- an API compatibility test locks the existing response shape.

`FactoryFlow_SRS.md` classifies roles as bounded RBAC configuration data,
while `FactoryFlow_Manufacturing_Operations_Platform.md` requires pagination
for large lists. The approved legacy exception is therefore limited to:

- `GET /api/roles`

This exception must not be copied to new endpoints.

## Validation

Request DTOs use Jakarta Bean Validation annotations:

```java
public class CreateBomRequest {
    @NotNull UUID finishedProductId;
    @NotNull @Min(1) Integer version;
    @NotNull UUID bomStatusId;
}
```

Controllers annotate with `@Valid @RequestBody`. Validation errors are caught by `GlobalExceptionHandler` and returned as `400` with field-level error details.

## Error Codes

Use the `ErrorCode` enum from `fpt.qn.mes.common.exception.ErrorCode`. These are generic
HTTP-semantic categories — the specific context is carried in the `message` field.

| `ErrorCode` | HTTP Status | When to use |
|-------------|-------------|-------------|
| `NOT_FOUND` | 404 | Entity does not exist (all `*NotFoundException`) |
| `INVALID_INPUT` | 400 | Validation errors, domain rule violations, bad arguments |
| `UNAUTHORIZED` | 401 | Invalid/expired JWT token |
| `FORBIDDEN` | 403 | Authenticated but does not have an allowed role |
| `CONFLICT` | 409 | Duplicate creation (e.g., username already exists) |
| `INTERNAL_SERVER_ERROR` | 500 | Unexpected errors |

**Exception pattern:**
```java
// Module-specific not-found exception
public class BomNotFoundException extends AppException {
    public BomNotFoundException(String message) {
        super(404, ErrorCode.NOT_FOUND, message);
    }
}

// Conflict exception
public class UsernameAlreadyExistsException extends AppException {
    public UsernameAlreadyExistsException(String message) {
        super(409, ErrorCode.CONFLICT, message);
    }
}
```

The `message` carries entity-specific context: `"BOM not found: " + id`
