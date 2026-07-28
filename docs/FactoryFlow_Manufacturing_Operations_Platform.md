# User Requirement Specification
# FactoryFlow — Manufacturing Operations Platform

## 1. Tổng quan đề bài

### 1.1. Tên project
FactoryFlow — Manufacturing Operations Platform

### 1.2. Mục tiêu
Xây dựng một hệ thống quản lý vận hành cơ bản cho nhà máy, hỗ trợ các nghiệp vụ chính:

- Quản lý master data: sản phẩm, nguyên vật liệu, kho, máy móc, dây chuyền.
- Quản lý tồn kho theo kho, vị trí, lô hàng.
- Quản lý stock movement như nhập kho, xuất kho, reserve, release, consume, scrap.
- Quản lý BOM — Bill of Materials.
- Quản lý lệnh sản xuất — Work Order.
- Cấp phát / xuất nguyên vật liệu cho sản xuất.
- Thực thi sản xuất trên máy/dây chuyền.
- Kiểm tra chất lượng sau sản xuất.
- Quản lý bảo trì máy móc cơ bản.
- Báo cáo vận hành nhà máy.

### 1.3. Trọng tâm đánh giá
Project này không tập trung vào UI đẹp. Trọng tâm là:

- Java Spring Boot backend
- PostgreSQL database design
- Transaction
- Concurrency control
- Business rule correctness
- Stock consistency
- Auditability
- Authorization
- Report query
- Code quality ở mức middle

### 1.4. Stack bắt buộc
- **Backend:** Java, Spring Boot
- **Frontend:** Angular
- **Database:** PostgreSQL
- **Migration:** Flyway hoặc Liquibase
- **Authentication:** Spring Security + JWT
- **API docs:** Swagger/OpenAPI
- **Container:** Docker Compose
- **Testing:** JUnit 5, Mockito, optional Testcontainers

---

## 2. Context nghiệp vụ

Một nhà máy nhỏ cần hệ thống để quản lý quá trình sản xuất từ nguyên vật liệu đến thành phẩm.

**Luồng nghiệp vụ chính:**

```
Nguyên vật liệu được nhập kho
        ↓
Planner tạo BOM cho thành phẩm
        ↓
Planner tạo Work Order sản xuất
        ↓
Hệ thống tính nguyên vật liệu cần dùng
        ↓
Hệ thống reserve nguyên vật liệu trong kho
        ↓
Operator bắt đầu sản xuất
        ↓
Hệ thống consume nguyên vật liệu
        ↓
Operator nhập kết quả sản xuất
        ↓
Hệ thống tạo thành phẩm ở trạng thái chờ QC
        ↓
QC Inspector kiểm tra chất lượng
        ↓
Hàng đạt QC chuyển thành AVAILABLE
        ↓
Factory Manager xem báo cáo
```

**Ngoài flow sản xuất, hệ thống còn cần quản lý:**

- Bảo trì máy móc.
- Máy đang hỏng hoặc bảo trì không được dùng sản xuất.
- Stock movement phải có lịch sử.
- Không được để tồn kho âm.
- Không được xuất hàng chưa qua QC.

---

## 3. Actor và quyền hạn

### 3.1. Admin
Admin có quyền:
- Quản lý user.
- Gán role cho user.
- Quản lý master data.
- Xem toàn bộ dữ liệu hệ thống.

### 3.2. Warehouse Manager
Warehouse Manager có quyền:
- Nhập kho.
- Điều chỉnh tồn kho.
- Xem stock balance.
- Xem stock movement history.
- Thực hiện stock transfer.
- Xử lý stock adjustment.

### 3.3. Planner
Planner có quyền:
- Tạo BOM.
- Tạo Work Order.
- Reserve nguyên vật liệu.
- Release nguyên vật liệu đã reserve.
- Cancel Work Order.
- Xem tình trạng thiếu vật tư.

### 3.4. Production Operator
Production Operator có quyền:
- Xem Work Order được assign.
- Start production.
- Pause/resume production.
- Complete production.
- Nhập actual quantity, defect quantity, scrap quantity.

### 3.5. QC Inspector
QC Inspector có quyền:
- Xem danh sách lô hàng chờ kiểm tra.
- Pass QC.
- Fail QC.
- Hold stock.
- Scrap stock.

### 3.6. Maintenance Engineer
Maintenance Engineer có quyền:
- Ghi nhận defect type và defect reason.
- Tạo maintenance ticket.
- Start maintenance.
- Close maintenance.
- Cập nhật trạng thái máy.
- Ghi nhận downtime.

### 3.7. Factory Manager
Factory Manager có quyền:
- Xem dashboard.
- Xem report.
- Approve các adjustment lớn nếu có.
- Xem audit log.

### 3.8. Auditor
Auditor có quyền:
- Chỉ đọc dữ liệu.
- Xem stock movement.
- Xem audit log.
- Xem lịch sử thay đổi quan trọng.

---

## 4. Module chức năng

Project gồm các module chính sau:

1. Authentication & Authorization
2. Master Data Management
3. Inventory & Warehouse Management
4. Stock Movement Ledger
5. BOM Management
6. Work Order Management
7. Material Reservation
8. Production Execution
9. Quality Control
10. Maintenance Management
11. Reporting
12. Audit Log

---

## 5. Functional Requirements

### 5.1. Authentication & Authorization

#### FR-AUTH-001 — Login
Hệ thống phải cho phép user đăng nhập bằng username/password.

**Input:**
- username
- password

**Output:**
- accessToken
- refreshToken (optional)
- user profile
- roles
- permissions

