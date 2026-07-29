# Feature Specification: Hoàn thiện Authentication và RBAC

**Feature Branch**: `feature/auth-rbac-stateless-jwt`
**Created**: 2026-07-28
**Updated**: 2026-07-29
**Status**: Implemented

## Clarifications

### Session 2026-07-29

- Refresh token là JWT stateless, không lưu token hoặc phiên refresh trong database.
- Refresh token hợp lệ được dùng đến khi hết hạn; mỗi lần refresh cấp access token và refresh token mới.
- Hệ thống phân quyền theo các role hiện tại của user. Authority `RESOURCE_ACTION` được suy ra từ ma trận role–permission tĩnh trong code theo `FactoryFlow_SRS.md`.
- Không cung cấp CRUD permission và không cung cấp API gán permission cho role.
- Không triển khai giới hạn đăng nhập sai, trusted proxy/source-address hoặc security event.
- Giữ nguyên schema và dữ liệu permission/role-permission hiện có để nâng cấp không làm mất dữ liệu, nhưng chúng không phải nguồn quyết định truy cập runtime.

## User Scenarios & Testing

### User Story 1 - Đăng nhập và refresh JWT stateless (Priority: P1)

User có tài khoản hoạt động đăng nhập bằng username/password để nhận user ID, username, access token và refresh token. User dùng refresh JWT hợp lệ để nhận cặp token mới mà không cần lưu token phía server.

**Independent Test**: Đăng nhập, dùng access token gọi API phù hợp, refresh để nhận cặp token mới, dùng cả refresh token gốc và token mới trước thời điểm hết hạn.

**Acceptance Scenarios**:

1. Tài khoản hoạt động và mật khẩu đúng chỉ trả `userId`, `username`, `accessToken`, `refreshToken`.
2. Username không tồn tại, mật khẩu sai hoặc tài khoản không hoạt động đều bị từ chối bằng lỗi xác thực không tiết lộ thông tin.
3. Refresh JWT đúng chữ ký, issuer, audience, thời hạn, loại `refresh` và thuộc user còn hoạt động trả cặp token mới.
4. Cùng một refresh JWT còn hạn có thể được dùng lại vì luồng stateless không lưu trạng thái token.
5. Token hết hạn, sai chữ ký, sai issuer/audience hoặc sai loại bị từ chối.
6. Refresh token không thể xác thực business API; access token không thể dùng cho endpoint refresh.
7. Mặc định access token hết hạn sau 15 phút và refresh token sau 7 ngày.
8. Đăng nhập sai lặp lại vẫn trả `401`; service không khóa hay throttle theo username/IP.

### User Story 2 - Phân quyền API theo role (Priority: P1)

Mỗi request được bảo vệ tải lại trạng thái user và toàn bộ role trực tiếp từ database. Hệ thống hợp các authority tĩnh của các role, loại trùng và dùng chúng cho `@PreAuthorize`.

**Independent Test**: Gán nhiều role cho một user, xác nhận hợp quyền đúng; thêm/gỡ role và vô hiệu hóa user rồi xác nhận request kế tiếp phản ánh ngay thay đổi.

**Acceptance Scenarios**:

1. Request không có access JWT hợp lệ trả `401`.
2. User xác thực nhưng role không cho phép hành động trả `403` và không thay đổi dữ liệu.
3. Chỉ cần một role cho phép hành động thì request được tiếp tục.
4. User có nhiều role nhận hợp authority không trùng lặp, sắp xếp ổn định.
5. Thêm/gỡ role hoặc đổi trạng thái user có hiệu lực ở request kế tiếp.
6. Thay đổi bảng `role_permissions` không thay đổi quyền runtime.

### User Story 3 - Quản lý user và gán role (Priority: P2)

ADMIN quản lý vòng đời user và thay thế tập role của user bằng một tập role hợp lệ, có thể gồm nhiều role hoặc tập rỗng.

**Acceptance Scenarios**:

