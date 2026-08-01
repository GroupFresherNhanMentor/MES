# Tasks: Authentication và Role-Based Access Control

## Authentication

- [X] Implement BCrypt login with a generic `401` failure.
- [X] Implement stateless access/refresh JWT with token-type validation.
- [X] Return only `userId`, `username`, `accessToken`, `refreshToken`.
- [X] Reload active user state for login, refresh and protected requests.

## Role authorization

- [X] Load direct roles in one jOOQ query.
- [X] Emit only `ROLE_<ROLE_NAME>` Spring Security authorities.
- [X] Define the ADMIN expression used by auth/user/role management.
- [X] Protect only user, role and user-role controllers with ADMIN.
- [X] Protect actuator endpoints with ADMIN.
- [X] Add a contract test covering auth/user/role API operations.

## User and role management

- [X] Keep user CRUD and multi-role replacement.
- [X] Keep role CRUD and return role-only DTOs.
- [X] Preserve final-active-ADMIN guard.
- [X] Keep role seed insert-only and idempotent.

## Database

- [X] Preserve existing `permissions` and `role_permissions` tables.
- [X] Remove unused seed files.
- [X] Add migration integration assertions that permission tables remain present.

## Verification

- [X] Update unit tests for role-only auth.
- [X] Scope endpoint authorization integration tests to auth/user/role.
- [X] Update SRS, API reference and contracts.
- [X] Run the final full Maven test suite.
