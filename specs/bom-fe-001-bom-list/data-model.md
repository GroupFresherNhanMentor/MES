# Data Model & State Specs: BOM List Page

## 1. Data Models

### `BomDto` (`fe/src/app/core/models/bom.model.ts`)
```typescript
export interface BomDto {
  id: string;
  finishedProductId: string;
  finishedProductName?: string;
  finishedProductCode?: string;
  version: number;
  bomStatusId: string;
  bomStatusName?: string;
  createdBy?: string;
  createdAt: string;
  items?: BomItemDto[];
}
```

### `BomListParams`
```typescript
export interface BomListParams {
  page?: number;
  size?: number;
  finishedProductId?: string;
  bomStatusId?: string;
}
```

## 2. Component State Matrix

| State Signal | Initial Value | Trigger / Effect |
|---|---|---|
| `items` | `[]` | Updated upon `load()` API completion |
| `total` | `0` | Total record count for `MatPaginator` |
| `page` | `0` | Updated on `onPage($event)` |
| `size` | `20` | Updated on `onPage($event)` |
| `selectedProductId` | `null` | Query filter parameter for API |
| `selectedStatusId` | `null` | Query filter parameter for API |
