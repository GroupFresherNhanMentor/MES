# RBAC Permission Matrix

## 1. Enforcement rules

- Every protected handler uses one exact
  `@PreAuthorize("hasAuthority('<RESOURCE_ACTION>')")` expression.
- Permission authorities use their raw canonical names.
- `ROLE_*` authorities are exposed for compatibility. Endpoint annotations use
  canonical `RESOURCE_ACTION` authorities derived from current role names by
  `RolePermissionPolicy`.
- A startup/integration contract test enumerates `RequestMappingHandlerMapping`
  and fails when an `/api` handler is public unexpectedly, lacks an exact
  permission, uses a role guard, or uses only `isAuthenticated()`.
- The only public handlers are the two authentication endpoints.

## 2. Endpoint-to-permission catalog

This catalog covers all 89 protected handlers plus the two public auth
handlers.

### Authentication and RBAC

| Endpoints | Permission |
|---|---|
| `POST /api/auth/login`, `POST /api/auth/refresh` | Public |
| `GET /api/users`, `GET /api/users/{id}` | `USER_READ` |
| `POST /api/users` | `USER_CREATE` |
| `PUT /api/users/{id}` | `USER_UPDATE` |
| `PATCH /api/users/{id}/activate` | `USER_ACTIVATE` |
| `PATCH /api/users/{id}/deactivate` | `USER_DEACTIVATE` |
| `GET /api/users/{id}/roles` | `USER_ROLE_READ` |
| `PUT /api/users/{id}/roles` | `USER_ROLE_ASSIGN` |
| `GET /api/roles`, `GET /api/roles/{id}` | `ROLE_READ` |
| `POST /api/roles` | `ROLE_CREATE` |
| `PUT /api/roles/{id}` | `ROLE_UPDATE` |
| `DELETE /api/roles/{id}` | `ROLE_DELETE` |

### Master data

| Endpoints | Permission |
|---|---|
| `GET /api/products`, `GET /api/products/{id}` | `PRODUCT_READ` |
| `POST /api/products` | `PRODUCT_CREATE` |
| `PUT /api/products/{id}` | `PRODUCT_UPDATE` |
| `PUT /api/products/{id}/deactivate` | `PRODUCT_DEACTIVATE` |
| `GET /api/products/types`, `GET /api/products/statuses` | `LOOKUP_READ` |
| `GET /api/warehouses`, `GET /api/warehouses/{id}` | `WAREHOUSE_READ` |
| `POST /api/warehouses` | `WAREHOUSE_CREATE` |
| `PUT /api/warehouses/{id}` | `WAREHOUSE_UPDATE` |
| `PUT /api/warehouses/{id}/deactivate` | `WAREHOUSE_DEACTIVATE` |
| `GET /api/warehouses/{warehouseId}/locations`, `GET /api/warehouses/{warehouseId}/locations/{id}` | `LOCATION_READ` |
| `POST /api/warehouses/{warehouseId}/locations` | `LOCATION_CREATE` |
| `PUT /api/warehouses/{warehouseId}/locations/{id}` | `LOCATION_UPDATE` |
| `PUT /api/warehouses/{warehouseId}/locations/{id}/deactivate` | `LOCATION_DEACTIVATE` |
| `GET /api/machines`, `GET /api/machines/{id}` | `MACHINE_READ` |
| `POST /api/machines` | `MACHINE_CREATE` |
| `PUT /api/machines/{id}` | `MACHINE_UPDATE` |
| `PUT /api/machines/{id}/deactivate` | `MACHINE_DEACTIVATE` |
| `PATCH /api/machines/{id}/status` | `MACHINE_STATUS_CHANGE` |
| `GET /api/production-lines`, `GET /api/production-lines/{id}` | `PRODUCTION_LINE_READ` |
| `POST /api/production-lines` | `PRODUCTION_LINE_CREATE` |
| `PUT /api/production-lines/{id}` | `PRODUCTION_LINE_UPDATE` |
| `PUT /api/production-lines/{id}/deactivate` | `PRODUCTION_LINE_DEACTIVATE` |

### Bill of materials

| Endpoints | Permission |
|---|---|
| `GET /api/boms`, `GET /api/boms/{id}` | `BOM_READ` |
| `POST /api/boms` | `BOM_CREATE` |
| `POST /api/boms/{id}/activate` | `BOM_ACTIVATE` |
| `POST /api/boms/{id}/new-version` | `BOM_CREATE_VERSION` |
| `POST /api/boms/{bomId}/items` | `BOM_ITEM_ADD` |
| `DELETE /api/boms/{bomId}/items/{itemId}` | `BOM_ITEM_DELETE` |
| `GET /api/boms/statuses` | `LOOKUP_READ` |

### Inventory

| Endpoints | Permission |
|---|---|
| `GET /api/stock-lots`, `GET /api/stock-lots/{id}` | `STOCK_LOT_READ` |
| `POST /api/stock-lots` | `STOCK_LOT_CREATE` |
| `GET /api/stock-movements` | `STOCK_MOVEMENT_READ` |
| `POST /api/stock-movements` | `STOCK_MOVEMENT_CREATE` |
| `GET /api/stock-balances` | `STOCK_BALANCE_READ` |
| `GET /api/lot-types`, `GET /api/stock-statuses`, `GET /api/movement-types` | `LOOKUP_READ` |

### Work orders

