# Software Requirements Specification (SRS)
## FactoryFlow — Manufacturing Operations Platform

**Version:** 1.0
**Nguồn:** User Requirement Specification (URS) gốc + `diagram.puml` + toàn bộ quyết định thiết kế đã thống nhất qua quá trình review DB.
**File đi kèm:** `V20260726194837__init.sql` (Flyway migration, PostgreSQL 14+)

---

## 1. Giới thiệu

### 1.1. Mục đích
Tài liệu này đặc tả chi tiết yêu cầu phần mềm cho FactoryFlow — hệ thống quản lý vận hành nhà máy cơ bản — dùng làm căn cứ thiết kế, triển khai và kiểm thử. SRS này **mở rộng** URS gốc bằng cách:
- Hình thức hóa lại toàn bộ functional requirement theo cấu trúc chuẩn (actor, precondition, business rule, data model liên quan).
- Ghi lại **quyết định thiết kế** cho những phần URS để hở (gap) — đã được thảo luận và chốt trong quá trình thiết kế DB.
- Gắn kết trực tiếp từng module với schema thật trong `V1__init_schema.sql`.

### 1.2. Phạm vi
Toàn bộ 12 module: Auth, Master Data, Inventory, Stock Movement Ledger, BOM, Work Order, Material Reservation, Production Execution, Quality Control, Maintenance, Reporting, Audit Log. Không bao gồm phần Angular UI chi tiết (chỉ liệt kê page bắt buộc).

### 1.3. Định nghĩa, thuật ngữ
Xem file `FactoryFlow_Glossary.md` đi kèm (13 nhóm thuật ngữ nghiệp vụ). Các thuật ngữ kỹ thuật quan trọng nhất:
- **Ledger pattern**: bản ghi lịch sử bất biến (`stock_movements`, `audit_logs`), chỉ INSERT, không UPDATE/DELETE.
- **Lookup table pattern**: bảng tham chiếu tĩnh cho status/type/priority (VD: `machine_statuses`), FK trỏ vào thay vì hard-code enum.
- **Snapshot table**: bảng phản ánh trạng thái hiện tại (`stock_balances`, `work_order_materials`), được UPDATE liên tục nhưng luôn kèm bằng chứng ở bảng ledger tương ứng.

### 1.4. Tài liệu tham chiếu
- `FactoryFlow_URS.md` — bản URS gốc, convert từ PDF.
- `FactoryFlow_Glossary.md` — bảng thuật ngữ nghiệp vụ.
- `V1__init_schema.sql` — DDL đầy đủ, 45 bảng.

---

## 2. Mô tả tổng quan

### 2.1. Bối cảnh sản phẩm
Một nhà máy nhỏ cần hệ thống quản lý toàn bộ vòng đời sản xuất: nguyên vật liệu → BOM → Work Order → reserve vật tư → sản xuất → QC → thành phẩm sẵn sàng bán/xuất, song song với quản lý bảo trì máy móc và báo cáo vận hành.

### 2.2. Luồng nghiệp vụ chính

```
Nhập kho nguyên vật liệu (Warehouse Manager)
        ↓
Tạo BOM cho thành phẩm (Planner)
        ↓
Tạo Work Order sản xuất (Planner)
        ↓
Tính material requirement (hệ thống, tự động)
        ↓
Reserve nguyên vật liệu (Planner)
        ↓
Start Production (Operator, trên 1 machine cụ thể)
        ↓
Complete Production → consume vật tư, tạo thành phẩm QUALITY_INSPECTION
        ↓
QC Pass/Fail (QC Inspector)
        ↓
Hàng PASSED → AVAILABLE → sẵn sàng dùng/xuất
        ↓
Factory Manager xem báo cáo | Auditor xem audit log
```

### 2.3. Nhóm người dùng (Actor)

| # | Role | Nhóm quyền chính | Bản chất |
|---|---|---|---|
| 1 | Admin | Quản lý user/role, master data, xem toàn bộ dữ liệu; **tạo maintenance ticket** (bổ sung) | Vận hành hệ thống |
| 2 | Warehouse Manager | Stock In, Adjustment, Transfer, xem balance/movement | Write — kho vật lý |
| 3 | Planner | BOM, Work Order, Reserve/Release material, xem shortage; **tạo maintenance ticket** (bổ sung) | Write — kế hoạch sản xuất |
| 4 | Production Operator | Start/Pause/Resume/Complete Work Order; **tạo maintenance ticket** (bổ sung) | Write — thực thi sản xuất |
| 5 | QC Inspector | Pass/Fail/Hold/Scrap QC | Write — chất lượng đầu ra |
| 6 | Maintenance Engineer | Tạo/Start/Close maintenance ticket | Write — bảo trì máy |
| 7 | Factory Manager | Dashboard, report, approve adjustment lớn, audit log; **tạo/start/close maintenance ticket** (bổ sung) | Read + approve |
| 8 | Auditor | Đọc thuần túy (stock movement, audit log) | Read-only |

**Quyết định bổ sung (ngoài URS mục 3, không đổi qua tầng Auth chuẩn RBAC theo role thuần túy):** người **phát hiện lỗi** — bất kỳ ai trong {Maintenance Engineer, Production Operator, Planner, Factory Manager, Admin} — được quyền **tạo** maintenance ticket. Nhưng chỉ **{Maintenance Engineer, Factory Manager, Admin}** — tức người trực tiếp bảo trì hoặc quản lý — mới được **start/close** ticket. URS gốc (mục 3.6) chỉ trao toàn bộ 3 quyền này cho riêng Maintenance Engineer; đây là mở rộng hợp lý vì thực tế Operator/Planner thường là người phát hiện máy hỏng đầu tiên trong lúc vận hành.

