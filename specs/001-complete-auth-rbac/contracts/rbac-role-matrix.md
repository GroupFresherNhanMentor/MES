# Auth-Scope Role Matrix

This feature owns authorization only for authentication, user management, role
management and user-role assignment.

| Endpoint family | Access |
|---|---|
| `POST /api/auth/login` | Public |
| `POST /api/auth/refresh` | Public |
| `/api/users/**` user management | `ADMIN` |
| `/api/roles/**` role management | `ADMIN` |
| `/api/users/{userId}/roles` | `ADMIN` |
| `/actuator/**` except health | `ADMIN` |

Business controllers are outside this feature's ownership. Their maintainers
define and test their own `@PreAuthorize` rules. The auth module only provides
authenticated principals whose authorities use the `ROLE_<ROLE_NAME>` format.
