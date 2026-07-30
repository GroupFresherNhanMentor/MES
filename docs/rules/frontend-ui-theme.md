# Frontend UI & Theme Guide

Angular 22 · Tailwind CSS v4 · Angular Material 22

---

## 🎨 1. Theme Color Palette & Design Tokens

Theme sử dụng **Industrial Dark Mode** với tone màu chủ đạo là **Stone & Orange Accent** cho hệ thống MES (Manufacturing Execution System). Tất cả thiết lập theme khai báo trong `@theme` của [styles.scss](file:///home/amtia/Projects/Fresher-java/F1-Q1/exer2/MES/fe/src/styles.scss).

| Token | Class Tailwind | Mã màu (Hex/RGBA) | Mục đích sử dụng |
|-------|----------------|-------------------|------------------|
| **Background App** | `bg-surface-app` | `#0c0a09` (Stone 950) | Nền chính của toàn bộ giao diện app |
| **Surface Elevated** | `bg-surface-elevated` | `#1c1917` (Stone 900) | Top Header Toolbar, Sidenav Sidebar |
| **Surface Card / Dialog** | `bg-surface-card` | `#292524` (Stone 800) | Cards, Modals/Dialogs, Dropdowns |
| **Primary Brand** | `bg-primary`, `text-primary` | `#ea580c` (Orange 600) | Accent màu cam chính: nút bấm chính, active tab/menu |
| **Primary Hover** | `bg-primary-hover` | `#c2410c` (Orange 700) | Hover state cho primary action |
| **Accent / Focus** | `text-accent`, `border-border-focus` | `#d97706` (Amber 600) | Highlight secondary, outline focus input |
| **Text Primary** | `text-text-primary` | `#fafaf9` (Stone 50) | Tiêu đề, chữ chính (High contrast) |
| **Text Secondary** | `text-text-secondary` | `#a8a29e` (Stone 400) | Subtitle, label, table header |
| **Text Muted** | `text-text-muted` | `#78716c` (Stone 500) | Placeholder, hint text |
| **Border Default** | `border-border-default` | `#44403c` (Stone 700) | Viền card, table header border, dialog border |
| **Border Light** | `border-border-light` | `#292524` (Stone 800) | Viền phân cách nhẹ (dividers) |

---

## 🏷️ 2. Dynamic Status Badges (`.ff-badge`)

Hệ thống MES có các trạng thái quy trình sản xuất được chuẩn hóa thông qua CSS Utilities toàn cục trong [styles.scss](file:///home/amtia/Projects/Fresher-java/F1-Q1/exer2/MES/fe/src/styles.scss):

```html
<span class="ff-badge ff-badge--active">Active</span>
<span class="ff-badge ff-badge--pending">Pending</span>
<span class="ff-badge ff-badge--completed">Completed</span>
<span class="ff-badge ff-badge--onhold">On Hold</span>
<span class="ff-badge ff-badge--cancelled">Cancelled</span>
<span class="ff-badge ff-badge--running">Running</span>
<span class="ff-badge ff-badge--idle">Idle</span>
```

### Quy tắc Màu Trạng thái (Status Colors):
- `active` / `running` / `success`: Chữ `#65a30d`, Nền `rgba(101,163,13,0.15)`, Border `#65a30d` (Green)
- `completed` / `orange`: Chữ `#ea580c`, Nền `rgba(234,88,12,0.15)`, Border `#ea580c` (Orange)
- `onhold` / `warning` / `maintenance`: Chữ `#d97706`, Nền `rgba(217,119,6,0.15)`, Border `#d97706` (Amber)
- `cancelled` / `error` / `inactive` / `retired`: Chữ `#f87171`, Nền `rgba(248,113,113,0.15)`, Border `#dc2626` (Vibrant Red)
- `pending` / `paused` / `idle`: Chữ `#a8a29e` / `#78716c`, Nền `#292524`, Border `#44403c` (Stone)

---

## 🧱 3. Quy tắc Component & Styling Guidelines

1. **Tailwind CSS Utility First**:
   - Sử dụng trực tiếp class Tailwind trong template HTML.
   - **Tuyệt đối KHÔNG** tạo file `.scss` riêng cho từng component. Loại bỏ khai báo `styleUrl` trong `@Component`.
2. **Không dùng Inline Style (`style="..."`)**:
   - Mọi spacing, typography, layout phải dùng utility class của Tailwind (vd: `p-4`, `flex`, `gap-3`).
3. **Angular Material Overrides**:
   - Tất cả Material deep override (`.mat-mdc-*`, `.mdc-*`) bắt buộc viết tập trung tại [styles.scss](file:///home/amtia/Projects/Fresher-java/F1-Q1/exer2/MES/fe/src/styles.scss).
   - Khi override style Material từ template Tailwind, thêm tiền tố `!` (ví dụ: `!bg-surface-elevated`).
4. **Reusability & Components**:
   - KPI Cards: Dùng class `.kpi-card`, `.kpi-label`, `.kpi-value`, `.kpi-trend`.
   - Cards/Tables: Dùng `mat-card` (đã được override background `#292524` & border `#44403c`) hoặc class `.ff-card`.
   - Primary Action Button (`.ff-btn-primary`): Dùng class `.ff-btn-primary` cho các nút bấm hành động chính (như `+ Add Product`, `+ Add Warehouse`). Nút có hiệu ứng Gradient màu cam (`#ea580c` -> `#c2410c`), bóng đổ nổi `box-shadow` và animation hover mượt mà (`translateY(-1px)`).
   - Card Header Layout (`mat-card-header`): Đặt tiêu đề (`mat-card-title`), nút thao tác (`.ff-btn-primary`), các ô lọc/tìm kiếm (`Search`, `Status filter`) nằm gọn trong cùng một hàng của `<mat-card-header class="!flex !items-center !justify-between !pb-4">`.
   - Table Action Icon Buttons (`.ff-action-btn-*`): Tất cả nút icon thao tác trong bảng dữ liệu toàn bộ dự án phải dùng chuẩn class màu sinh động:
     - Edit (`.ff-action-btn-edit`): Màu Xanh Sky Blue (`#38bdf8`), nền mờ 10% `rgba(56,189,248,0.1)`.
     - View Details (`.ff-action-btn-view`): Màu Tím Violet (`#a855f7`), nền mờ 10% `rgba(168,85,247,0.1)`.
     - Deactivate / Delete (`.ff-action-btn-delete`): Màu Đỏ Vibrant Red (`#f87171`), nền mờ 10% `rgba(248,113,113,0.1)`.
     - Tất cả có bo góc 6px, hiệu ứng hover đổi màu sáng hơn và zoom nhẹ `scale(1.08)`.
   - Dynamic Status Badge Behavior: 
     - Ánh xạ trạng thái linh hoạt không phân biệt hoa-thường (`.toUpperCase()`).
     - Khi vô hiệu hóa (`Deactivate`) hoặc khi statusName không khả dụng, luôn có fallback chữ **`INACTIVE` đỏ nổi bật** (`.ff-badge--cancelled`) giúp người dùng nhận biết ngay lập tức.
   - App Shell Header & Navigation: 
     - Top Toolbar Header: Chứa Logo Brand `FF` nền gradient cam bo góc, tên hệ thống `MES` nổi bật, tiêu đề trang hiện tại, và Menu tài khoản người dùng hình tròn đại diện cho Avatar.
     - Sidebar Menu (`.nav-item`): Mục menu dạng pill có icon rõ ràng, khi được kích hoạt (`.active`) có nền hiệu ứng màu cam mờ `rgba(234,88,12,0.15)` và chữ màu `#ea580c`.
   - Login Page Design: Trang đăng nhập sử dụng thiết kế **Glassmorphism Dark Mode** hiện đại với vòng sáng hiệu ứng Radial Glow, logo hiệu ứng 3D Gradient, ô nhập liệu có icon dẫn đường, và các Nút chọn quyền nhanh (Quick Demo Access Pills) để thử nghiệm phân quyền dễ dàng.
   - Table Headers (`.mat-mdc-header-cell`): Chữ màu `#ea580c` (Primary Orange), font-weight `bold`, size `17px`, uppercase, letter-spacing `0.5px`, viền dưới `#44403c`.
   - Modals/Dialogs Popup: Sử dụng `panelClass: 'ff-dialog-panel'` khi mở `MatDialog`. Thẻ Popup được bo góc 16px, có viền tối `#44403c`, tiêu đề phân cách rõ ràng và bóng đổ độ sâu lớn (`box-shadow: 0 20px 40px rgba(0,0,0,0.6)`).
   - SnackBar Popup Notifications: Tất cả thông báo nổi (`MatSnackBar`) được cố định hiển thị ở **Góc trên cùng bên phải (Top-Right Toast)** với lề `24px`, nền gradient tối sang trọng `linear-gradient(135deg, #292524 -> #1c1917)`, viền màu cam chính `#ea580c`, bo góc 10px, hiệu ứng phát sáng nhẹ và chữ đậm độ tương phản cao.