**Acceptance criteria:**
- Given user nhập đúng username/password, When gọi API login, Then hệ thống trả về JWT token hợp lệ.
- Given user nhập sai password, When gọi API login, Then hệ thống trả về lỗi authentication failed.

#### FR-AUTH-002 — Role-based access control
Hệ thống phải giới hạn API theo role.

**Ví dụ:**
- Chỉ Admin được tạo user.
- Chỉ Planner được tạo Work Order.
- Chỉ QC Inspector được pass/fail QC.
- Chỉ Maintenance Engineer được close maintenance ticket.

**Acceptance criteria:**
- Given user không có quyền, When gọi API bị giới hạn, Then hệ thống trả về HTTP 403 Forbidden.

### 5.2. Master Data Management

#### FR-MST-001 — Product/Material Management
Hệ thống phải cho phép quản lý sản phẩm và nguyên vật liệu.

**Product có các thông tin:**
- id, code, name, type, unit, status, createdAt, createdBy, updatedAt, updatedBy, version

**Product type:**
- RAW_MATERIAL
- SEMI_FINISHED
- FINISHED_GOOD
- CONSUMABLE
- SPARE_PART

**Business rule:**
- Product code là unique.
- Không được xóa product đã phát sinh stock movement.
- Product không dùng nữa thì chuyển status thành INACTIVE.

**Acceptance criteria:**
- Given Admin tạo product với code chưa tồn tại, Then hệ thống tạo product thành công.
- Given Admin tạo product với code đã tồn tại, Then hệ thống trả lỗi duplicate product code.

#### FR-MST-002 — Warehouse Management
Hệ thống phải cho phép quản lý kho.

**Warehouse có các thông tin:**
- id, code, name, address, status, createdAt, updatedAt

**Business rule:**
- Warehouse code là unique.
- Không được xóa warehouse đã có stock.
- Warehouse không còn dùng thì chuyển status INACTIVE.

#### FR-MST-003 — Warehouse Location Management
Hệ thống phải cho phép quản lý vị trí trong kho.

**Location có các thông tin:**
- id, warehouseId, code, name, status

**Ví dụ:**
- WH-HN / A01
- WH-HN / A02
- WH-HCM / B01

**Business rule:**
- Location code unique trong cùng một warehouse.
- Không được nhập hàng vào location INACTIVE.

#### FR-MST-004 — Machine Management
Hệ thống phải cho phép quản lý máy móc.

**Machine có các thông tin:**
- id, code, name, productionLineId, status, createdAt, updatedAt

**Machine status:**
- AVAILABLE
- RUNNING
- DOWN
- UNDER_MAINTENANCE
- RETIRED

**Business rule:**
- Machine code là unique.
- Machine DOWN hoặc UNDER_MAINTENANCE không được dùng để chạy Work Order.

#### FR-MST-005 — Production Line Management
Hệ thống phải cho phép quản lý dây chuyền sản xuất.

**Production line có các thông tin:**
- id, code, name, status

**Business rule:**
- Production line INACTIVE không được assign Work Order.

### 5.3. Inventory & Warehouse Management

#### FR-INV-001 — Stock Balance
Hệ thống phải quản lý tồn kho theo: warehouse, location, product, lot number, stock status, quantity.

**Stock status:**
- AVAILABLE
- RESERVED
- ON_HOLD
- QUALITY_INSPECTION
- DAMAGED
- SCRAPPED
- CONSUMED
- SHIPPED

**Stock balance cần có:**
- id, warehouseId, locationId, productId, lotId, status, quantity, version, createdAt, updatedAt

**Business rule:**
- Không được để quantity âm.
- Không được update stock balance trực tiếp nếu không tạo stock movement.
- Một product có thể tồn tại ở nhiều warehouse, location, lot và status khác nhau.

**Acceptance criteria:**
- Given stock balance quantity = 10, When hệ thống thực hiện movement xuất 3, Then quantity còn 7.
- Given stock balance quantity = 2, When hệ thống xuất 5, Then hệ thống reject vì không đủ tồn kho.

#### FR-INV-002 — Stock In
Warehouse Manager phải có thể nhập kho.

**Input:**
- productId, warehouseId, locationId, lotNumber, quantity, reason, referenceNo

**Business rule:**
- Quantity phải > 0.
- Stock in phải tạo stock movement PURCHASE_IN.
- Nếu lot chưa tồn tại thì tạo lot mới.
- Nếu stock balance chưa tồn tại thì tạo mới.
- Nếu đã tồn tại thì cộng quantity.

**Acceptance criteria:**
- Given Warehouse Manager nhập 100 nguyên vật liệu A vào WH1/A01, Then stock balance AVAILABLE tăng 100 And stock movement PURCHASE_IN được tạo.

#### FR-INV-003 — Stock Adjustment
Warehouse Manager có thể điều chỉnh tồn kho.

**Input:**
- stockBalanceId, adjustmentQuantity, reason

**Business rule:**
- Adjustment phải có reason.
- Không được làm quantity âm.
- Adjustment phải tạo stock movement ADJUSTMENT.
- Nếu adjustment vượt ngưỡng cấu hình thì cần Factory Manager approve.

**Acceptance criteria:**
- Given stock balance = 100, When adjustment -10 với reason hợp lệ, Then stock balance = 90 And movement ADJUSTMENT được tạo.

#### FR-INV-004 — Stock Transfer
Warehouse Manager có thể chuyển hàng giữa kho/vị trí.

**Input:**
- fromWarehouseId, fromLocationId, toWarehouseId, toLocationId, productId, lotId, quantity

