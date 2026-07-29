# Data Model: Query Work Orders (`GET /api/v1/work-orders`)

## Domain Entities & DTOs

### 1. `WorkOrder` (Domain Entity)

Located at: `fpt.qn.mes.workorder.domain.entities.WorkOrder`

Plain Java POJO with `@Getter`, `@Builder`, `@FieldDefaults(level = AccessLevel.PRIVATE)` (No ORM annotations).

| Field Name | Type | Description |
|---|---|---|
| `id` | `UUID` | Unique identifier (Primary Key) |
| `code` | `String` | Unique human-readable work order code (e.g. `WO-2026-001`) |
| `finishedProductId` | `UUID` | Reference to Product entity |
| `bomId` | `UUID` | Reference to BOM entity |
| `plannedQuantity` | `BigDecimal` | Target quantity to produce |
| `plannedStartDate` | `Instant` | Scheduled start timestamp |
| `plannedEndDate` | `Instant` | Scheduled completion timestamp |
| `priorityId` | `UUID` | Reference to WorkOrderPriority entity |
| `workOrderStatusId` | `UUID` | Reference to WorkOrderStatus entity |
| `createdBy` | `UUID` | Reference to User entity |
| `createdAt` | `Instant` | Creation timestamp |

---

### 2. `WorkOrdersRecord` (jOOQ Persistence Table Record)

Located at: `fpt.qn.mes.jooq.tables.records.WorkOrdersRecord`

Generated automatically from PostgreSQL table `work_orders`.

Mapping between `WorkOrdersRecord` and `WorkOrder` domain entity handled by `WorkOrderRecordMapper`:
- `OffsetDateTime` (DB `TIMESTAMPTZ`) ↔ `Instant` (Java Domain)

---

### 3. `WorkOrderDto` (Application Response DTO)

Located at: `fpt.qn.mes.workorder.application.dto.response.WorkOrderDto`

Plain Java Class with `@Getter`, `@Builder`.

| Field Name | Type | Description |
|---|---|---|
| `id` | `UUID` | Work Order UUID |
| `code` | `String` | Work Order code |
| `finishedProductId` | `UUID` | Product UUID |
| `bomId` | `UUID` | BOM UUID |
| `plannedQuantity` | `BigDecimal` | Quantity |
| `plannedStartDate` | `Instant` | Start date |
| `plannedEndDate` | `Instant` | End date |
| `priorityId` | `UUID` | Priority UUID |
| `workOrderStatusId` | `UUID` | Status UUID |
| `createdBy` | `UUID` | Created by user UUID |
| `createdAt` | `Instant` | Created timestamp |

---

### 4. `PageResponse<T>` (Common Pagination Response Wrapper)

Located at: `fpt.qn.mes.common.dto.response.PageResponse`

| Field Name | Type | Description |
|---|---|---|
| `content` | `List<T>` | List of items for current page |
| `page` | `int` | Current 0-based page number |
| `size` | `int` | Requested page size |
| `totalElements` | `long` | Total matching records across all pages |
| `totalPages` | `int` | Total number of pages (`ceil(totalElements / size)`) |
