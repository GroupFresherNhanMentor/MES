# REST API Contracts — Master Data Management

Base URL: `/api`

## Common Patterns

### Response Envelope

```json
{
  "success": true,
  "data": { ... },
  "message": "OK",
  "timestamp": "2026-07-28T12:00:00Z"
}
```

### Error Response

```json
{
  "success": false,
  "errorCode": "PRODUCT_NOT_FOUND",
  "message": "Product not found: 550e8400-e29b-41d4-a716-446655440000",
  "timestamp": "2026-07-28T12:00:00Z"
}
```

### Paginated Response (list endpoints)

```json
{
  "success": true,
  "data": {
    "items": [ ... ],
    "total": 42,
    "page": 0,
    "size": 20
  },
  "message": "OK",
  "timestamp": "2026-07-28T12:00:00Z"
}
```

### Pagination Query Params

| Param | Type | Default | Description |
|-------|------|---------|-------------|
| page | int | 0 | Zero-indexed page number |
| size | int | 20 | Page size |

### HTTP Status Codes

| Scenario | Status |
|----------|--------|
| Successful read | 200 OK |
| Successful create | 201 Created |
| Successful update | 200 OK |
| Successful deactivation | 200 OK |
| Validation error | 400 Bad Request |
| Unauthenticated (no JWT) | 401 Unauthorized |
| Forbidden (wrong role) | 403 Forbidden |
| Resource not found | 404 Not Found |
| Duplicate code / conflict | 409 Conflict |
| Deactivation blocked (has references) | 409 Conflict |

---

## Products

### `GET /api/products` — List products (paginated)

**Query Params**: `page`, `size`, `productType` (optional filter), `status` (optional filter)

**Response 200**:
```json
{
  "success": true,
  "data": {
    "items": [
      {
        "id": "uuid",
        "code": "SP-001",
        "name": "Steel Plate",
        "productTypeId": "uuid",
        "productTypeName": "RAW_MATERIAL",
        "unitId": "uuid",
        "unitName": "KG",
        "productStatusId": "uuid",
        "productStatusName": "ACTIVE",
        "version": 0,
        "createdAt": "2026-07-28T12:00:00Z",
        "updatedAt": "2026-07-28T12:00:00Z"
      }
    ],
    "total": 1,
    "page": 0,
    "size": 20
  },
  "message": "OK",
  "timestamp": "2026-07-28T12:00:00Z"
}
```

### `GET /api/products/{id}` — Get product by ID

**Response 200**: Single product object (same shape as item above)

**Response 404**: `PRODUCT_NOT_FOUND`

### `POST /api/products` — Create product (ADMIN)

**Request Body**:
```json
{
  "code": "SP-001",
  "name": "Steel Plate",
  "productTypeId": "uuid",
  "unitId": "uuid"
}
```

**Validation**: `@NotBlank code`, `@NotBlank name`, `@NotNull productTypeId`, `@NotNull unitId`

**Response 201**: Created product

**Response 409**: `PRODUCT_CODE_DUPLICATE`

### `PUT /api/products/{id}` — Update product (ADMIN)

**Request Body**:
```json
{
  "code": "SP-001",
  "name": "Steel Plate Updated",
  "productTypeId": "uuid",
  "unitId": "uuid"
}
```

**Response 200**: Updated product

**Response 404**: `PRODUCT_NOT_FOUND`

**Response 409**: `PRODUCT_CODE_DUPLICATE`

### `PUT /api/products/{id}/deactivate` — Deactivate product (soft delete, ADMIN)

**Response 200**: `{ "success": true, "data": null, "message": "Deactivated" }`

**Response 404**: `PRODUCT_NOT_FOUND`

**Response 409**: `PRODUCT_HAS_STOCK_MOVEMENTS` (cannot deactivate)

### Error Codes (Products)

| Error Code | HTTP Status | Condition |
|-----------|-------------|-----------|
| PRODUCT_NOT_FOUND | 404 | Product ID does not exist |
| PRODUCT_CODE_DUPLICATE | 409 | Code already in use |
| PRODUCT_HAS_STOCK_MOVEMENTS | 409 | Cannot deactivate — referenced in stock |

---

## Warehouses

### `GET /api/warehouses` — List warehouses (paginated)

**Query Params**: `page`, `size`, `status` (optional filter)

