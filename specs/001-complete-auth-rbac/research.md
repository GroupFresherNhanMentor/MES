# Research Decisions

## Stateless refresh JWT

Refresh JWT được xác thực bằng signature, issuer, audience, expiry và token type. Server không lưu session/token, phù hợp yêu cầu đơn giản hóa hệ thống.

## Role-only method security

Mỗi request tải direct roles từ database. Converter tạo Spring authorities dạng `ROLE_<ROLE_NAME>`. Controller dùng `hasRole`/`hasAnyRole`, giúp reviewer nhìn thấy trực tiếp actor nào được gọi API.

## Multiple roles

Spring Security cho phép request nếu có ít nhất một authority khớp biểu thức role của endpoint. Thay đổi role có hiệu lực ngay ở request kế tiếp vì role không được nhúng làm nguồn quyết định trong JWT.

## Custom roles

Custom role vẫn có thể được tạo và gán. Việc role đó được truy cập API nghiệp vụ nào do chủ sở hữu controller tương ứng khai báo.

## Database cleanup

Không sửa migration đã áp dụng. Một migration mới xóa các bảng auth không còn được runtime sử dụng để schema cuối phản ánh đúng mô hình role-only.