1. Tạo user mới lưu mật khẩu BCrypt và không trả password/password hash.
2. Username trùng trả `409`, kể cả khi có request đồng thời.
3. API list/detail/update/activate/deactivate user giữ nguyên contract.
4. Gán role loại ID trùng; nếu có role không tồn tại thì toàn bộ mutation bị từ chối.
5. Không được vô hiệu hóa hoặc gỡ role làm hệ thống mất active user có role `ADMIN` cuối cùng.

### User Story 4 - Quản lý role với policy tĩnh (Priority: P2)

ADMIN xem, tạo, cập nhật và xóa role. `permissionNames` trong `RoleDto` là thông tin chỉ đọc được suy ra từ policy tĩnh theo tên role.

**Acceptance Scenarios**:

1. Role name được trim, chuẩn hóa uppercase và giữ duy nhất.
2. Role chuẩn trong SRS trả đúng `permissionNames` từ policy tĩnh.
3. Role tùy chỉnh không có policy trả danh sách quyền rỗng.
4. Role đang được gán và role hệ thống `ADMIN` không thể bị xóa.
5. `/api/permissions`, `/api/permissions/{id}` và `/api/roles/{id}/permissions` không tồn tại.

### User Story 5 - Hợp nhất module và bảo toàn dữ liệu (Priority: P3)

Toàn bộ role/auth nằm trong module `auth`; không còn package `fpt.qn.mes.role`. Schema và dữ liệu user, role, permission, user-role, role-permission hiện có không bị xóa hoặc đổi ID.

**Acceptance Scenarios**:

1. API auth, user và role còn trong phạm vi giữ nguyên contract, ngoại trừ các API permission bị loại theo clarification mới.
2. Migration đã áp dụng không bị sửa; không có migration phá dữ liệu.
3. Seed role chạy lặp lại, insert-only và không ghi đè dữ liệu hiện hữu.
4. Schema mới không tạo bảng refresh session/token, throttle hoặc security event; dữ liệu permission/role-permission có sẵn vẫn được bảo toàn nhưng không được runtime auth sử dụng.

## Edge Cases

- User không có role vẫn đăng nhập/refresh được nhưng mọi business API trả `403`.
- User có nhiều role cùng sinh một authority chỉ nhận authority đó một lần.
- Refresh JWT hợp lệ nhưng user đã bị xóa hoặc vô hiệu hóa bị từ chối.
- Username của user thay đổi sau khi token phát hành: refresh dùng user ID trong `sub`, tải lại username hiện tại và phát hành token mới.
- Role tùy chỉnh không tự nhận quyền từ dữ liệu `role_permissions`.
- ADMIN cuối cùng được bảo vệ khi deactivate hoặc thay thế role đồng thời.

## Functional Requirements

