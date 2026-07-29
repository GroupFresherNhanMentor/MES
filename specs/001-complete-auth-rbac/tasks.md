# Tasks: Authentication và role-based authorization

## Phase 1 - Contract and design

- [X] T001 Đối chiếu `FactoryFlow_SRS.md`, `AGENTS.md`, `docs/rules` và auth flow của ProjectManagementSystem.
- [X] T002 Chốt refresh JWT stateless, static role policy và danh sách API bị loại.
- [X] T003 Cập nhật spec, plan, contracts, data model, research, quickstart và endpoint docs.

## Phase 2 - Stateless authentication

- [X] T004 Đơn giản hóa `TokenPort` thành issue access/refresh và parse refresh.
- [X] T005 Bỏ `sid`, session persistence, token hash, rotation/replay service khỏi `JwtTokenProvider` và `AuthService`.
- [X] T006 Login dùng dummy BCrypt hash và generic `401`.
- [X] T007 Refresh validate JWT + active user rồi issue cặp token mới.
- [X] T008 Giữ access/refresh purpose validation bằng decoder riêng.
- [X] T009 Loại throttle, trusted proxy/source-address và security-event runtime/config/handler.

## Phase 3 - Role authorization

- [X] T010 Tạo `RolePermissionPolicy` tĩnh cho 8 role SRS.
- [X] T011 Tải user + direct roles trong một query, không join refresh/permission tables.
- [X] T012 Sinh authorities từ hợp policy của nhiều role và thêm `ROLE_<role>`.
- [X] T013 Đảm bảo thay đổi role/active state có hiệu lực ở request kế tiếp.
- [X] T014 Đổi last-admin guard sang active user có role `ADMIN`.

## Phase 4 - RBAC management scope

- [X] T015 Giữ user CRUD và atomic multi-role replacement.
- [X] T016 Giữ role CRUD; sinh `RoleDto.permissionNames` từ policy tĩnh.
- [X] T017 Loại PermissionController/service/repository/DTO/domain runtime.
- [X] T018 Loại `POST /api/roles/{id}/permissions`.
- [X] T019 Seed chỉ roles; giữ nguyên schema/dữ liệu legacy.

## Phase 5 - Tests

- [X] T020 Unit test login, refresh, inactive user và token issuance.
- [X] T021 Unit test JWT claims, lifetime và purpose isolation.
- [X] T022 Unit test multi-role union, no-role/unknown-role và inactive user.
- [X] T023 Unit test role/user-role service và ADMIN guard.
- [X] T024 Integration test stateless refresh reuse tuần tự/đồng thời.
- [X] T025 Integration test current role/active reload và DB role-permission không ảnh hưởng.
- [X] T026 Contract test 91 handlers, 89 protected handlers và removed permission APIs.
- [X] T027 Integration test user/role CRUD, concurrency và sensitive-data safety.
- [X] T028 Chạy full `verify` và performance profile; ghi nhận kết quả cuối.