**Business rule:**
- Chỉ transfer stock AVAILABLE.
- Quantity phải > 0.
- Nguồn phải đủ quantity.
- Transfer phải tạo 2 movement: TRANSFER_OUT, TRANSFER_IN.

**Acceptance criteria:**
- Given WH1/A01 có 50 item A, When transfer 20 item A sang WH1/A02, Then WH1/A01 còn 30 And WH1/A02 tăng 20 And có movement TRANSFER_OUT, TRANSFER_IN.

### 5.4. Stock Movement Ledger

#### FR-MOV-001 — Create Stock Movement
Mọi thay đổi tồn kho phải tạo stock movement.

**Movement type:**
- PURCHASE_IN
- TRANSFER_IN
- TRANSFER_OUT
- RESERVE
- RELEASE_RESERVATION
- ISSUE_TO_PRODUCTION
- CONSUME_IN_PRODUCTION
- PRODUCTION_OUTPUT
- QC_HOLD
- SCRAP
- ADJUSTMENT
- SHIP_OUT
- RETURN_TO_WAREHOUSE

**Stock movement có các thông tin:**
- id, movementType, productId, lotId, warehouseId, locationId, quantity, fromStatus, toStatus, referenceType, referenceId, reason, createdBy, createdAt

**Business rule:**
- Stock movement là immutable.
- Không được update hoặc delete stock movement.
- Nếu sai thì phải tạo movement điều chỉnh.
- Movement quantity phải > 0.

**Acceptance criteria:**
- Given hệ thống reserve material, Then stock movement type RESERVE được tạo.
- Given user cố update stock movement đã tạo, Then hệ thống reject.

### 5.5. BOM Management

#### FR-BOM-001 — Create BOM
Planner có thể tạo BOM cho finished product.

**BOM gồm:**
- id, finishedProductId, version, status, createdAt, createdBy

**BOM item gồm:**
- id, bomId, materialProductId, quantityPerUnit, unit, scrapRate (optional)

**Business rule:**
- BOM chỉ áp dụng cho FINISHED_GOOD hoặc SEMI_FINISHED.
- BOM item phải là RAW_MATERIAL, SEMI_FINISHED hoặc CONSUMABLE.
- quantityPerUnit phải > 0.
- Một product có thể có nhiều BOM version.
- Chỉ một BOM version được ACTIVE tại một thời điểm.

**Acceptance criteria:**
- Given Planner tạo BOM cho Product FG-001 And BOM gồm Material A quantity 2, Material B quantity 1, Then hệ thống tạo BOM thành công.

#### FR-BOM-002 — Activate BOM
Planner có thể activate một BOM version.

**Business rule:**
- Khi activate BOM version mới, các version khác của cùng product phải chuyển INACTIVE.
- Không được activate BOM không có item.

#### FR-BOM-003 — BOM Versioning
Không được sửa BOM đã được dùng trong Work Order.

**Business rule:**
- Nếu BOM đã được dùng trong Work Order, không cho update trực tiếp.
- Nếu cần thay đổi, phải tạo version mới.

**Acceptance criteria:**
- Given BOM version 1 đã được dùng trong Work Order, When Planner update quantity của BOM item, Then hệ thống reject và yêu cầu tạo version mới.

### 5.6. Work Order Management

#### FR-WO-001 — Create Work Order
Planner có thể tạo lệnh sản xuất.

**Work Order gồm:**
- id, code, finishedProductId, bomId, plannedQuantity, plannedStartDate, plannedEndDate, priority, status, createdAt, createdBy

**Work Order status:**
- DRAFT
- PLANNED
- MATERIAL_RESERVED
- MATERIAL_SHORTAGE
- READY_TO_PRODUCE
- IN_PROGRESS
- PAUSED
- COMPLETED
- CANCELLED
- QC_HOLD

**Business rule:**
- plannedQuantity phải > 0.
- finishedProduct phải có BOM ACTIVE.
- Work Order mới tạo có status DRAFT hoặc PLANNED.
- Không được xóa Work Order đã phát sinh movement.

**Acceptance criteria:**
- Given Planner tạo WO sản xuất 100 Product FG-001 And FG-001 có BOM active, Then Work Order được tạo thành công.

#### FR-WO-002 — Calculate Material Requirement
Hệ thống phải tính nguyên vật liệu cần dùng dựa trên BOM.

**Công thức:**
- `requiredQuantity = plannedQuantity * quantityPerUnit`

**Nếu có scrapRate:**
- `requiredQuantity = plannedQuantity * quantityPerUnit * (1 + scrapRate)`

**Acceptance criteria:**
- Given BOM của FG-001 cần 2 Material A cho 1 sản phẩm, When tạo WO sản xuất 100 FG-001, Then hệ thống tính required Material A = 200.

#### FR-WO-003 — Work Order Status Transition
Hệ thống phải kiểm soát chuyển trạng thái hợp lệ.

**Status transition hợp lệ:**
- DRAFT -> PLANNED
- PLANNED -> MATERIAL_RESERVED
- PLANNED -> MATERIAL_SHORTAGE
- MATERIAL_SHORTAGE -> MATERIAL_RESERVED
- MATERIAL_RESERVED -> READY_TO_PRODUCE
- READY_TO_PRODUCE -> IN_PROGRESS
- IN_PROGRESS -> PAUSED
- PAUSED -> IN_PROGRESS
- IN_PROGRESS -> COMPLETED
- PLANNED -> CANCELLED
- MATERIAL_RESERVED -> CANCELLED
- MATERIAL_SHORTAGE -> CANCELLED
- READY_TO_PRODUCE -> CANCELLED

**Business rule:**
- Không cho chuyển trạng thái tùy tiện.
- Mọi status transition quan trọng phải tạo audit log.