**Mô hình phân quyền:** role-based, không resource-scoped — 1 role áp dụng cho toàn hệ thống (VD: mọi Warehouse Manager thao tác được mọi warehouse), trừ khi ứng dụng thêm logic kiểm tra `warehouse_managers` (bảng mở rộng, xem mục 4.4).

### 2.4. Môi trường vận hành / Stack bắt buộc

| Thành phần | Công nghệ |
|---|---|
| Backend | Java, Spring Boot |
| Frontend | Angular |
| Database | PostgreSQL 14+ |
| Migration | Flyway |
| Auth | Spring Security + JWT, password hash BCrypt |
| API docs | Swagger/OpenAPI |
| Container | Docker Compose |
| Testing | JUnit 5, Mockito, Testcontainers (optional) |

### 2.5. Ràng buộc thiết kế
- Không dùng `ddl-auto=create` cho demo chính thức.
- Response format thống nhất toàn hệ thống (xem mục 6.1).
- Mọi ID là UUID, do application sinh (`UUID.randomUUID()`), không dùng `DEFAULT gen_random_uuid()` ở tầng DB.

### 2.6. Giả định & quyết định thiết kế cho các gap của URS

Đây là phần **quan trọng nhất** của SRS này — URS để hở nhiều chi tiết (có chủ đích, vì nhiều phần thuộc Should/Could Have). Bảng dưới tổng hợp toàn bộ quyết định đã thống nhất:

