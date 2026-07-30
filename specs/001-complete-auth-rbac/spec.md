# Feature Specification: Authentication và Role-Based Access Control

**Feature Branch**: `feature/auth-rbac-stateless-jwt`
**Created**: 2026-07-28
**Updated**: 2026-07-30
**Status**: Implemented

## Clarifications

- Backend dùng Spring Security OAuth2 Resource Server với JWT.
- Access JWT hết hạn sau 15 phút; refresh JWT hết hạn sau 7 ngày theo cấu hình mặc định.
- Refresh hoàn toàn stateless. Token hợp lệ có thể dùng lại đến khi hết hạn và mỗi lần refresh cấp một cặp token mới.
- Login/refresh chỉ trả `userId`, `username`, `accessToken`, `refreshToken`.
- User có thể có nhiều role. Mỗi request tải lại active state và role hiện tại từ database.
- Spring Security chỉ tạo authority dạng `ROLE_<ROLE_NAME>`.
- Auth chỉ khai báo `ADMIN` cho API quản lý user, role và user-role. Chủ sở hữu từng module nghiệp vụ tự khai báo role cho controller của module đó.
- Không triển khai brute-force throttle, trusted proxy/source-address hoặc security event.
- Runtime chỉ dùng `users`, `roles`, `user_roles` và `rbac_mutation_guard`; schema vẫn giữ `permissions` và `role_permissions` để bảo toàn dữ liệu hiện có.

## User Stories

### US1 — Login và refresh JWT stateless

1. Active user với mật khẩu đúng nhận đúng bốn trường response.
2. Username không tồn tại, sai mật khẩu hoặc inactive đều trả `401` chung.
3. Refresh kiểm tra signature, issuer, audience, expiry và `token_type=refresh`.
4. Refresh JWT còn hạn có thể dùng lại; server không lưu token.
5. Refresh token không gọi được business API và access token không gọi được refresh.

### US2 — Cung cấp nền tảng phân quyền API theo role

1. Không có access JWT hợp lệ trả `401`.
2. Có JWT nhưng không có `ADMIN` khi gọi API quản lý user/role trả `403` và không mutation dữ liệu.
3. Có `ADMIN` thì API quản lý user/role được xử lý.
4. Thêm/gỡ role hoặc deactivate user có hiệu lực ở request kế tiếp.
5. Custom role có thể được tạo và gán; quyền trên API nghiệp vụ do chủ sở hữu module khai báo.

### US3 — Quản lý user và gán nhiều role

1. ADMIN quản lý list/detail/create/update/activate/deactivate user.
2. Mật khẩu được lưu bằng BCrypt và không xuất hiện trong response/log.
3. Username trùng trả `409`, kể cả request đồng thời.
4. ADMIN thay thế toàn bộ tập role của user; ID trùng được loại bỏ.
5. Mutation có role ID không tồn tại bị từ chối toàn bộ.
6. Không được làm mất active ADMIN cuối cùng.

### US4 — Quản lý role

1. ADMIN quản lý list/detail/create/update/delete role.
2. Role name được trim, uppercase và unique.
3. `RoleDto` chỉ có `id`, `name`, `description`.
4. Role `ADMIN` và role đang được gán không thể bị xóa.

## Functional Requirements

- **FR-001**: Chỉ login và refresh là public trong `/api`.
- **FR-002**: Access/refresh JWT dùng HS256 và kiểm tra đầy đủ issuer, audience, signature, expiration, token type.
- **FR-003**: Login và refresh chỉ trả bốn trường đã quy định.
- **FR-004**: Refresh không đọc/ghi token hoặc session trong database.
- **FR-005**: Mỗi protected request tải user và direct roles bằng một query.
- **FR-006**: Inactive hoặc missing user làm access JWT mất hiệu lực ngay ở request kế tiếp.
- **FR-007**: Authentication chỉ cấp các authority có tiền tố `ROLE_`.
- **FR-008**: Các handler quản lý user, role và user-role dùng `hasRole('ADMIN')`.
- **FR-009**: Feature auth không thêm `@PreAuthorize` vào controller nghiệp vụ ngoài phạm vi; chủ sở hữu module tự định nghĩa role.
- **FR-010**: User có nhiều role được phép nếu bất kỳ role nào nằm trong nhóm được khai báo.
- **FR-011**: User CRUD và role assignment chỉ dành cho ADMIN.
- **FR-012**: Role CRUD chỉ dành cho ADMIN.
- **FR-013**: User-role replacement hỗ trợ nhiều role và tập rỗng.
- **FR-014**: Bảo vệ active ADMIN cuối cùng dưới concurrent mutation.
- **FR-015**: Code role nằm trong module `auth`; không có module role độc lập.
- **FR-016**: Seed role insert-only, idempotent và không ghi đè dữ liệu hiện hữu.
- **FR-017**: Database giữ nguyên `permissions` và `role_permissions`, nhưng runtime authorization không đọc hoặc ghi hai bảng này.
- **FR-018**: OpenAPI đánh dấu bearer security cho mọi protected operation.

## Key Entities

- **User**: identity, username, BCrypt hash, profile, active state.
- **Role**: nhóm trách nhiệm có name duy nhất.
- **User–Role Assignment**: quan hệ nhiều-nhiều `(user_id, role_id)`.
- **Access JWT**: JWT ngắn hạn, `token_type=access`.
- **Refresh JWT**: JWT dài hạn stateless, `token_type=refresh`.

## Success Criteria

- Toàn bộ test login/refresh, 401/403, role reload và active-state reload vượt qua.
- 100% protected API có role expression hợp lệ.
- Migration cuối xóa hai bảng auth không còn sử dụng.
- Không rò rỉ password, password hash hoặc token ngoài hai field token.
- Ít nhất 95% login/refresh hợp lệ hoàn tất dưới 2 giây trong profile test tải.
