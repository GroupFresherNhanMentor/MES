# Implementation Plan: Authentication và role-based authorization

**Branch**: `feature/auth-rbac-stateless-jwt` | **Updated**: 2026-07-29
**Scope**: Backend only

## Summary

Hoàn thiện login và refresh bằng Spring Security JWT. Refresh JWT stateless theo luồng của ProjectManagementSystem: decode/validate, tải lại user, kiểm tra active và cấp access/refresh mới. Không lưu refresh token hay refresh session.

Authorization tải direct roles của user ở mỗi request. `RolePermissionPolicy` là `Map<Role, Set<Permission>>` tĩnh theo SRS; Spring authorities được suy ra từ map để giữ các `@PreAuthorize("hasAuthority('RESOURCE_ACTION')")` hiện có. Permission database không điều khiển runtime.

Giữ CRUD user/role và multi-role assignment. Loại permission CRUD, role-permission assignment, login throttle, trusted proxy/source address và security-event runtime.

## Technical Context

- Java 25, Spring Boot 4.1, Spring Security OAuth2 Resource Server
- HS256 JWT; issuer/audience/type/expiry validators
- PostgreSQL 18 + jOOQ
- Clean Architecture theo `AGENTS.md` và `docs/rules`
- JUnit 5, Mockito, Spring integration test, Testcontainers

## Architecture

### Authentication

1. Login tìm credential theo username và luôn chạy BCrypt match (dummy hash nếu user không tồn tại).
2. Active user được resolve cùng direct roles.
3. `RolePermissionPolicy` sinh authorities; response chứa profile, roles, permissions.
4. `JwtTokenProvider` phát access/refresh có `sub=userId`, `username`, `jti`, `iss`, `aud`, `iat`, `exp`, `token_type`.
5. Refresh validate JWT loại `refresh`, resolve lại active user bằng `sub`, rồi phát cặp mới.

### Authorization

1. Resource Server chỉ nhận JWT loại `access`.
2. Converter parse `sub`, resolve user + roles bằng một query.
3. Authorities gồm `ROLE_<role>` và authorities tĩnh suy ra từ mọi role.
4. Inactive/missing user làm access token bị coi là invalid.

### RBAC management

- User có nhiều role; replacement nguyên tử và loại ID trùng.
- ADMIN guard dựa vào active user có direct role `ADMIN`.
- Role CRUD giữ nguyên; `permissionNames` read-only từ static policy.
- Không có `PermissionController` hoặc API `/roles/{id}/permissions`.

## Database Strategy

- Migration auth/RBAC chưa phát hành không tạo `refresh_sessions`, `refresh_tokens`, `login_throttles` hoặc `security_events`.
- Không drop hoặc thay đổi dữ liệu permission/role-permission có sẵn từ schema nền.
- Runtime không đọc/ghi `permissions` hoặc `role_permissions`.
- `RoleDataSeeder` chỉ seed roles bằng `ON CONFLICT DO NOTHING`.

## API Contract

- Public: `POST /api/auth/login`, `POST /api/auth/refresh`.
- 89 business/RBAC handlers còn lại yêu cầu access JWT + exact authority.
- Tổng số `/api` handlers: 91.
- Không còn permission CRUD và role-permission assignment.

## Verification

- Unit: login/refresh, JWT claims/type, static multi-role union, inactive/no-role user, ADMIN guard, role/user-role services.
- Contract: OpenAPI có 91 operations; reflection catalog có 89 protected handlers; mọi authority nằm trong ADMIN policy.
- Integration: stateless refresh reuse + concurrent reuse, token purpose, active-state/role reload, DB role-permission ignored, CRUD user/role, removed endpoints, no sensitive-data leak.
- Performance: login/refresh p95 dưới 2 giây và authorization snapshot một query.
- Final command: `.\mvnw.cmd verify "-Djooq.codegen.skip=true"`; performance chạy riêng bằng profile `auth-performance`.

## Complexity Tracking

- Giữ `permissionNames` trong `RoleDto` để mô tả policy tĩnh; `TokenResponse` chỉ trả `userId`, `username`, `accessToken`, `refreshToken`.
- Giữ dữ liệu permission/role-permission có sẵn dù runtime không sử dụng.