**Acceptance criteria:**
- Given WO đang COMPLETED, When user cancel WO, Then hệ thống reject vì transition không hợp lệ.

### 5.7. Material Reservation

#### FR-RES-001 — Reserve Materials For Work Order
Planner có thể reserve nguyên vật liệu cho Work Order.

**Business rule:**
- Hệ thống đọc BOM của Work Order.
- Hệ thống tính required quantity.
- Chỉ reserve nếu AVAILABLE stock đủ.
- Nếu đủ, chuyển stock từ AVAILABLE sang RESERVED.
- Nếu thiếu, Work Order chuyển MATERIAL_SHORTAGE.
- Reserve phải tạo stock movement RESERVE.
- Không được để available stock âm.

**Acceptance criteria:**
- Given Material A available = 500 And WO cần Material A = 200, When Planner reserve material, Then available giảm 200 And reserved tăng 200 And WO status = MATERIAL_RESERVED And movement RESERVE được tạo.

**Acceptance criteria thiếu vật tư:**
- Given Material A available = 100 And WO cần Material A = 200, When Planner reserve material, Then hệ thống không reserve And WO status = MATERIAL_SHORTAGE And trả lỗi thiếu Material A 100 units.

#### FR-RES-002 — Release Reserved Materials
Planner có thể release nguyên vật liệu đã reserve.

**Business rule:**
- Chỉ release khi material đang RESERVED.
- Release chuyển RESERVED về AVAILABLE.
- Release phải tạo movement RELEASE_RESERVATION.
- Không được release nếu Work Order đã IN_PROGRESS hoặc COMPLETED.

**Acceptance criteria:**
- Given WO đã reserve 200 Material A, When Planner cancel WO, Then reserved giảm 200 And available tăng 200 And movement RELEASE_RESERVATION được tạo.

#### FR-RES-003 — Concurrent Reservation
Hệ thống phải xử lý đúng khi nhiều Work Order cùng reserve một material.

**Business requirement bắt buộc:**
- Không được để stock âm.
- Không được để reserved vượt on_hand.
- Không được tạo duplicate movement do race condition.

**Test case bắt buộc:**
- Given Material A available = 10, When 20 request đồng thời reserve 1 Material A, Then chỉ 10 request success And 10 request failed And available = 0 And reserved = 10.

**Technical expectation:**
- Có thể dùng pessimistic locking, optimistic locking, hoặc database constraint.
- Nhóm phải giải thích được lựa chọn của mình.

### 5.8. Production Execution

#### FR-PROD-001 — Start Production
Production Operator có thể start Work Order.

**Input:**
- workOrderId, machineId, productionLineId, operatorId

**Business rule:**
- Chỉ start Work Order ở status READY_TO_PRODUCE hoặc MATERIAL_RESERVED.
- Machine phải AVAILABLE.
- Machine không được DOWN hoặc UNDER_MAINTENANCE.
- Một machine không được chạy 2 Work Order cùng lúc.
- Khi start, Work Order chuyển IN_PROGRESS.
- Machine chuyển RUNNING.
- Reserved material chuyển sang ISSUE_TO_PRODUCTION hoặc CONSUME_IN_PRODUCTION tùy thiết kế.
- Tạo production event START.

**Acceptance criteria:**
- Given WO đã reserve đủ material And Machine M1 AVAILABLE, When Operator start production, Then WO status = IN_PROGRESS And Machine M1 status = RUNNING And production event START được tạo.

**Acceptance criteria machine lỗi:**
- Given Machine M1 UNDER_MAINTENANCE, When Operator start production bằng M1, Then hệ thống reject.

#### FR-PROD-002 — Pause/Resume Production
Operator có thể pause/resume Work Order.

**Business rule:**
- Chỉ pause Work Order đang IN_PROGRESS.
- Chỉ resume Work Order đang PAUSED.
- Mỗi action phải tạo production event.

#### FR-PROD-003 — Complete Production
Operator có thể hoàn thành Work Order.

**Input:**
- workOrderId, actualQuantity, goodQuantity, defectQuantity, scrapQuantity, note

**Business rule:**
- Chỉ complete Work Order đang IN_PROGRESS.
- actualQuantity phải >= 0.
- goodQuantity + defectQuantity + scrapQuantity phải bằng actualQuantity.
- Khi complete:
  - Consume reserved material.
  - Tạo movement CONSUME_IN_PRODUCTION.
  - Tạo finished goods lot.
  - Tạo movement PRODUCTION_OUTPUT.
  - Tạo QC inspection record.
  - Work Order chuyển COMPLETED.
  - Machine chuyển AVAILABLE.

**Acceptance criteria:**
- Given WO sản xuất 100 sản phẩm, When Operator complete với actual=100, good=95, defect=5, scrap=0, Then WO status = COMPLETED And nguyên vật liệu được consume And thành phẩm được tạo ở trạng thái QUALITY_INSPECTION And QC inspection được tạo And movement PRODUCTION_OUTPUT được tạo.

### 5.9. Quality Control

#### FR-QC-001 — Create QC Inspection Automatically
Sau khi Work Order completed, hệ thống phải tự tạo QC inspection.

**QC inspection gồm:**
- id, workOrderId, productId, lotId, quantity, status, createdAt

**QC status (6 giá trị):**
- `PENDING_INSPECTION` — Chờ kiểm tra (initial)
- `PASSED` — Đạt QC, hàng chuyển AVAILABLE
- `FAILED` — Không đạt QC
- `ON_HOLD` — Hàng đang giữ để điều tra
- `REWORK_REQUIRED` — Hàng cần làm lại
- `SCRAPPED` — Hàng đã loại bỏ