| Endpoints | Permission |
|---|---|
| `GET /api/work-orders`, `GET /api/work-orders/{id}` | `WORK_ORDER_READ` |
| `POST /api/work-orders` | `WORK_ORDER_CREATE` |
| `PUT /api/work-orders/{id}` | `WORK_ORDER_UPDATE` |
| `DELETE /api/work-orders/{id}` | `WORK_ORDER_DELETE` |
| `GET /api/work-orders/{workOrderId}/materials` | `WORK_ORDER_MATERIAL_READ` |
| `POST /api/work-orders/{workOrderId}/materials` | `WORK_ORDER_MATERIAL_ADD` |
| `DELETE /api/work-orders/{workOrderId}/materials/{materialId}` | `WORK_ORDER_MATERIAL_DELETE` |
| `GET /api/work-orders/{workOrderId}/events` | `WORK_ORDER_EVENT_READ` |
| `POST /api/work-orders/{workOrderId}/events` | `WORK_ORDER_EVENT_ADD` |
| `GET /api/work-orders/statuses`, `GET /api/work-orders/priorities`, `GET /api/work-orders/event-types` | `LOOKUP_READ` |

### Quality

| Endpoints | Permission |
|---|---|
| `GET /api/quality-inspections`, `GET /api/quality-inspections/{id}` | `QUALITY_INSPECTION_READ` |
| `POST /api/quality-inspections` | `QUALITY_INSPECTION_CREATE` |
| `DELETE /api/quality-inspections/{id}` | `QUALITY_INSPECTION_DELETE` |
| `GET /api/quality-inspections/{id}/results` | `QUALITY_RESULT_READ` |
| `POST /api/quality-inspections/{id}/results` | `QUALITY_RESULT_CREATE` |
| `GET /api/quality-inspections/statuses`, `GET /api/quality-inspections/defect-types` | `LOOKUP_READ` |

### Maintenance

| Endpoints | Permission |
|---|---|
| `GET /api/maintenance-tickets`, `GET /api/maintenance-tickets/{id}` | `MAINTENANCE_TICKET_READ` |
| `POST /api/maintenance-tickets` | `MAINTENANCE_TICKET_CREATE` |
| `PUT /api/maintenance-tickets/{id}` | `MAINTENANCE_TICKET_UPDATE` |
| `DELETE /api/maintenance-tickets/{id}` | `MAINTENANCE_TICKET_DELETE` |
| `GET /api/maintenance-tickets/{ticketId}/downtime` | `MACHINE_DOWNTIME_READ` |
| `POST /api/maintenance-tickets/{ticketId}/downtime` | `MACHINE_DOWNTIME_CREATE` |
| `GET /api/maintenance-tickets/types`, `GET /api/maintenance-tickets/statuses`, `GET /api/maintenance-tickets/priorities` | `LOOKUP_READ` |

## 3. Default seeded roles

These mappings are an idempotent baseline. Administrators may subsequently
change role-permission assignments through the API, subject to the
last-administrator invariant.

| Role | Baseline permissions |
|---|---|
| `ADMIN` | Every existing permission; new permissions and missing startup links are added without replacing other assignments |
| `WAREHOUSE_MANAGER` | `LOOKUP_READ`; all master-data `*_READ`; `LOCATION_CREATE`, `LOCATION_UPDATE`; `STOCK_LOT_READ`, `STOCK_LOT_CREATE`, `STOCK_MOVEMENT_READ`, `STOCK_MOVEMENT_CREATE`, `STOCK_BALANCE_READ`; `WORK_ORDER_MATERIAL_READ` |
| `PLANNER` | `LOOKUP_READ`; all master-data `*_READ`; all BOM permissions; `STOCK_LOT_READ`, `STOCK_BALANCE_READ`; all work-order, work-order-material, and work-order-event permissions; `MAINTENANCE_TICKET_CREATE` |
| `OPERATOR` | `LOOKUP_READ`; all master-data `*_READ`; `BOM_READ`; `WORK_ORDER_READ`, `WORK_ORDER_EVENT_READ`, `WORK_ORDER_EVENT_ADD`; `MAINTENANCE_TICKET_CREATE` |
| `QC_INSPECTOR` | `LOOKUP_READ`; all master-data `*_READ`; `BOM_READ`; `QUALITY_INSPECTION_READ`, `QUALITY_INSPECTION_CREATE`, `QUALITY_RESULT_READ`, `QUALITY_RESULT_CREATE` |
| `MAINTENANCE_ENGINEER` | `LOOKUP_READ`; all master-data `*_READ`; `MACHINE_UPDATE`, `MACHINE_STATUS_CHANGE`; `MAINTENANCE_TICKET_READ`, `MAINTENANCE_TICKET_CREATE`, `MAINTENANCE_TICKET_UPDATE`; `MACHINE_DOWNTIME_READ`, `MACHINE_DOWNTIME_CREATE` |
| `FACTORY_MANAGER` | `LOOKUP_READ`; all `*_READ`; `MAINTENANCE_TICKET_CREATE`, `MAINTENANCE_TICKET_UPDATE`; `MACHINE_DOWNTIME_CREATE` |
| `AUDITOR` | `LOOKUP_READ`; all master-data `*_READ`; `STOCK_LOT_READ`, `STOCK_MOVEMENT_READ`, `STOCK_BALANCE_READ` |

“All master-data `*_READ`” means:

- `PRODUCT_READ`
- `WAREHOUSE_READ`
- `LOCATION_READ`
- `MACHINE_READ`
- `PRODUCTION_LINE_READ`

“All `*_READ`” means every canonical read permission in the endpoint catalog,
not create, update, delete, activate, deactivate, assign, add, or status-change
permissions.
