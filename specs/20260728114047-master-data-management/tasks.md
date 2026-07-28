---
description: "Implementation tasks for Master Data Management feature — organized by WBS"
---

# Tasks: Master Data Management

**Input**: Design documents from `specs/20260728114047-master-data-management/`
**Tests**: Tests are REQUIRED — unit tests (Mockito) + integration tests (AbstractIntegrationTest)
**Codebase state**: All 5 sub-modules scaffolded with 4-layer Clean Architecture stubs. Every method throws `UnsupportedOperationException("Not implemented")`. Schema exists in `V20260726194837__init.sql`. Seed JSON files exist under `be/src/main/resources/seed/`.

---

## WBS 4.1 — Product CRUD

- [ ] T001 Create `MasterDataSeeder` in `master/infrastructure/seed/` — read JSON seed files, insert into lookup tables via jOOQ DSLContext. `ON CONFLICT DO NOTHING`. Not Flyway.
- [ ] T002 Create `LookupService` in `common/service/` — read any lookup table via DSLContext.
- [ ] T003 Create `LookupRepository` in `common/service/` — wrap lookup tables into `findAll(tableName)` pattern.
- [ ] T004 Implement `Product.create()` factory — set id, version=0, createdAt, updatedAt. Add `productTypeName`, `unitName`, `productStatusName` transient fields.
- [ ] T005 Implement `ProductRecordMapper.toDomain()` and `toRecord()` — null-safe mapping.
- [ ] T006 Add `productTypeName`, `unitName`, `productStatusName`, `updatedBy` to `ProductDto`.
- [ ] T007 Implement `ProductRepository` (jOOQ): `findById`, `save` (insert), `update`, `findAll` (paginated), `existsByCode`.
- [ ] T008 Implement `ProductPersistenceAdapter` — delegate to jOOQ, return `PaginationResult`.
- [ ] T009 Implement `CreateProductRequest` (code, name, productTypeId, unit, productStatusId — all `@NotBlank`/`@NotNull`).
- [ ] T010 Implement `UpdateProductRequest` (name, unit, productStatusId).
- [ ] T011 Implement `ProductService` — create (check duplicate code → 409), getProductById (404 if not found), getProducts (paginated + filter by type/status), updateProduct (check code uniqueness if changed + optimistic locking), deactivateProduct (check stock movements → soft delete).
- [ ] T012 Implement `ProductController`: `GET /api/products` (paginated, filter by type/status), `GET /api/products/{id}`, `POST /api/products`, `PUT /api/products/{id}`, `PUT /api/products/{id}/deactivate` (soft delete).
- [ ] T013 Write `ProductServiceTest` (Mockito) — create (success + duplicate 409), get (found + 404), update (success + code conflict), deactivate (success + stock guard 409).
- [ ] T014 Write `ProductIntegrationTest` (AbstractIntegrationTest) — full HTTP flows for all endpoints.
- [ ] T015 Add `PRODUCT_NOT_FOUND`, `PRODUCT_CODE_DUPLICATE`, `PRODUCT_HAS_STOCK_MOVEMENTS` to exception handler.

## WBS 4.2 — Product Deactivation Validation

- [ ] T016 In `ProductService.deactivateProduct()`: check `stock_movements` table for any movement referencing this product. If exists → throw 409 `PRODUCT_HAS_STOCK_MOVEMENTS`. Otherwise set status to INACTIVE.
- [ ] T017 In `ProductService`: default list query filters `productStatusId = ACTIVE` unless explicit filter passed.

## WBS 4.3 — Status Transition Active/Inactive (All Entities)

- [ ] T018 Apply deactivation guard pattern to Warehouse: check `stock_balances` before deactivate.
- [ ] T019 Apply deactivation guard pattern to Location: check `stock_balances` before deactivate.
- [ ] T020 Apply deactivation guard pattern to Production Line: check `machines` still active before deactivate.
- [ ] T021 Apply deactivation guard pattern to Machine: check `work_order_events` or status before deactivate.
- [ ] T022 All list endpoints default filter by `status = ACTIVE` unless `?status=INACTIVE` explicitly passed.

## WBS 4.4 — Warehouse CRUD

