# Data Model & Form Specs: Create BOM Dialog

## 1. Request / Response Interfaces

### `CreateBomRequest` (`fe/src/app/core/models/bom.model.ts`)
```typescript
export interface CreateBomRequest {
  finishedProductId: string;
  version?: number;
  bomStatusId?: string;
}
```

### `BomDto`
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

## 2. Reactive Form Structure

| Form Control | Type | Validators | Description |
|---|---|---|---|
| `finishedProductId` | `string` | `Validators.required` | ID of selected finished/semi-finished product |
| `version` | `number` | `Validators.required`, `Validators.min(1)` | Version number (defaults to 1) |