**Stock status liên quan:**
- `QUALITY_INSPECTION` — Hàng chờ QC
- `AVAILABLE` — Hàng đã pass QC, sẵn sàng
- `ON_HOLD` — Hàng đang giữ
- `SCRAPPED` — Hàng đã loại bỏ

**Business rule:**
- Finished goods sau production output mặc định ở trạng thái QUALITY_INSPECTION.
- Hàng ở QUALITY_INSPECTION không được chuyển AVAILABLE trừ khi QC pass.
- Hàng ở QUALITY_INSPECTION không được ship out.
- Mỗi inspection có `quantity` (tổng số). Số lượng còn lại chưa xử lý = quantity − sum(pass qty) − sum(SCRAP qty) − sum(HOLD qty). (fail action=REWORK không count vì stock không đổi)
- inspection giữ PENDING_INSPECTION cho tới khi quantity đã xử lý (pass + SCRAP + HOLD) = quantity. Khi đó: nếu tất cả pass → PASSED; nếu có fail → FAILED.

#### FR-QC-002 — Pass QC
QC Inspector có thể pass QC.

**Input:**
- inspectionId, passedQuantity, note

**Business rule:**
- passedQuantity phải > 0.
- passedQuantity không được vượt số lượng còn lại: quantity − sum(pass) − sum(SCRAP) − sum(HOLD).
- Pass QC chuyển stock từ QUALITY_INSPECTION sang AVAILABLE với số lượng = passedQuantity.
- Tạo movement QC_PASS.
- Mỗi lần pass ghi 1 dòng `quality_inspection_results` với `isPass = true`.

**API:** `POST /api/quality-inspections/{inspectionId}/pass`

**Acceptance criteria:**
- Given 100 finished goods đang QUALITY_INSPECTION, When QC pass 95, Then 95 chuyển AVAILABLE And movement QC_PASS được tạo.

#### FR-QC-003 — Fail QC
QC Inspector có thể fail QC.

**Input:**
- inspectionId, failedQuantity, action, defectTypeId, reason, note

**Action (3 giá trị):**
- `SCRAP` — Hàng lỗi nặng, loại bỏ vĩnh viễn
- `HOLD` — Cần điều tra thêm, giữ hàng lại
- `REWORK` — Có thể sửa, quay về chờ QC kiểm tra lại

**Business rule:**
- Fail QC bắt buộc có `defectTypeId` và `reason`.
- `failedQuantity` > 0 và ≤ quantity − sum(pass) − sum(SCRAP) − sum(HOLD) (REWORK không count vào remaining).
- Mỗi lần fail ghi 1 dòng `quality_inspection_results` với `isPass = false`.

**Stock transition theo action:**

| Action | Stock transition | Movement | QC status |
|--------|-----------------|----------|-----------|
| SCRAP  | QUALITY_INSPECTION → SCRAPPED | SCRAP | FAILED |
| HOLD   | QUALITY_INSPECTION → ON_HOLD  | QC_HOLD | ON_HOLD |
| REWORK | QUALITY_INSPECTION → QUALITY_INSPECTION *(giữ nguyên)* | *(không tạo)* | REWORK_REQUIRED |

**API:** `POST /api/quality-inspections/{inspectionId}/fail`

**Acceptance criteria (SCRAP):**
- Given QC fail 5 sản phẩm với action SCRAP, Then 5 sản phẩm chuyển SCRAPPED And movement SCRAP được tạo And defect reason được lưu.

**Acceptance criteria (REWORK):**
- Given QC fail 5 sản phẩm với action REWORK, Then stock giữ QUALITY_INSPECTION And QC chờ kiểm tra lại after rework.

#### FR-QC-004 — QC Status & Action Management (Master Data)

Hệ thống cho phép quản lý danh mục QC statuses, QC actions, và defect types.

**QC Statuses:** `PENDING, PASSED, FAILED, ON_HOLD`
**QC Actions:** `HOLD, REWORK, SCRAP`
**Defect Types:** 10 loại (SCRATCH, DIMENSION_ERROR, WEIGHT_ERROR, COLOR_DEFECT, CRACK, CONTAMINATION, FUNCTIONAL_FAIL, ASSEMBLY_ERROR, LABEL_ERROR, OTHER)

**API:**
- `GET/POST /api/quality-inspections/statuses` — List/Create
- `PUT/DELETE /api/quality-inspections/statuses/{id}` — Update/Delete
- `GET/POST /api/quality-inspections/actions` — List/Create
- `PUT/DELETE /api/quality-inspections/actions/{id}` — Update/Delete
- `GET/POST /api/quality-inspections/defect-types` — List/Create
- `PUT/DELETE /api/quality-inspections/defect-types/{id}` — Update/Delete

**Roles:** `ADMIN` được CRUD; các role khác read-only. Chi tiết: [`docs/api-spec/QC/api.md`](docs/api-spec/QC/api.md)

### 5.10. Maintenance Management

#### FR-MNT-001 — Create Maintenance Ticket
Maintenance Engineer có thể tạo ticket bảo trì.

**Maintenance ticket gồm:**
- id, machineId, type, priority, description, status, createdAt, createdBy

**Type:**
- PREVENTIVE
- CORRECTIVE
- EMERGENCY
- INSPECTION

**Status:**
- OPEN
- IN_PROGRESS
- RESOLVED
- CLOSED
- CANCELLED

**Business rule:**
- Machine DOWN phải có maintenance ticket OPEN hoặc IN_PROGRESS.
- Machine UNDER_MAINTENANCE không được dùng sản xuất.
- Ticket OPEN mới được start.

