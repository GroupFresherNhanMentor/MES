# Data Model: Inventory Reference Lookup Endpoints

## Entities, DTOs & Search Criteria

### 1. LotType & Criteria
- `LotType` domain entity
- `LotTypeSummaryDto` (`UUID id`, `String name`, `String description`)
- `LotTypeSearchCriteria` (extends `BaseSearchCriteria`)
- `LotTypeSearchRequest` (`String query`, `String name`, `Integer page`, `Integer size`)

### 2. StockStatus & Criteria
- `StockStatus` domain entity
- `StockStatusSummaryDto` (`UUID id`, `String name`, `String description`)
- `StockStatusSearchCriteria` (extends `BaseSearchCriteria`)
- `StockStatusSearchRequest` (`String query`, `String name`, `Integer page`, `Integer size`)

### 3. MovementType & Criteria
- `MovementType` domain entity
- `MovementTypeSummaryDto` (`UUID id`, `String name`, `String description`)
- `MovementTypeSearchCriteria` (extends `BaseSearchCriteria`)
- `MovementTypeSearchRequest` (`String query`, `String name`, `Integer page`, `Integer size`)

## Persistence Mapping

- `LOT_TYPES` jOOQ table $\to$ `LotTypePersistenceAdapter.search(criteria)` $\to$ `LotTypeSummaryDto`
- `STOCK_STATUSES` jOOQ table $\to$ `StockStatusPersistenceAdapter.search(criteria)` $\to$ `StockStatusSummaryDto`
- `MOVEMENT_TYPES` jOOQ table $\to$ `MovementTypePersistenceAdapter.search(criteria)` $\to$ `MovementTypeSummaryDto`