| # | Vấn đề URS để hở | Quyết định |
|---|---|---|
| 1 | Pause/Resume có đổi machine status hay tạo movement không | **Không** — chỉ ghi `work_order_events`, machine giữ nguyên `RUNNING` |
| 2 | Field list của `stock_lots` | `id, product_id, lot_number, lot_type_id, expiry_date, created_at` |
| 3 | Ngưỡng adjustment cần Factory Manager approve | Cấu hình tĩnh (`application.yml`), không có UI quản lý ngưỡng ở mức Must Have |
| 4 | Reserve nên lấy vật tư từ lot nào khi có nhiều lot | FIFO theo `created_at` của `stock_lots` |
| 5 | `READY_TO_PRODUCE` kích hoạt bởi hành động nào | **Đã resolve** — bỏ hẳn `MATERIAL_RESERVED`, Reserve thành công chuyển thẳng WO sang `READY_TO_PRODUCE` (xem mục 3.6, 3.7) |
| 6 | `work_order_event_types` gồm giá trị gì | `START, PAUSE, RESUME, COMPLETE` (suy từ FR-AUD-001) |
| 7 | Consume material tính theo `reserved` hay theo tỷ lệ `actual/planned` | Consume = toàn bộ `reservedQuantity` (không tính tỷ lệ) |
| 8 | `quality_inspections.status` khi xử lý pass/fail một phần | Giữ `PENDING_INSPECTION` cho tới khi `SUM(quality_inspection_results.quantity) = quality_inspections.quantity` |
| 9 | `REWORK_REQUIRED` (QC) không có giá trị tương ứng trong `stock_status` | Map tạm về `ON_HOLD` |
| 10 | `/quality-inspections/{id}/release` chuyển về đâu | `ON_HOLD → AVAILABLE` |
| 11 | Validate cộng dồn pass/fail vượt tổng quantity | Tự thêm check ở tầng service (URS chỉ check từng lần riêng lẻ) |
| 12 | `DAMAGED` (stock status) không có luồng nghiệp vụ | Không dùng ở mức Must Have, hoặc gán qua Stock Adjustment nếu cần |
| 13 | Locking cho Reserve Material / Start Production | Optimistic locking qua cột `version` (đã có ở `products`, `stock_balances`); Start Production dùng pessimistic lock (`SELECT FOR UPDATE`) trên `machines` do bảng này chưa có `version` |
| 14 | Machine `RETIRED` có được assign line/WO không | Không — coi như ngừng dùng vĩnh viễn, cùng logic với `product INACTIVE` |
| 15 | Maintenance ticket `RESOLVED` status dùng khi nào | Không bắt buộc dùng trong luồng thực thi — transition trực tiếp `IN_PROGRESS → CLOSED` theo đúng FR-MNT-003 |
| 16 | `referenceNo` (input Stock In) lưu ở đâu | **Gap còn mở** — hiện chưa có cột riêng trong `stock_movements`, cần quyết định thêm cột hoặc gộp vào `reason` |
| 17 | Bất biến của `stock_movements`/`audit_logs`, và rule chỉ-1-BOM-ACTIVE | **Không còn được DB enforce** (trigger đã bị gỡ theo yêu cầu) — bắt buộc phải enforce ở tầng service, cần unit test riêng đảm bảo không có code path nào update/delete 2 bảng ledger này |
| 18 | 1 Work Order có được chạy nhiều `production_runs` không (nhiều lần start, đổi machine giữa chừng) | **Gap còn mở** — state machine FR-WO-003 ngụ ý chỉ 1 run/WO, nhưng chưa thêm `UNIQUE (work_order_id)` trên `production_runs`, để ngỏ 1-nhiều dự phòng |
| 19 | Stock In: dùng lại lot cũ hay luôn tạo lot mới | **Cả 2** — Warehouse Manager chọn 1 trong 2: (a) chọn `lotNumber` đã tồn tại của product đó để cộng dồn quantity, hoặc (b) tạo `lotNumber` mới (qua hàm auto-generate, xem #20) |
| 20 | Thành phẩm sinh ra từ Complete Production có được dùng lại lot cũ không | **Không** — bắt buộc luôn tạo `lotNumber` mới, không có lựa chọn "chọn lot có sẵn" (khác Stock In) |
| 21 | Định dạng sinh `lotNumber` tự động | Hàm dùng chung `LotNumberGenerator.generate(productCode)` → `{productCode}-{yyyyMMdd}-{seq}`, VD `STEEL-20260727-A`. `seq` là ký tự A, B, C... tăng dần theo từng cặp (product, ngày) — lần đầu trong ngày là A, lần 2 là B... Dùng chung cho cả Stock In (khi chọn "tạo mới") lẫn Production Output (luôn bắt buộc) |
| 22 | Ai được tạo/start/close maintenance ticket | Tạo: {Maintenance Engineer, Production Operator, Planner, Factory Manager, Admin} — người phát hiện lỗi. Start/Close: chỉ {Maintenance Engineer, Factory Manager, Admin} — người trực tiếp bảo trì/quản lý. Mở rộng so với URS mục 3.6 (gốc chỉ trao cả 3 quyền cho Maintenance Engineer) |
| 23 | Ai đang đảm nhận 1 maintenance ticket cụ thể | Thêm cột `maintenance_tickets.assigned_engineer_id` (nullable) — không có trong URS, bổ sung để tránh phải join `audit_logs` khi hiển thị UI |

---

## 3. Yêu cầu chức năng (Functional Requirements)

> Định dạng mỗi module: **Actor** → **Business rule** (nguyên văn URS + bổ sung) → **Data model** (bảng liên quan trong SQL).

### 3.1. Authentication & Authorization

**Actor:** Mọi user (login), Admin (quản lý user/role).

**FR-AUTH-001 — Login**
- Input: `username, password`. Output: `accessToken, refreshToken (optional), user profile, roles, permissions`.
- Sai password → lỗi authentication failed. Đúng → JWT hợp lệ.

**FR-AUTH-002 — Role-based access control**
- API giới hạn theo role (VD: chỉ Admin tạo user, chỉ Planner tạo Work Order). Sai quyền → HTTP 403.
- **Quyết định triển khai:** Permission tính theo role qua map tĩnh trong code (`Map<Role, Set<Permission>>`) ở mức Must Have — không bắt buộc phải có UI quản lý permission động, dù bảng `permissions`/`role_permissions` đã có sẵn trong schema (kế thừa từ `diagram.puml`) cho hướng mở rộng sau này.

**Data model:** `users, roles, permissions, user_roles, role_permissions`

---

### 3.2. Master Data Management

**Actor:** Admin (CRUD), mọi role khác (read tùy quyền).

| FR | Entity | Business rule chính |
|---|---|---|
| FR-MST-001 | Product | Code unique; không xóa product đã có movement; hết dùng → INACTIVE; type ∈ {RAW_MATERIAL, SEMI_FINISHED, FINISHED_GOOD, CONSUMABLE, SPARE_PART} |
| FR-MST-002 | Warehouse | Code unique; không xóa warehouse đã có stock; hết dùng → INACTIVE |
| FR-MST-003 | Warehouse Location | Code unique **trong 1 warehouse**; không nhập hàng vào location INACTIVE |
| FR-MST-004 | Machine | Code unique; DOWN/UNDER_MAINTENANCE không dùng chạy WO; RETIRED không assign line/WO mới (bổ sung) |
| FR-MST-005 | Production Line | INACTIVE không assign WO |

**Data model:** `products, product_types, product_statuses, units_of_measure, warehouses, warehouse_statuses, warehouse_managers (bổ sung), warehouse_locations, location_statuses, production_lines, line_statuses, machines, machine_statuses`

---

### 3.3. Inventory & Warehouse Management

**Actor:** Warehouse Manager.

**FR-INV-001 — Stock Balance**
- Quản lý theo `(warehouse, location, product, lot, status)` — 8 status: `AVAILABLE, RESERVED, ON_HOLD, QUALITY_INSPECTION, DAMAGED, SCRAPPED, CONSUMED, SHIPPED`.
- Không âm quantity; không update trực tiếp nếu không kèm stock movement.
- Khóa duy nhất: `(warehouse_id, location_id, product_id, lot_id, stock_status_id)`.

**FR-INV-002 — Stock In**
- Input: `productId, warehouseId, locationId, lotNumber (optional), quantity, reason, referenceNo`.
- Quantity > 0; tạo movement `PURCHASE_IN`.
- **2 trường hợp chọn lot (bổ sung, làm rõ hơn URS gốc):**
  1. **Chọn lot đã có** — Warehouse Manager chọn 1 `lotNumber` đã tồn tại của đúng product đó → cộng dồn `quantity` vào `stock_balances` của lot đó.
  2. **Tạo lot mới** — không chọn lot có sẵn → hệ thống gọi `LotNumberGenerator.generate()` (xem bên dưới) để tự sinh `lotNumber` mới, tạo dòng `stock_lots` + `stock_balances` mới.

**Quy tắc sinh Lot Number tự động (dùng chung toàn hệ thống)**

Hàm tái sử dụng: `LotNumberGenerator.generate(productCode)` → định dạng:
```
{productCode}-{yyyyMMdd}-{seq}
```
- `productCode`: mã sản phẩm (`products.code`).
- `yyyyMMdd`: ngày hiện tại lúc sinh lot.
- `seq`: ký tự chữ cái `A, B, C...`, tăng dần theo từng cặp `(productCode, ngày)` — lần đầu trong ngày là `A`, lần thứ 2 (cùng product, cùng ngày) là `B`, v.v. Reset lại từ `A` khi sang ngày mới.

**Ví dụ:** `STEEL-20260727-A` (lot thép đầu tiên tạo ngày 27/07/2026).

Hàm này dùng chung cho **2 nơi**:
- Stock In, khi Warehouse Manager chọn "tạo lot mới" (trường hợp 2 ở trên).
- Production Output (FR-PROD-003) — **bắt buộc**, không có lựa chọn tái sử dụng lot cũ (xem bên dưới).

```java
public interface LotNumberGenerator {
    String generate(String productCode); // trả về "STEEL-20260727-A"
}
```
Cài đặt cần transaction-safe (tránh 2 request cùng lúc sinh trùng `seq` trong cùng ngày) — có thể dùng `SELECT ... FOR UPDATE` trên 1 bảng đếm sequence riêng theo `(productCode, date)`, hoặc dựa vào `UNIQUE (lot_number)` đã có sẵn trên `stock_lots` để retry nếu trùng.

**FR-INV-003 — Stock Adjustment**
- Input: `stockBalanceId, adjustmentQuantity, reason` (bắt buộc reason).
- Không âm; tạo movement `ADJUSTMENT`; vượt ngưỡng cấu hình → cần Factory Manager approve (workflow tối giản ở Must Have — xem mục 2.6 #3).

**FR-INV-004 — Stock Transfer**
- Input: `fromWarehouseId, fromLocationId, toWarehouseId, toLocationId, productId, lotId, quantity`.
- Chỉ transfer stock AVAILABLE; nguồn đủ hàng; tạo cặp movement `TRANSFER_OUT` + `TRANSFER_IN` trong 1 transaction.
- **Bổ sung:** không có `reason` bắt buộc (khác Adjustment); nên chặn thêm transfer vào location INACTIVE dù không có FR nào nói trực tiếp.

**Data model:** `stock_balances, stock_lots, lot_types`

---

### 3.4. Stock Movement Ledger

**Actor:** Hệ thống (tự động, mọi module khác gọi vào).

**FR-MOV-001 — Create Stock Movement**
- 14 movement type: `PURCHASE_IN, TRANSFER_IN, TRANSFER_OUT, RESERVE, RELEASE_RESERVATION, ISSUE_TO_PRODUCTION, CONSUME_IN_PRODUCTION, PRODUCTION_OUTPUT, QC_HOLD, QC_RELEASE, SCRAP, ADJUSTMENT, SHIP_OUT, RETURN_TO_WAREHOUSE`.
- Immutable — không update/delete (⚠️ hiện **không** được DB enforce, xem mục 2.6 #17). Quantity > 0.

**Thiết kế cột vị trí (khác URS field list gốc):**
Thay vì `warehouseId/locationId` đơn, bảng dùng `from_warehouse_id/from_location_id/to_warehouse_id/to_location_id` — cho phép 1 dòng movement tự mô tả đủ cả nguồn lẫn đích (đặc biệt quan trọng với Transfer), quy ước theo loại:

| Movement type | from | to |
|---|---|---|
| PURCHASE_IN | NULL | có |
| TRANSFER_OUT / TRANSFER_IN | có | có (2 dòng cùng giá trị) |
| RESERVE / RELEASE_RESERVATION / QC_HOLD / QC_RELEASE / ADJUSTMENT | có | = from (không đổi vị trí) |
| CONSUME_IN_PRODUCTION / SCRAP / SHIP_OUT | có | NULL |
| PRODUCTION_OUTPUT / RETURN_TO_WAREHOUSE | NULL | có |

**Truy vết nguồn gốc nghiệp vụ:** dùng cột `work_order_id` (FK thật tới `work_orders`, thay cho `referenceType/referenceId` generic của URS gốc) — áp dụng cho `RESERVE, RELEASE_RESERVATION, ISSUE_TO_PRODUCTION, CONSUME_IN_PRODUCTION, PRODUCTION_OUTPUT, QC_HOLD, QC_RELEASE, SCRAP`; để NULL với 6 loại còn lại không gắn Work Order nào.

**Data model:** `stock_movements, movement_types`

---

### 3.5. BOM Management

**Actor:** Planner.

**FR-BOM-001 — Create BOM**
- BOM chỉ áp dụng FINISHED_GOOD/SEMI_FINISHED; item phải là RAW_MATERIAL/SEMI_FINISHED/CONSUMABLE; `quantityPerUnit > 0`.
- `scrapRate` — DB hiện quy ước `NOT NULL DEFAULT 0` (khác URS "optional") để đơn giản hóa công thức tính (không cần check NULL).

**FR-BOM-002 — Activate BOM**
- Chỉ 1 version ACTIVE/product tại 1 thời điểm (⚠️ hiện **không** được DB enforce, xem mục 2.6 #17). Không activate BOM rỗng.

**FR-BOM-003 — BOM Versioning**
- BOM đã dùng trong WO → không sửa trực tiếp, phải tạo version mới.

**Data model:** `boms, bom_statuses, bom_items, units_of_measure`

---

### 3.6. Work Order Management

**Actor:** Planner (tạo/cancel), hệ thống (tính requirement, kiểm soát transition).

**FR-WO-001 — Create Work Order**
- `plannedQuantity > 0`; sản phẩm phải có BOM ACTIVE; status khởi tạo DRAFT/PLANNED; không xóa WO đã có movement.

**FR-WO-002 — Calculate Material Requirement**
```
requiredQuantity = plannedQuantity × quantityPerUnit × (1 + scrapRate)
```

**FR-WO-003 — Status Transition**

```
DRAFT → PLANNED → READY_TO_PRODUCE / MATERIAL_SHORTAGE
MATERIAL_SHORTAGE → READY_TO_PRODUCE → IN_PROGRESS ⇄ PAUSED → COMPLETED
(CANCELLED khả dụng từ PLANNED / MATERIAL_SHORTAGE / READY_TO_PRODUCE)
```
- **Thay đổi so với URS gốc:** bỏ hẳn trạng thái `MATERIAL_RESERVED` — Reserve Material thành công (FR-RES-001) chuyển thẳng WO sang `READY_TO_PRODUCE` (không qua bước trung gian nào). Đây là cách resolve gap "READY_TO_PRODUCE kích hoạt khi nào" (mục 2.6 #5) — nay READY_TO_PRODUCE chính là trạng thái "đã reserve xong, sẵn sàng chạy", không còn 2 trạng thái chồng chéo ý nghĩa như URS gốc.
- Transition không hợp lệ → reject. Mọi transition quan trọng → audit log.
- **Thiết kế mở rộng:** bảng `work_order_status_transitions` lưu transition hợp lệ dưới dạng dữ liệu (data-driven), có cột `is_initial`/`is_final` trên `work_order_statuses` để xác định trạng thái đầu/cuối — không có trong URS, là cải tiến kiến trúc tự thêm. **Lưu ý khi seed dữ liệu:** không insert row `MATERIAL_RESERVED` vào `work_order_statuses`.

**Data model:** `work_orders, work_order_statuses, work_order_status_transitions, work_order_priorities, work_order_materials, production_runs, work_order_events, work_order_event_types`

---

### 3.7. Material Reservation

**Actor:** Planner.

**FR-RES-001 — Reserve**
- Đọc BOM → tính required → chỉ reserve nếu AVAILABLE đủ → chuyển AVAILABLE→RESERVED (ở `stock_balances`), tạo movement RESERVE, **WO chuyển sang `READY_TO_PRODUCE`** (đã bỏ `MATERIAL_RESERVED`, xem mục 3.6). Thiếu → WO chuyển `MATERIAL_SHORTAGE`, không reserve gì, trả lỗi rõ thiếu bao nhiêu.
- **Chiến lược chọn lot khi có nhiều lot cùng product:** FIFO theo `stock_lots.created_at` (gap tự quyết định).

**FR-RES-002 — Release**
- Chỉ release khi RESERVED; chuyển về AVAILABLE; tạo movement RELEASE_RESERVATION; không release nếu WO đã IN_PROGRESS/COMPLETED.
- Nếu reserve trải nhiều lot: release phải truy lại từng dòng `stock_movements` loại RESERVE (lọc theo `work_order_id`) để trả đúng lot, không chỉ dựa vào tổng `work_order_materials.reservedQuantity`.

**FR-RES-003 — Concurrent Reservation**
- Không âm stock; không để reserved vượt on-hand; không duplicate movement do race condition.
- **Test case bắt buộc (TC-003):** 20 request đồng thời reserve 1 đơn vị tồn 10 → đúng 10 success, 10 failed.
- **Locking:** optimistic (`stock_balances.version`) hoặc pessimistic (`SELECT FOR UPDATE`) — nhóm phải chọn và giải thích trade-off trong README.

**Data model:** `work_order_materials, stock_balances, stock_movements`

---

### 3.8. Production Execution

**Actor:** Production Operator.

**FR-PROD-001 — Start Production**
- Input: `workOrderId, machineId, productionLineId, operatorId`.
- WO phải `READY_TO_PRODUCE` (đã bỏ `MATERIAL_RESERVED`, xem mục 3.6); machine AVAILABLE (không DOWN/UNDER_MAINTENANCE); 1 machine không chạy 2 WO cùng lúc (concurrency case #2, cần lock riêng).
- WO→IN_PROGRESS, machine→RUNNING, reserved material→ISSUE_TO_PRODUCTION/CONSUME_IN_PRODUCTION (tùy thiết kế).
- **Ghi dữ liệu:** INSERT 1 dòng `production_runs` (`work_order_id, machine_id, production_line_id, operator_id, start_time=now()`) + INSERT 1 dòng `work_order_events` (event=START, `production_run_id` trỏ về run vừa tạo).

**FR-PROD-002 — Pause/Resume**
- Chỉ pause khi IN_PROGRESS, chỉ resume khi PAUSED. Mỗi action tạo production event.
- **Quyết định (gap):** không đổi machine status, không tạo stock movement — chỉ INSERT `work_order_events` (event=PAUSE/RESUME, gắn `production_run_id` của run đang chạy). **Không đụng vào `production_runs`** — bảng này chỉ có 2 thời điểm ghi/sửa duy nhất là Start (INSERT) và Complete (UPDATE), xem bên dưới.

**FR-PROD-003 — Complete Production**
- Input: `workOrderId, actualQuantity, goodQuantity, defectQuantity, scrapQuantity, note`. `good+defect+scrap = actual`.
- Transaction: consume reserved material (= toàn bộ reservedQuantity, không tính tỷ lệ actual/planned) → movement CONSUME_IN_PRODUCTION → tạo finished goods lot (`lot_type = FINISHED_GOODS_LOT`, **luôn dùng `LotNumberGenerator.generate()` để sinh `lotNumber` mới — không có lựa chọn tái sử dụng lot cũ**, khác Stock In) → movement PRODUCTION_OUTPUT → tạo QC inspection (PENDING_INSPECTION) → WO→COMPLETED → machine→AVAILABLE.
- **Ghi dữ liệu:** UPDATE dòng `production_runs` tương ứng (`end_time=now()`, `actual/good/defect/scrap_quantity`) + INSERT `work_order_events` (event=COMPLETE).

**Thiết kế tách bảng (khác `diagram.puml` gốc, đúng theo URS mục 9):**

| Bảng | Vai trò | Ghi/sửa khi nào |
|---|---|---|
| `production_runs` | Snapshot 1 lần thực thi — machine, thời gian, kết quả tổng | INSERT tại Start; UPDATE duy nhất tại Complete |
| `work_order_events` | Ledger từng thao tác rời rạc (không có quantity) | INSERT tại mỗi action: START/PAUSE/RESUME/COMPLETE |

`diagram.puml` ban đầu gộp 2 bảng này làm 1 (`work_order_events` có sẵn cột machine/quantity) — đã tách lại theo đúng tên 2 bảng riêng biệt mà URS liệt kê ở mục 9. **Quyết định còn mở:** chưa thêm `UNIQUE (work_order_id)` trên `production_runs` — vì state machine URS (FR-WO-003) không có transition nào cho phép 1 WO chạy lại/chuyển máy giữa chừng, về lý thuyết mỗi WO chỉ có đúng 1 run, nhưng để ngỏ (1-nhiều) để dự phòng nếu rule thay đổi sau này.

**Data model:** `production_runs, work_order_events, work_order_event_types, machines, production_lines, stock_lots, quality_inspections`

---

### 3.9. Quality Control

**Actor:** QC Inspector.

**FR-QC-001 — Auto-create Inspection**
- Sau Complete Production → tự động tạo `quality_inspections` (PENDING_INSPECTION). Chưa PASSED → không AVAILABLE, không ship out.
- Chỉ áp dụng cho **output của Work Order** (bán thành phẩm/thành phẩm) — **không** áp dụng cho nguyên vật liệu Stock In.

**FR-QC-002 — Pass QC**
- `passedQuantity > 0`, không vượt `quantity` còn lại. Chuyển QUALITY_INSPECTION→AVAILABLE, tạo movement QC_RELEASE.

**FR-QC-003 — Fail QC**
- Bắt buộc `defectType + reason`. `action ∈ {HOLD, REWORK, SCRAP}` → HOLD: ON_HOLD; SCRAP: SCRAPPED (+movement SCRAP); REWORK: `quality_inspections.status = REWORK_REQUIRED` (không có stock_status tương ứng, xem mục 2.6 #9).

**Thiết kế bảng kết quả:** `quality_inspection_results` gộp chung Pass/Fail (`is_pass` boolean), 1 `quality_inspection` có thể có **nhiều** dòng kết quả (xử lý một phần qua nhiều lần gọi). `CHECK` đảm bảo Fail luôn có `defect_type_id + reason`.

**Data model:** `quality_inspections, qc_statuses, quality_inspection_results, defect_types`

---

### 3.10. Maintenance Management

**Actor:** Tạo ticket — {Maintenance Engineer, Production Operator, Planner, Factory Manager, Admin} (người phát hiện lỗi). Start/Close ticket — chỉ {Maintenance Engineer, Factory Manager, Admin} (bổ sung so với URS mục 3.6, gốc chỉ cho Maintenance Engineer cả 3 quyền).

**FR-MNT-001 — Create Ticket**
- Type: `PREVENTIVE, CORRECTIVE, EMERGENCY, INSPECTION`. Status: `OPEN, IN_PROGRESS, RESOLVED, CLOSED, CANCELLED`.
- Machine DOWN phải có ticket OPEN/IN_PROGRESS đi kèm.
- **Bổ sung:** cột `assigned_engineer_id` (nullable, FK `users`) — khác `created_by` (ai tạo ticket, có thể là Operator/Planner phát hiện lỗi) — dùng để biết **ai đang phụ trách xử lý**, phục vụ UI filter "ticket của tôi" mà không cần join `audit_logs`. Có thể để trống lúc tạo, Maintenance Engineer tự nhận khi Start.

**FR-MNT-002 — Start Maintenance**
- Chỉ Maintenance Engineer/Factory Manager/Admin được gọi. Chỉ start ticket OPEN → IN_PROGRESS; machine→UNDER_MAINTENANCE; downtime bắt đầu ghi nhận; nên set `assigned_engineer_id = người gọi API` nếu đang trống.

**FR-MNT-003 — Close Maintenance**
- Chỉ Maintenance Engineer/Factory Manager/Admin được gọi. Bắt buộc `actionTaken`. Ticket→CLOSED; machine→AVAILABLE/DOWN; downtime end time cập nhật.
- **Quyết định (gap):** `RESOLVED` không dùng trong luồng chính (transition thẳng `IN_PROGRESS → CLOSED`).

**Data model:** `maintenance_tickets (+ assigned_engineer_id), maintenance_ticket_types, maintenance_ticket_statuses, maintenance_ticket_priorities, machine_downtimes`

---

### 3.11. Reporting

**Actor:** Factory Manager (chủ yếu).

| FR | Report | Output chính |
|---|---|---|
| FR-RPT-001 | Inventory Summary | productCode, warehouse, available/reserved/QI/onHold/scrapped quantity, totalOnHand |
| FR-RPT-002 | Material Shortage | workOrderCode, materialCode, required/available/shortage quantity |
| FR-RPT-003 | Production Output | plannedQuantity, actualQuantity, good/defect/scrap, completionRate |
| FR-RPT-004 | Defect Rate | totalProduced, defectRate, topDefectTypes |
| FR-RPT-005 | Machine Downtime | totalDowntimeMinutes, ticketCount, lastDowntimeReason |
| FR-RPT-006 | Stock Movement History | filter đa điều kiện, output đầy đủ từng dòng movement |

Đặc điểm: chỉ đọc (không business rule/transaction), toàn bộ là query tổng hợp từ dữ liệu module khác — không có bảng riêng.

---

### 3.12. Audit Log

**Actor:** Hệ thống (tự động ghi), Admin/Factory Manager/Auditor (xem).

**FR-AUD-001** — Ghi log cho 17 action: `CREATE_WORK_ORDER, RESERVE_MATERIAL, RELEASE_RESERVATION, START_PRODUCTION, PAUSE_PRODUCTION, RESUME_PRODUCTION, COMPLETE_PRODUCTION, QC_PASS, QC_FAIL, QC_HOLD, QC_RELEASE, SCRAP_STOCK, CREATE_MAINTENANCE_TICKET, START_MAINTENANCE, CLOSE_MAINTENANCE_TICKET, ADJUST_STOCK, ACTIVATE_BOM`.
- Không sửa/xóa (⚠️ không được DB enforce, xem mục 2.6 #17). Action quan trọng thiếu log = chưa đạt requirement.

**Data model:** `audit_logs`

---

## 4. Thiết kế dữ liệu (Data Model)

### 4.1. Tổng quan theo domain

```
AUTH            → users, roles, permissions, user_roles, role_permissions
MASTER DATA     → products (+ types/statuses/UOM), warehouses (+ managers/locations),
                   production_lines, machines
INVENTORY       → stock_lots (+ lot_types), stock_balances (+ statuses)
BOM             → boms (+ statuses), bom_items
WORK ORDER      → work_orders (+ statuses/transitions/priorities),
                   stock_movements (+ movement_types), work_order_materials,
                   production_runs, work_order_events (+ event_types)
QC              → quality_inspections (+ statuses), defect_types,
                   quality_inspection_results
MAINTENANCE     → maintenance_tickets (+ types/statuses/priorities), machine_downtimes
AUDIT           → audit_logs, idempotency_keys
```

### 4.2. Danh mục 45 bảng (tóm tắt)

| Nhóm | Bảng | Vai trò |
|---|---|---|
| Auth | `users` | Tài khoản đăng nhập |
| | `roles`, `permissions`, `user_roles`, `role_permissions` | RBAC (permission tables kế thừa diagram.puml, không có trong URS mục 9) |
| Master Data | `products`, `product_types`, `product_statuses` | Sản phẩm/vật tư |
| | `units_of_measure` | Đơn vị tính (bổ sung theo URS mục 9) |
| | `warehouses`, `warehouse_statuses`, `warehouse_managers` (bổ sung) | Kho |
| | `warehouse_locations`, `location_statuses` | Vị trí trong kho |
| | `production_lines`, `line_statuses` | Dây chuyền |
| | `machines`, `machine_statuses` | Máy móc |
| Inventory | `stock_lots`, `lot_types` (bổ sung) | Lô hàng — `lot_number` **unique toàn hệ thống** (không chỉ theo product); sinh tự động qua `LotNumberGenerator` (mục 3.3) |
| | `stock_balances`, `stock_statuses` | Tồn kho hiện tại (snapshot) |
| | `movement_types` | Danh mục loại biến động |
| BOM | `boms`, `bom_statuses`, `bom_items` | Công thức sản xuất |
| Work Order | `work_orders`, `work_order_statuses`, `work_order_status_transitions` (bổ sung), `work_order_priorities` (bổ sung) | Lệnh sản xuất |
| | `stock_movements` | Ledger biến động kho (bất biến) |
| | `work_order_materials` | Snapshot vật tư theo từng WO |
| | `production_runs` | Snapshot 1 lần thực thi — machine, thời gian, kết quả tổng (tách lại đúng theo URS mục 9, `diagram.puml` gốc đã gộp nhầm vào `work_order_events`) |
| | `work_order_events`, `work_order_event_types` | Ledger từng thao tác rời rạc (START/PAUSE/RESUME/COMPLETE), không chứa quantity |
| QC | `quality_inspections`, `qc_statuses` | Phiếu chờ kiểm |
| | `defect_types` | Danh mục loại lỗi |
| | `quality_inspection_results` | Từng quyết định pass/fail |
| Maintenance | `maintenance_tickets` (+ `assigned_engineer_id`, bổ sung), `maintenance_ticket_types`, `maintenance_ticket_statuses`, `maintenance_ticket_priorities` (bổ sung) | Phiếu bảo trì — tạo bởi nhiều role (mục 3.10), start/close giới hạn 3 role |
| | `machine_downtimes` | Thời gian dừng máy |
| Audit | `audit_logs` | Nhật ký hệ thống |
| | `idempotency_keys` | Chống trùng request (Could Have) |

*(DDL đầy đủ với constraint/index: xem `V1__init_schema.sql`.)*

### 4.3. Design pattern áp dụng xuyên suốt

| Pattern | Áp dụng ở | Mục đích |
|---|---|---|
| Ledger (bất biến, chỉ INSERT) | `stock_movements`, `audit_logs` | Lịch sử không thể chối cãi, phục vụ auditability |
| Snapshot (mutable có kỷ luật) | `stock_balances`, `work_order_materials` | Đọc nhanh trạng thái hiện tại, luôn kèm bằng chứng ở bảng ledger |
| Lookup table cho status/type | Toàn bộ `*_statuses`, `*_types`, `*_priorities` | Không hard-code enum, dễ mở rộng giá trị mới không cần deploy |
| Polymorphic → chuyển sang FK cụ thể | `stock_movements.work_order_id` (thay cho `reference_type/reference_id` gốc) | Đánh đổi: mất khả năng generic, đổi lại có FK constraint thật, type-safe hơn |
| Data-driven state machine | `work_order_status_transitions` | Transition hợp lệ là dữ liệu, không hard-code trong code |
| Composite from/to thay vì cột đơn | `stock_movements` (warehouse/location) | 1 dòng tự mô tả đủ nguồn+đích, không cần bảng phụ để ghép cặp Transfer |

### 4.4. Giới hạn đã biết (Known Limitations)

1. **Không có composite FK** đảm bảo `location_id` khớp đúng `warehouse_id` trên `stock_balances`/`stock_movements` — rủi ro dữ liệu lệch nếu code tầng service viết sai (đề xuất đã có, chưa áp dụng).
2. **Không có unique constraint** chống trùng vật tư trong 1 BOM (`bom_items`) — đề xuất đã có, chưa áp dụng.
3. **Tính bất biến của ledger và rule 1-BOM-ACTIVE không được DB enforce** (trigger đã gỡ bỏ) — bắt buộc kiểm soát ở tầng service, nên có test riêng.
4. **`warehouse_managers`** chỉ lưu quan hệ, **không tự động giới hạn quyền** — cần code thêm ở tầng service nếu muốn thực sự enforce.
5. **`referenceNo`** (input Stock In) chưa có cột lưu trữ riêng trong `stock_movements`.

---

## 5. Yêu cầu giao diện ngoài

### 5.1. Quy ước định dạng response

Response thành công:
```json
{ "success": true, "data": {}, "message": "Success", "timestamp": "..." }
```
Response lỗi:
```json
{ "success": false, "errorCode": "INSUFFICIENT_STOCK", "message": "...", "details": {}, "timestamp": "..." }
```

### 5.2. UI bắt buộc (13 page)

Login, Dashboard, Product/Material Management, Warehouse & Stock Balance, Stock Movement History, BOM Management, Work Order List/Detail, Material Reservation Screen, Production Execution Screen, QC Inspection Screen, Maintenance Ticket Screen, Reports Screen.

Yêu cầu tối thiểu: route guard theo login, menu theo role, form validation cơ bản, table + pagination, hiển thị lỗi từ backend. Không cần chart phức tạp/realtime/drag-drop.

---

## 6. Yêu cầu phi chức năng

### 6.1. Transaction

Bắt buộc transaction cho: Stock In, Adjustment, Transfer, Reserve, Release, Start Production, Complete Production, QC Pass/Fail, Maintenance Start/Close, Cancel Work Order.

### 6.2. Concurrency

| Case | Cơ chế đề xuất |
|---|---|
| Nhiều WO reserve cùng material | Optimistic lock qua `stock_balances.version` |
| Nhiều user adjustment cùng stock balance | Cùng cơ chế với reserve |
| Nhiều operator start cùng machine | Pessimistic lock (`SELECT FOR UPDATE`) trên `machines` |
| QC release và stock operation gần đồng thời | Optimistic lock qua `stock_balances.version` |

Bắt buộc: test case chứng minh không stock âm (TC-003), giải thích trade-off trong README.

### 6.3. Security

JWT + Spring Security; password hash BCrypt; API phân quyền theo role; không expose password hash; user thường không gọi được API admin.

### 6.4. Testing bắt buộc

TC-001 → TC-008 (Reserve thành công/thiếu/concurrent, Cancel release material, Machine maintenance, Complete production, QC fail requires reason, QC pass → available). Xem chi tiết Given/When/Then trong `FactoryFlow_URS.md` mục 11.

### 6.5. Auditability

Mọi action trong FR-AUD-001 phải có audit log. Ledger (`stock_movements`) phải cho phép trả lời đầy đủ "ai/khi nào/vì sao" cho Auditor.

---

## 7. Phụ lục

### 7.1. Scope phân cấp (Must/Should/Could Have)

Xem `FactoryFlow_URS.md` mục 12 — không lặp lại. Lưu ý: các bảng/cột **bổ sung** trong SRS này (`warehouse_managers`, `lot_types`, `*_priorities`, `units_of_measure`, `work_order_status_transitions`) đều **không thuộc Must Have** của URS gốc — là cải tiến kiến trúc tự thêm, cần cân nhắc thời gian trước khi đầu tư công sức.

### 7.2. Definition of Done

Một requirement chỉ done khi: API implemented, validation implemented, authorization checked, business rule implemented, transaction handled, DB migration completed, Swagger documented, test added, UI integrated (nếu cần demo), error handling rõ ràng, demo bằng dữ liệu thật.

### 7.3. Rubric liên quan trực tiếp tới phần Data Model của SRS này

| Tiêu chí | Điểm | Liên quan trực tiếp tới |
|---|---|---|
| Database design | 20 | Mục 4 — FK, unique constraint, index, ledger pattern |
| Transaction & concurrency | 15 | Mục 6.2 |
| Business rule correctness | 15 | Mục 3 (từng FR) + mục 4.4 (giới hạn cần bù ở tầng service) |