#### FR-MNT-002 — Start Maintenance
Maintenance Engineer có thể start ticket.

**Business rule:**
- Khi start ticket:
  - Ticket chuyển IN_PROGRESS.
  - Machine chuyển UNDER_MAINTENANCE.
  - Downtime bắt đầu được ghi nhận.

#### FR-MNT-003 — Close Maintenance Ticket
Maintenance Engineer có thể close ticket.

**Input:**
- ticketId, actionTaken, rootCause, note

**Business rule:**
- Close ticket phải có actionTaken.
- Khi close:
  - Ticket chuyển CLOSED.
  - Machine chuyển AVAILABLE hoặc DOWN tùy kết quả.
  - Downtime end time được cập nhật.

**Acceptance criteria:**
- Given Machine M1 UNDER_MAINTENANCE, When Maintenance Engineer close ticket thành công, Then ticket status = CLOSED And Machine M1 status = AVAILABLE And downtime được ghi nhận.

### 5.11. Reporting

#### FR-RPT-001 — Inventory Summary Report
Factory Manager có thể xem tổng quan tồn kho.

**Report cần hiển thị:**
- productCode, productName, warehouse, availableQuantity, reservedQuantity, qualityInspectionQuantity, onHoldQuantity, scrappedQuantity, totalOnHand

**Filter:**
- warehouseId, productType, productCode, status

#### FR-RPT-002 — Material Shortage Report
Report danh sách vật tư thiếu cho Work Order.

**Output:**
- workOrderCode, materialCode, requiredQuantity, availableQuantity, shortageQuantity

#### FR-RPT-003 — Production Output Report
Report sản lượng sản xuất.

**Output:**
- date, workOrderCode, productCode, plannedQuantity, actualQuantity, goodQuantity, defectQuantity, scrapQuantity, completionRate

#### FR-RPT-004 — Defect Rate Report
Report tỷ lệ lỗi.

**Output:**
- productCode, totalProduced, defectQuantity, scrapQuantity, defectRate, topDefectTypes

#### FR-RPT-005 — Machine Downtime Report
Report downtime máy.

**Output:**
- machineCode, totalDowntimeMinutes, maintenanceTicketCount, lastDowntimeReason

#### FR-RPT-006 — Stock Movement History Report
Report lịch sử biến động kho.

**Filter:**
- productId, warehouseId, movementType, fromDate, toDate, referenceType, referenceId

**Output:**
- movementTime, movementType, productCode, lotNumber, warehouse, location, quantity, referenceType, referenceId, createdBy, reason

### 5.12. Audit Log

#### FR-AUD-001 — Audit Important Actions
Hệ thống phải ghi audit log cho các action quan trọng:

- CREATE_WORK_ORDER
- RESERVE_MATERIAL
- RELEASE_RESERVATION
- START_PRODUCTION
- PAUSE_PRODUCTION
- RESUME_PRODUCTION
- COMPLETE_PRODUCTION
- QC_PASS
- QC_FAIL
- QC_HOLD
- SCRAP_STOCK
- CREATE_MAINTENANCE_TICKET
- START_MAINTENANCE
- CLOSE_MAINTENANCE_TICKET
- ADJUST_STOCK
- ACTIVATE_BOM

**Audit log gồm:**
- id, actorId, action, entityType, entityId, oldValue, newValue, createdAt, ipAddress (optional)

**Business rule:**
- Audit log không được sửa hoặc xóa.
- Action quan trọng không có audit log xem như chưa đạt requirement.

---

## 6. Non-functional Requirements

### 6.1. API convention
Tất cả API phải có response format thống nhất.

**Ví dụ success:**
```json
{
  "success": true,
  "data": {},
  "message": "Success",
  "timestamp": "2026-07-24T09:00:00"
}
```

**Ví dụ error:**
```json
{
  "success": false,
  "errorCode": "INSUFFICIENT_STOCK",
  "message": "Available stock is not enough",
  "details": {},
  "timestamp": "2026-07-24T09:00:00"
}
```

### 6.2. Validation
Hệ thống phải validate:
- Required fields
- Quantity > 0
- Date hợp lệ
- Status transition hợp lệ
- Duplicate code
- Permission
- Stock không âm
- Machine availability
- BOM active

### 6.3. Transaction
Các action sau bắt buộc phải nằm trong transaction:
- Stock in
- Stock adjustment
- Stock transfer
- Reserve material
- Release material
- Start production
- Complete production
- QC pass/fail
- Maintenance start/close
- Cancel Work Order

### 6.4. Concurrency
Nhóm phải xử lý concurrency cho các case:
- Nhiều Work Order reserve cùng một material.
- Nhiều user adjustment cùng một stock balance.
- Nhiều operator start cùng một machine.
- QC release và stock operation xảy ra gần đồng thời.

**Yêu cầu tối thiểu:**
- Có giải pháp locking hoặc constraint rõ ràng.
- Có test case chứng minh không bị stock âm.
- Có giải thích trade-off trong README.

### 6.5. Database
Yêu cầu database:
- Dùng PostgreSQL.
- Có migration bằng Flyway hoặc Liquibase.
- Có foreign key.
- Có unique constraint.
- Có index cho query/filter chính.
- Có audit columns.
- Không dùng ddl-auto=create cho demo chính thức.

**Audit columns khuyến nghị:**
- created_at, created_by, updated_at, updated_by, deleted_at (optional), version

### 6.6. Security
Yêu cầu security:
- Password hash bằng BCrypt.
- JWT authentication.
- API phân quyền theo role.
- Không expose password hash.
- Không cho user thường gọi API admin.

