# Quickstart: Authentication và role-based authorization

## Configuration

Required environment:

```text
JWT_SECRET=<at-least-32-byte-secret>
JWT_ISSUER=factoryflow
JWT_AUDIENCE=factoryflow-api
JWT_ACCESS_EXPIRATION=900000
JWT_REFRESH_EXPIRATION=604800000
```

Không có `app.auth` throttle/trusted-proxy configuration.

## Run focused unit tests

```powershell
cd be
.\mvnw.cmd "-Djooq.codegen.skip=true" "-Dtest=AuthServiceTest,EffectivePermissionServiceTest,AdministrativeAccessGuardTest,RoleServiceTest,UserRoleServiceTest,JwtTokenProviderTest" test
```

## Run full verification

```powershell
.\mvnw.cmd verify "-Djooq.codegen.skip=true"
```

Performance profile:

```powershell
.\mvnw.cmd "-Pauth-performance" "-Djooq.codegen.skip=true" "-Dtest=AuthPerformanceIntegrationTest" test
```

## Manual smoke flow

1. `POST /api/auth/login` và lấy `accessToken`, `refreshToken`.
2. Dùng access token gọi API được role cho phép.
3. `POST /api/auth/refresh` nhiều lần với cùng refresh token còn hạn; mỗi lần phải trả `200` và cặp token mới.
4. Dùng refresh token làm bearer cho business API phải nhận `401`.
5. Dùng access token cho refresh endpoint phải nhận `401`.
6. Gán/gỡ role và gọi lại API bằng access token cũ; kết quả phải phản ánh role hiện tại.
7. Gọi `/api/permissions` hoặc `/api/roles/{id}/permissions`; endpoint phải không tồn tại.

## Database check

Migration auth/RBAC chỉ bổ sung ràng buộc/index cần thiết và `rbac_mutation_guard`. Schema mới không có `refresh_sessions`, `refresh_tokens`, `login_throttles` hoặc `security_events`.
