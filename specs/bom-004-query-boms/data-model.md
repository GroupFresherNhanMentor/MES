# Data Model & Query Contracts: Get BOM List and BOM Detail (Query BOMs)

**Feature**: Query BOMs (`bom-004-query-boms`)  
**Created**: 2026-07-28  

## Query Parameters

### `GET /api/boms`

| Parameter | Type | Required | Default | Description |
|---|---|---|---|---|
| `page` | `int` | No | `0` | Page index (0-based) |
| `size` | `int` | No | `20` | Page size limit |
| `finishedProductId` | `UUID` | No | `null` | Optional filter by product ID |
| `bomStatusId` | `UUID` | No | `null` | Optional filter by status ID |

---

## Response DTO Models

### 1. `BomDto` Header Model

```json
{
  "id": "123e4567-e89b-12d3-a456-426614174000",
  "finishedProductId": "987e6543-e89b-12d3-a456-426614174000",
  "version": 1,
  "bomStatusId": "00000000-0000-0000-0000-000000000001",
  "createdBy": "11111111-1111-1111-1111-111111111111",
  "createdAt": "2026-07-28T10:00:00Z",
  "items": []
}
```

### 2. `BomItemDto` Line Item Model

```json
{
  "id": "22222222-2222-2222-2222-222222222222",
  "bomId": "123e4567-e89b-12d3-a456-426614174000",
  "materialProductId": "33333333-3333-3333-3333-333333333333",
  "quantityPerUnit": 2.5000,
  "unit": "PCS",
  "scrapRate": 0.0200
}
```