### 6.7. Testing
Yêu cầu testing tối thiểu:
- Unit test cho service quan trọng.
- Integration test cho reserve material.
- Integration/concurrency test cho stock không âm.
- Test business rule status transition.
- Test authorization cơ bản.

**Test bắt buộc:**
- Concurrent reservation test
- Insufficient stock test
- Complete production test
- QC fail requires reason test
- Machine under maintenance cannot start production test

---

## 7. UI Requirements

Angular UI chỉ cần đơn giản, không yêu cầu đẹp.

### 7.1. Page bắt buộc
1. Login
2. Dashboard
3. Product/Material Management
4. Warehouse & Stock Balance
5. Stock Movement History
6. BOM Management
7. Work Order List
8. Work Order Detail
9. Material Reservation Screen
10. Production Execution Screen
11. QC Inspection Screen
12. Maintenance Ticket Screen
13. Reports Screen

### 7.2. UI behavior tối thiểu
- Có route guard theo login.
- Có menu theo role.
- Có form validation cơ bản.
- Có table list/search đơn giản.
- Có pagination cho danh sách lớn.
- Hiển thị error message từ backend.
- Không cần chart phức tạp.
- Không cần realtime.
- Không cần drag/drop.

---

## 8. API Requirement Summary

### Auth
- `POST /api/auth/login`
- `GET /api/users/me`

### Master Data
- `GET /api/products`
- `POST /api/products`
- `GET /api/products/{id}`
- `PUT /api/products/{id}`
- `GET /api/warehouses`
- `POST /api/warehouses`
- `GET /api/warehouse-locations`
- `POST /api/warehouse-locations`
- `GET /api/machines`
- `POST /api/machines`
- `PUT /api/machines/{id}`
- `GET /api/production-lines`
- `POST /api/production-lines`

### Inventory
- `GET /api/stock-balances`
- `POST /api/stock-in`
- `POST /api/stock-adjustments`
- `POST /api/stock-transfers`
- `GET /api/stock-movements`

### BOM
- `POST /api/boms`
- `GET /api/boms`
- `GET /api/boms/{id}`
- `POST /api/boms/{id}/activate`
- `POST /api/boms/{id}/new-version`

### Work Order
- `POST /api/work-orders`
- `GET /api/work-orders`
- `GET /api/work-orders/{id}`
- `POST /api/work-orders/{id}/reserve-materials`
- `POST /api/work-orders/{id}/release-materials`
- `POST /api/work-orders/{id}/start`
- `POST /api/work-orders/{id}/pause`
- `POST /api/work-orders/{id}/resume`
- `POST /api/work-orders/{id}/complete`
- `POST /api/work-orders/{id}/cancel`

### QC
- `GET /api/quality-inspections`
- `GET /api/quality-inspections/{id}`
- `POST /api/quality-inspections`
- `POST /api/quality-inspections/{inspectionId}/pass`
- `POST /api/quality-inspections/{inspectionId}/fail`
- `GET /api/quality-inspections/statuses`
- `POST /api/quality-inspections/statuses`
- `GET /api/quality-inspections/actions`
- `POST /api/quality-inspections/actions`
- `GET /api/quality-inspections/defect-types`
- `POST /api/quality-inspections/defect-types`

### Maintenance
- `POST /api/maintenance-tickets`
- `GET /api/maintenance-tickets`
- `GET /api/maintenance-tickets/{id}`
- `POST /api/maintenance-tickets/{id}/start`
- `POST /api/maintenance-tickets/{id}/close`
- `POST /api/maintenance-tickets/{id}/cancel`

### Reports
- `GET /api/reports/inventory-summary`
- `GET /api/reports/material-shortage`
- `GET /api/reports/production-output`
- `GET /api/reports/defect-rate`
- `GET /api/reports/machine-downtime`
- `GET /api/reports/stock-movement-history`

### Audit
- `GET /api/audit-logs`
- `GET /api/audit-logs/{id}`

---

## 9. Database Entity Draft

Các bảng core nên có:

- users
- roles
- user_roles
- products
- units_of_measure
- warehouses
- warehouse_locations
- stock_lots
- stock_balances
- stock_movements
- boms
- bom_items
- work_orders
- work_order_materials
- work_order_events
- production_lines
- machines
- production_runs
- quality_inspections
- quality_inspection_results
- defect_types
- maintenance_tickets
- machine_downtimes
- audit_logs
- idempotency_keys

---

## 10. End-to-end Demo Flow bắt buộc

Nhóm phải demo được flow sau:

1. Admin tạo user và role.
2. Admin tạo warehouse, location, machine, production line.
3. Admin tạo raw material và finished product.
4. Warehouse Manager nhập kho raw material.
5. Planner tạo BOM cho finished product.
6. Planner tạo Work Order sản xuất 100 sản phẩm.
7. Hệ thống tính material requirement.
8. Planner reserve material.
9. Operator start production trên machine.
10. Operator complete production.
11. Hệ thống consume material và tạo finished goods.
12. QC Inspector pass/fail QC.
13. Factory Manager xem inventory report, production report, defect report.
14. Auditor xem stock movement và audit log.

---

## 11. Test case bắt buộc

**TC-001 — Reserve thành công**
- Given Material A available = 500 And Work Order cần 200 Material A
- When Planner reserve material
- Then available = 300 And reserved = 200 And Work Order status = MATERIAL_RESERVED

**TC-002 — Reserve thiếu vật tư**
- Given Material A available = 100 And Work Order cần 200 Material A
- When Planner reserve material
- Then reserve failed And Work Order status = MATERIAL_SHORTAGE And stock không thay đổi

