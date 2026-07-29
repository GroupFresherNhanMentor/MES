# Authentication and RBAC API Contract

## General

- Base path: `/api`
- Response envelope: `ApiResponse<T>`
- Public endpoints: `POST /api/auth/login`, `POST /api/auth/refresh`
- Other endpoints require a valid access JWT and the exact authority in `rbac-permission-matrix.md`
- Access lifetime: 15 minutes by default
- Refresh lifetime: 7 days by default
- Refresh is stateless and reusable until expiry

Error mapping:

| Status | Meaning |
|---|---|
| `400` | Validation/malformed request |
| `401` | Failed credentials, inactive user, invalid/expired/wrong-type JWT |
| `403` | Authenticated but current roles do not grant the required authority |
| `404` | User/role not found, or removed permission endpoint |
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

The response exposes only user ID, username and the two tokens. No profile,
role, permission, password or password hash is returned.

## POST `/api/auth/refresh`

Request:

```json
{"refreshToken":"<jwt>"}
```

Success `200` uses the same `TokenResponse` shape as login. Server validates signature, issuer, audience, expiration and `token_type=refresh`, reloads active user/current roles and returns a new pair. It does not persist, consume or revoke the submitted refresh JWT.

## User-role APIs

- `GET /api/users/{userId}/roles`
- `PUT /api/users/{userId}/roles`

PUT body:

```json
{"roleIds":["uuid"]}
```

Replacement is atomic, supports multiple roles/empty set and deduplicates IDs.

## Role APIs

- `GET /api/roles`
- `GET /api/roles/{id}`
- `POST /api/roles`
- `PUT /api/roles/{id}`
- `DELETE /api/roles/{id}`

`RoleDto.permissionNames` is read-only and derived from static role policy.

## Removed APIs

- `GET|POST /api/permissions`
- `GET|DELETE /api/permissions/{id}`
- `POST /api/roles/{id}/permissions`

The database tables remain for data compatibility, but these routes are not published.