- **FR-001**: Xác thực username/password cho active user; dùng lỗi chung cho username không tồn tại và password sai.
- **FR-002**: Login và refresh trả access token, refresh token, non-sensitive profile, sorted roles và sorted derived authorities.
- **FR-003**: Không trả hoặc log password, password hash hay token ngoài các field token được quy định.
- **FR-004**: Access/refresh JWT dùng HS256 và kiểm tra issuer, audience, signature, expiration và `token_type`.
- **FR-005**: Access JWT mặc định 15 phút; refresh JWT mặc định 7 ngày và cho phép cấu hình môi trường ghi đè.
- **FR-006**: Refresh hoàn toàn stateless; không đọc/ghi refresh token, refresh session, token hash hoặc replay state trong database.
- **FR-007**: Mỗi refresh thành công cấp cặp token mới; refresh token trước đó vẫn hợp lệ đến khi hết hạn.
- **FR-008**: Từ chối token dùng sai mục đích.
- **FR-009**: Chỉ login và refresh là public trong `/api`; mọi business API yêu cầu access JWT.
- **FR-010**: Mỗi protected handler có đúng một authority `RESOURCE_ACTION`.
- **FR-011**: Tải active user và direct roles ở mỗi protected request.
- **FR-012**: Authority hiệu lực là hợp không trùng của policy tĩnh cho tất cả role hiện tại của user.
- **FR-013**: Database `role_permissions` và `permissions` không tham gia quyết định truy cập runtime.
- **FR-014**: Hỗ trợ list có phân trang, detail, create, update, activate và deactivate user theo contract hiện có.
- **FR-015**: Hỗ trợ thay thế nguyên tử tập nhiều role của user, gồm tập rỗng.
- **FR-016**: Bảo toàn unique username và xử lý duplicate concurrent bằng `409`.
- **FR-017**: Hỗ trợ list, detail, create, update và safe-delete role.
- **FR-018**: Không cung cấp permission CRUD hoặc role-permission assignment.
- **FR-019**: `RoleDto.permissionNames` được sinh từ policy tĩnh và là read-only.
- **FR-020**: Chỉ active user có role `ADMIN` được coi là administrator cho guard cuối cùng; policy của ADMIN bao phủ mọi authority API.
- **FR-021**: Không triển khai brute-force throttle, source-address/trusted-proxy processing hoặc security-event persistence.
- **FR-022**: Giữ nguyên dữ liệu permission/role-permission có sẵn; migration auth/RBAC chưa phát hành không tạo bảng refresh session/token, throttle hoặc security event.
- **FR-023**: Toàn bộ code role thuộc module `auth`, không còn module/package role độc lập.
- **FR-024**: Seed chỉ reconcile roles theo kiểu insert-only; không sửa/xóa permission hoặc role-permission hiện có.

## Key Entities

- **User**: Identity, username, BCrypt password hash, profile, active state và direct roles.
- **Role**: Nhóm trách nhiệm có name duy nhất; policy tĩnh ánh xạ role chuẩn sang authorities.
- **User–Role Assignment**: Quan hệ nhiều-nhiều trực tiếp; một cặp user-role chỉ tồn tại một lần.
- **Access JWT**: JWT ngắn hạn có `sub`, `username`, `jti`, issuer, audience và `token_type=access`.
- **Refresh JWT**: JWT dài hạn stateless có cùng identity claims và `token_type=refresh`.
- **Legacy Permission Data**: Bảng `permissions` và `role_permissions` được bảo toàn cho tương lai nhưng không điều khiển runtime.

## Success Criteria

- **SC-001**: 100% test login đúng/sai, inactive user, refresh đúng/sai loại/hết hạn vượt qua.
- **SC-002**: 100% protected handlers phân biệt đúng `401`, `403` và request được phép.
- **SC-003**: Cùng refresh JWT còn hạn thành công ở các lần gọi tuần tự và đồng thời; không phát sinh refresh/session record.
- **SC-004**: Thay đổi role/active state phản ánh ở request kế tiếp; thay đổi `role_permissions` không ảnh hưởng.
- **SC-005**: Không còn endpoint permission CRUD hoặc role-permission assignment trong OpenAPI.
- **SC-006**: Không mất hoặc đổi ID dữ liệu user, role, permission, user-role, role-permission hiện có.
- **SC-007**: Ít nhất 95% login/refresh hợp lệ hoàn tất dưới 2 giây ở profile tải đã định.
- **SC-008**: Không có password, hash hoặc token bị rò rỉ trong error/log.

## Assumptions

- `FactoryFlow_SRS.md` là nguồn yêu cầu nghiệp vụ chính; ma trận role–authority tĩnh thực thi mục 4.1 của SRS.
- Permission names vẫn được dùng nội bộ làm Spring Security authorities để giữ nguyên annotation của business API; đây không phải permission được quản trị động.
- User có thể có nhiều role.
- Logout, blacklist/revocation, self-registration, forgot/reset password và MFA nằm ngoài phạm vi.
- Dữ liệu permission/role-permission có sẵn không bị drop để đảm bảo khả năng nâng cấp và bảo toàn dữ liệu.
