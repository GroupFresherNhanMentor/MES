# Data Model: Authentication và Role-Based Access Control

## users

- `id` UUID primary key
- `username` normalized unique login name
- `password_hash` BCrypt
- `full_name`
- `active`
- `created_at`

## roles

- `id` UUID primary key
- `name` normalized unique role name
- `description`

Standard roles:

- `ADMIN`
- `WAREHOUSE_MANAGER`
- `PLANNER`
- `OPERATOR`
- `QC_INSPECTOR`
- `MAINTENANCE_ENGINEER`
- `FACTORY_MANAGER`
- `AUDITOR`

## user_roles

- Composite key `(user_id, role_id)`.
- Một user có thể có nhiều direct role.
- Thay thế role chạy trong một transaction.
- Foreign key role dùng `RESTRICT`; xóa user cascade các assignment.

## rbac_mutation_guard

Singleton row được khóa trước mutation có thể ảnh hưởng active ADMIN cuối cùng.

## permissions

- Giữ nguyên bảng và dữ liệu hiện có để tương thích schema.
- Không có CRUD permission trong phạm vi feature.
- Không được dùng làm nguồn quyết định authorization runtime.

## role_permissions

- Giữ nguyên quan hệ `(role_id, permission_id)` và dữ liệu hiện có.
- Không có API gán permission cho role.
- Không được đọc khi tạo Spring Security authorities.

## JWT

Cả access và refresh JWT có:

- `sub`: user UUID
- `username`
- `jti`
- `iss`, `aud`, `iat`, `exp`
- `token_type`: `access` hoặc `refresh`

Token không được lưu trong database.

## Final schema rule

Authorization chỉ dựa trên `roles` và `user_roles`. `permissions` và `role_permissions` được giữ lại vì tương thích dữ liệu, nhưng không tham gia luồng authorization.
