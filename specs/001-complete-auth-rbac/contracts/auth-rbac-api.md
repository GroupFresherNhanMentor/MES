# Authentication and Role-Based Access API Contract

## General

- Base path: `/api`
- Envelope: `ApiResponse<T>`
- Public: `POST /api/auth/login`, `POST /api/auth/refresh`
- User, role and user-role management endpoints require a valid access JWT with `ADMIN`.
- Business endpoints require a valid access JWT; each owning module defines its own role rule.
- Access lifetime: 15 minutes by default.
- Refresh lifetime: 7 days by default.
- Refresh is stateless and reusable until expiry.

| Status | Meaning |
|---|---|
| `400` | Validation or malformed request |
| `401` | Failed credentials, inactive user, invalid/expired/wrong-type JWT |
| `403` | Authenticated but current roles are not allowed |
| `404` | User or role not found |
| `409` | Duplicate, assigned-role deletion or final-ADMIN invariant |

## POST `/api/auth/login`

Request:

```json
{"username":"admin","password":"secret"}
```

Success `200`:

```json
{
  "success": true,
  "data": {
    "userId": "uuid",
    "username": "admin",
    "accessToken": "<jwt>",
    "refreshToken": "<jwt>"
  },
  "message": "Login successful",
  "timestamp": "..."
}
```

## POST `/api/auth/refresh`

Request:

```json
{"refreshToken":"<jwt>"}
```

Success `200` uses the same response data shape as login.

## User-role APIs

- `GET /api/users/{userId}/roles` — ADMIN
- `PUT /api/users/{userId}/roles` — ADMIN

PUT body:

```json
{"roleIds":["uuid"]}
```

Replacement is atomic, supports multiple roles or an empty set, and deduplicates IDs.

## Role APIs

All require ADMIN:

- `GET /api/roles`
- `GET /api/roles/{id}`
- `POST /api/roles`
- `PUT /api/roles/{id}`
- `DELETE /api/roles/{id}`

Role response:

```json
{"id":"uuid","name":"PLANNER","description":"Plans production"}
```
