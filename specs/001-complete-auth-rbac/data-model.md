# Data Model: Authentication và role-based authorization

## Runtime data

### users

- `id` UUID primary key
- `username` unique
- `password_hash` BCrypt
- `full_name`
- `active`
- `created_at`

Auth đọc credential khi login và đọc identity/active state ở login, refresh và mỗi access request.

### roles

- `id` UUID primary key
- `name` normalized unique role name
- `description`

Role name là key của static `RolePermissionPolicy`.

### user_roles

- composite key `(user_id, role_id)`
- user có thể có nhiều direct roles
- replacement xóa tập cũ và insert tập mới trong một transaction

Đây là quan hệ RBAC duy nhất được runtime authorization đọc.

### rbac_mutation_guard

Singleton row được lock khi mutation có thể ảnh hưởng ADMIN cuối cùng. Guard kiểm tra còn ít nhất một active user có direct role `ADMIN`.

## JWT model

Access và refresh JWT có:

- `sub`: user UUID
- `username`: username tại lúc issue
- `jti`: UUID token ID
- `iss`, `aud`, `iat`, `exp`
- `token_type`: `access` hoặc `refresh`

Không có session ID và không có token record trong database.

## Static policy

`RolePermissionPolicy` giữ immutable mapping cho:

- `ADMIN`
- `WAREHOUSE_MANAGER`
- `PLANNER`
- `OPERATOR`
- `QC_INSPECTOR`
- `MAINTENANCE_ENGINEER`
- `FACTORY_MANAGER`
- `AUDITOR`

Authority hiệu lực là sorted distinct union của mọi role hiện tại.

## Legacy permission tables

Các bảng permission có sẵn từ schema nền được giữ nguyên để không làm mất dữ liệu, nhưng không được runtime auth đọc/ghi:

- `permissions`
- `role_permissions`

Migration auth/RBAC chưa phát hành không tạo bảng refresh session/token, login throttle hoặc security event.
