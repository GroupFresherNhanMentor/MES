# Quickstart: Verify Role-Based Authentication

## Automated

```powershell
cd be
.\mvnw.cmd -q test "-Djooq.codegen.skip=true"
```

## Swagger

1. Start PostgreSQL and backend with the `dev` profile.
2. Open `/swagger-ui/index.html`.
3. Login with an active account.
4. Copy `accessToken`, click **Authorize**, and enter the token.
5. Verify ADMIN can call user and role management APIs.
6. Verify a non-ADMIN role receives `403` from user management APIs.
7. Verify a role-specific account can call only endpoint groups listed in `contracts/rbac-role-matrix.md`.
8. Refresh using the refresh JWT and verify the returned access JWT works.

## Database

After all Flyway migrations, auth uses:

- `users`
- `roles`
- `user_roles`
- `rbac_mutation_guard`
