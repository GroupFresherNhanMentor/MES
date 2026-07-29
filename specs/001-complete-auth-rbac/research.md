# Research: Authentication và RBAC

## Final decisions

### Stateless refresh JWT

**Decision**: Theo flow ProjectManagementSystem, refresh endpoint decode JWT, kiểm tra loại refresh, tải user hiện tại và issue access/refresh mới. MES bổ sung issuer/audience validation và kiểm tra active user.

**Rationale**: Reviewer yêu cầu không lưu refresh token. Cách này loại refresh-session/token persistence và đồng nghĩa token cũ còn dùng được tới expiry.

**Trade-off**: Không hỗ trợ one-time rotation, replay detection hay server-side revoke. Logout/blacklist không thuộc scope.

### Role-based authorization with static policy

**Decision**: Database lưu direct user-role assignments. Static `Map<Role, Set<Permission>>` trong code sinh Spring authorities.

**Rationale**: Khớp `FactoryFlow_SRS.md` mục 4.1 và comment “hiện tại dùng role để phân quyền”, đồng thời giữ được 89 annotation authority đã có.

**Trade-off**: Custom role không có quyền cho đến khi được thêm vào policy code; thay đổi policy cần deploy. `role_permissions` legacy không có hiệu lực runtime.

### No permission management

**Decision**: Loại permission CRUD và role-permission assignment.

**Rationale**: Reviewer xác nhận không cần CRUD permission và không gán nhiều permission cho role.

### No login throttle, trusted proxy or security events

**Decision**: Loại toàn bộ runtime code/config/test cho ba concern này.

**Rationale**: Đây là yêu cầu loại bỏ rõ ràng. Password verification vẫn dùng dummy BCrypt hash để giảm chênh lệch giữa missing user và wrong password.

### Database compatibility

**Decision**: Vì migration auth/RBAC chưa phát hành, loại DDL tạo refresh session/token, login throttle và security event trước khi phát hành. Giữ nguyên dữ liệu permission/role-permission có sẵn từ schema nền.

**Rationale**: SRS yêu cầu bảo toàn dữ liệu hiện có, nhưng bốn bảng auth cũ chưa có dữ liệu dùng chung cần giữ và không thuộc thiết kế stateless hiện tại.

## Validation targets

- Refresh/access purpose isolation.
- Signature, issuer, audience và expiration.
- User active state và current roles được tải ở từng request.
- Static policy bao phủ mọi authority của protected handlers.
- Không query refresh/permission tables trong auth hot path.
- No sensitive credentials/tokens in errors or logs.
