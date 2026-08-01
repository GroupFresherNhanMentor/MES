# Implementation Plan: Authentication và Role-Based Access Control

## Scope

Backend only. Giữ nguyên URL user/role/auth hiện có, chuyển authorization sang role-only và giữ refresh JWT stateless. Không khai báo role cho controller nghiệp vụ thuộc module của thành viên khác.

## Architecture

- `auth/application`: use case, role access expressions và auth services.
- `auth/domain`: `Role`, repository contracts.
- `auth/infrastructure/security`: JWT encoder/decoder, authentication converter.
- `auth/infrastructure/persistence`: tải user + direct roles bằng jOOQ.
- `auth/presentation`: login, refresh và role management controllers.
- `user`, `role` và `user-role` controllers dùng `@PreAuthorize(RoleAccess.ADMIN_ONLY)`.
- Chủ sở hữu từng module nghiệp vụ tự khai báo `@PreAuthorize` và ma trận role của module đó.

## Runtime Flow

1. Login xác thực BCrypt và active state.
2. Server phát access/refresh JWT chỉ chứa identity claims.
3. Mỗi access request xác thực JWT rồi dùng `sub` tải user + direct roles.
4. Converter tạo `ROLE_<ROLE_NAME>` authorities.
5. Method security kiểm tra `ADMIN` cho API quản lý user, role và gán role.
6. Refresh kiểm tra refresh JWT, tải lại user hiện tại và phát cặp token mới.

## Database

- Runtime auth dùng `users`, `roles`, `user_roles`.
- `rbac_mutation_guard` bảo vệ ADMIN cuối cùng.
- Giữ nguyên `permissions` và `role_permissions` cùng dữ liệu hiện có để tương thích schema; runtime không đọc hai bảng này.
- Không sửa migration đã áp dụng.

## API Contract

- Token response: `userId`, `username`, `accessToken`, `refreshToken`.
- Role response: `id`, `name`, `description`.
- User-role replacement: `PUT /api/users/{userId}/roles`.
- Auth-scope role matrix: `contracts/rbac-role-matrix.md`.

## Verification

- Unit: auth service, authorization resolver, role service, user-role service, JWT provider.
- Integration: login/refresh, role reload, active reload, 401/403, user/role CRUD, migration and seed.
- Contract: 15 operation thuộc auth/user/role được phân loại public hoặc ADMIN-protected.
- Full backend suite chạy bằng Maven + Testcontainers.