**TC-003 — Concurrent reservation**
- Given Material A available = 10
- When 20 request đồng thời reserve 1 Material A
- Then 10 request success And 10 request failed And available = 0 And reserved = 10 And stock không âm

**TC-004 — Cancel Work Order release material**
- Given Work Order đã reserve 200 Material A
- When Planner cancel Work Order
- Then reserved giảm 200 And available tăng 200 And movement RELEASE_RESERVATION được tạo

**TC-005 — Machine under maintenance**
- Given Machine M1 status = UNDER_MAINTENANCE
- When Operator start Work Order bằng M1
- Then hệ thống reject

**TC-006 — Complete production**
- Given Work Order đang IN_PROGRESS
- When Operator complete với actual=100, good=95, defect=5, scrap=0
- Then Work Order status = COMPLETED And raw material bị consume And finished goods được tạo ở QUALITY_INSPECTION And QC inspection được tạo

**TC-007 — QC failed requires reason**
- Given QC inspection đang PENDING_INSPECTION
- When QC Inspector fail quantity 5 nhưng không nhập reason
- Then hệ thống reject

**TC-008 — QC pass chuyển stock available**
- Given 95 finished goods đang QUALITY_INSPECTION
- When QC Inspector pass 95
- Then 95 finished goods chuyển AVAILABLE And movement QC_PASS được tạo

---

## 12. Scope phân cấp

### 12.1. Must Have
Bắt buộc hoàn thành:
- Auth/JWT
- Role-based API
- Product/material
- Warehouse/location
- Stock balance
- Stock movement
- Stock in
- Stock adjustment
- BOM
- Work Order
- Material reservation
- Start/complete production
- QC pass/fail
- Maintenance ticket basic
- Inventory report
- Production report
- Audit log basic
- Concurrent reservation test

### 12.2. Should Have
Nên có để đạt mức khá:
- BOM versioning
- Machine double-booking prevention
- Stock transfer
- QC pass/fail/hold/rework/scrap
- Material shortage report
- Machine downtime report
- Swagger đầy đủ
- Docker Compose

### 12.3. Could Have
Nếu còn thời gian:
- Idempotency key
- Lot traceability
- Approval cho adjustment lớn
- Notification outbox
- Export report
- Dashboard chart
- Soft delete
- Advanced search/filter

---

## 13. Rubric chấm điểm đề xuất

**Tổng: 100 điểm**

| Nhóm tiêu chí | Điểm |
|---|---|
| Requirement completion | 15 |
| Backend architecture | 15 |
| Database design | 20 |
| Transaction & concurrency | 15 |
| Business rule correctness | 15 |
| Security & authorization | 5 |
| Testing | 8 |
| Documentation/demo | 4 |
| Git/team discipline | 3 |

### 13.1. Requirement completion — 15 điểm
**Đánh giá:**
- Hoàn thành các module must-have.
- Demo được end-to-end flow chính.
- API hoạt động thật.
- UI gọi API thật.
- Không hardcode dữ liệu demo quan trọng.

### 13.2. Backend architecture — 15 điểm
**Đánh giá:**
- Layer rõ ràng: controller/service/repository/domain/dto.
- Không để business logic trong controller.
- DTO mapping rõ ràng.
- Exception handling thống nhất.
- Transaction boundary hợp lý.
- Code dễ đọc, dễ maintain.

### 13.3. Database design — 20 điểm
**Đánh giá:**
- Entity relationship hợp lý.
- Có FK đầy đủ.
- Có unique constraint.
- Có index cho query chính.
- Có migration rõ ràng.
- Stock movement thiết kế đúng kiểu ledger.
- Không update tồn kho tùy tiện.
- Không dùng ddl-auto=create cho demo chính thức.

### 13.4. Transaction & concurrency — 15 điểm
**Đánh giá:**
- Reserve material không bị race condition.
- Không để stock âm.
- Có locking hoặc constraint phù hợp.
- Có test concurrent reservation.
- Complete production atomic.
- QC pass/fail atomic.
- Giải thích được trade-off optimistic/pessimistic locking.

### 13.5. Business rule correctness — 15 điểm
**Đánh giá:**
- Status transition đúng.
- Không start production khi thiếu material.
- Không start bằng machine maintenance.
- Không ship hàng chưa QC passed.
- Cancel Work Order release material đúng.
- QC failed bắt buộc có reason.
- Stock movement được tạo đầy đủ.

### 13.6. Security & authorization — 5 điểm
**Đánh giá:**
- JWT hoạt động.
- Password được hash.
- API phân quyền đúng role.
- Không expose thông tin nhạy cảm.

### 13.7. Testing — 8 điểm
**Đánh giá:**
- Có unit test service chính.
- Có integration test flow chính.
- Có concurrency test.
- Có test business rule quan trọng.

### 13.8. Documentation/demo — 4 điểm
**Đánh giá:**
- README rõ cách chạy.
- Có API docs Swagger.
- Có demo data.
- Có demo script.
- Có mô tả kiến trúc và decision quan trọng.

### 13.9. Git/team discipline — 3 điểm
**Đánh giá:**
- Commit message rõ ràng.
- Có branch/PR/MR convention.
- Không commit file không cần thiết.
- Resolve conflict sạch.
- Có review trước merge.

---

## 14. Definition of Done

Một requirement chỉ được xem là done khi đạt đủ:
- API implemented
- Validation implemented
- Authorization checked
- Business rule implemented
- Transaction handled if needed
- Database migration completed
- Swagger documented
- Unit/integration test added where applicable
- UI integrated if requirement cần demo
- Error handling rõ ràng
- Demo được bằng dữ liệu thật