### `GET /api/warehouses/{id}` — Get warehouse by ID

### `POST /api/warehouses` — Create warehouse (ADMIN)

**Request Body**:
```json
{
  "code": "WH-HN",
  "name": "Hanoi Warehouse",
  "address": "123 Factory Street"
}
```

### `PUT /api/warehouses/{id}` — Update warehouse (ADMIN)

### `PUT /api/warehouses/{id}/deactivate` — Deactivate warehouse (ADMIN)

**Response 409**: `WAREHOUSE_HAS_ACTIVE_STOCK`

### Error Codes

| Error Code | HTTP Status | Condition |
|-----------|-------------|-----------|
| WAREHOUSE_NOT_FOUND | 404 | Warehouse ID does not exist |
| WAREHOUSE_CODE_DUPLICATE | 409 | Code already in use |
| WAREHOUSE_HAS_ACTIVE_STOCK | 409 | Cannot deactivate — contains active stock |

---

## Warehouse Locations (sub-resource of Warehouse)

### `GET /api/warehouses/{warehouseId}/locations` — List locations (paginated)

**Query Params**: `page`, `size`, `status` (optional filter)

### `GET /api/warehouses/{warehouseId}/locations/{id}` — Get location by ID

### `POST /api/warehouses/{warehouseId}/locations` — Create location (ADMIN)

**Request Body**:
```json
{
  "code": "A01",
  "name": "Aisle 1 Shelf A"
}
```

### `PUT /api/warehouses/{warehouseId}/locations/{id}` — Update location (ADMIN)

### `PUT /api/warehouses/{warehouseId}/locations/{id}/deactivate` — Deactivate location (ADMIN)

### Error Codes

| Error Code | HTTP Status | Condition |
|-----------|-------------|-----------|
| LOCATION_NOT_FOUND | 404 | Location ID does not exist |
| LOCATION_CODE_DUPLICATE | 409 | Code already in use within same warehouse |
| WAREHOUSE_NOT_FOUND | 404 | Parent warehouse does not exist |

---

## Production Lines

### `GET /api/production-lines` — List production lines (paginated)

**Query Params**: `page`, `size`, `status` (optional filter)

### `GET /api/production-lines/{id}` — Get production line by ID

### `POST /api/production-lines` — Create production line (ADMIN)

**Request Body**:
```json
{
  "code": "LINE-01",
  "name": "Assembly Line 1"
}
```

### `PUT /api/production-lines/{id}` — Update production line (ADMIN)

### `PUT /api/production-lines/{id}/deactivate` — Deactivate production line (ADMIN)

### Error Codes

| Error Code | HTTP Status | Condition |
|-----------|-------------|-----------|
| LINE_NOT_FOUND | 404 | Production line ID does not exist |
| LINE_CODE_DUPLICATE | 409 | Code already in use |
| LINE_HAS_ACTIVE_MACHINES | 409 | Cannot deactivate — has active machines |

---

## Machines (sub-resource of Production Line)

### `GET /api/production-lines/{lineId}/machines` — List machines (paginated)

**Query Params**: `page`, `size`, `status` (optional filter)

### `GET /api/production-lines/{lineId}/machines/{id}` — Get machine by ID

### `POST /api/production-lines/{lineId}/machines` — Create machine (ADMIN)

**Request Body**:
```json
{
  "code": "M-001",
  "name": "CNC Machine 1"
}
```

### `PUT /api/production-lines/{lineId}/machines/{id}` — Update machine (ADMIN)

### `PUT /api/production-lines/{lineId}/machines/{id}/deactivate` — Deactivate machine (ADMIN)

### `PUT /api/production-lines/{lineId}/machines/{id}/status` — Change machine status (ADMIN)

**Request Body**:
```json
{
  "statusId": "uuid"
}
```

**Response 400**: `MACHINE_INVALID_STATUS_TRANSITION`

### Error Codes

| Error Code | HTTP Status | Condition |
|-----------|-------------|-----------|
| MACHINE_NOT_FOUND | 404 | Machine ID does not exist |
| MACHINE_CODE_DUPLICATE | 409 | Code already in use |
| MACHINE_INVALID_STATUS_TRANSITION | 400 | Status change violates state machine |
| LINE_NOT_FOUND | 404 | Parent production line does not exist |