- [ ] T023 Implement `Warehouse.create()` factory method.
- [ ] T024 Implement `WarehouseRecordMapper.toDomain()` and `toRecord()`.
- [ ] T025 Add `warehouseStatusName` to `WarehouseDto`.
- [ ] T026 Implement `WarehouseRepository` (jOOQ) + `WarehousePersistenceAdapter`.
- [ ] T027 Implement `WarehouseService` — CRUD + code unique + stock guard on deactivate.
- [ ] T028 Implement `WarehouseController`: `GET /api/warehouses`, `GET /api/warehouses/{id}`, `POST /api/warehouses`, `PUT /api/warehouses/{id}`, `PUT /api/warehouses/{id}/deactivate`.
- [ ] T029 Write `WarehouseServiceTest` + `WarehouseIntegrationTest`.

## WBS 4.5 — Warehouse Location CRUD

- [ ] T030 Implement `WarehouseLocation.create()` factory method.
- [ ] T031 Implement `LocationRecordMapper.toDomain()` and `toRecord()`.
- [ ] T032 Add `locationStatusName` to `WarehouseLocationDto`.
- [ ] T033 Implement `WarehouseLocationRepository` (jOOQ) — unique code within same warehouse.
- [ ] T034 Implement `LocationService` — CRUD + validate code unique trong cùng warehouse + location INACTIVE không cho nhập hàng.
- [ ] T035 Implement `LocationController`: `GET /api/warehouses/{whId}/locations`, `POST /api/warehouses/{whId}/locations`, `PUT /api/warehouses/{whId}/locations/{id}`, `PUT /api/warehouses/{whId}/locations/{id}/deactivate`.
- [ ] T036 Write `LocationServiceTest` + `LocationIntegrationTest`.

## WBS 4.6 — Validation Stock (Warehouse/Location)

- [ ] T037 In `WarehouseService.deactivateWarehouse()`: check `stock_balances` for any active stock in this warehouse. If exists → 409.
- [ ] T038 In `LocationService.deactivateLocation()`: check `stock_balances` for any active stock at this location. If exists → 409.
- [ ] T039 Integration test: seed stock → try deactivate warehouse → expect 409.

## WBS 4.7 — Production Line CRUD

- [ ] T040 Implement `ProductionLine.create()` factory method.
- [ ] T041 Implement `LineRecordMapper.toDomain()` and `toRecord()`.
- [ ] T042 Add `lineStatusName` to `ProductionLineDto`.
- [ ] T043 Implement `ProductionLineRepository` (jOOQ) + `LinePersistenceAdapter`.
- [ ] T044 Implement `LineService` — CRUD + INACTIVE không assign WO.
- [ ] T045 Implement `LineController`: `GET /api/production-lines`, `POST /api/production-lines`, `PUT /api/production-lines/{id}`, `PUT /api/production-lines/{id}/deactivate`.
- [ ] T046 Write `LineServiceTest` + `LineIntegrationTest`.

## WBS 4.8 — Machine CRUD

- [ ] T047 Implement `Machine.create()` factory method.
- [ ] T048 Implement `MachineRecordMapper.toDomain()` and `toRecord()`.
- [ ] T049 Add `machineStatusName` to `MachineDto`.
- [ ] T050 Implement `MachineRepository` (jOOQ) + `MachinePersistenceAdapter`.
- [ ] T051 Implement `MachineService` — CRUD + validate machine code unique + machine gắn với production line tồn tại.
- [ ] T052 Implement `MachineController`: `GET /api/machines`, `POST /api/machines`, `PUT /api/machines/{id}`, `PUT /api/machines/{id}/deactivate`.
- [ ] T053 Write `MachineServiceTest` + `MachineIntegrationTest`.

## WBS 4.9 — Machine Status Logic

- [ ] T054 Implement `MachineService.changeMachineStatus(machineId, newStatus)` — validate transition hợp lệ. Statuses: `AVAILABLE`, `RUNNING`, `DOWN`, `UNDER_MAINTENANCE`, `RETIRED`.
- [ ] T055 Rules: chỉ `AVAILABLE` mới start production. `DOWN`/`UNDER_MAINTENANCE`/`RETIRED` không chạy WO.
- [ ] T056 Implement `PATCH /api/machines/{id}/status` endpoint — accept new status, return updated machine.
- [ ] T057 Write `MachineStatusServiceTest` — test all valid/invalid transitions, test start WO on DOWN/MAINTENANCE/RETIRED → reject.
