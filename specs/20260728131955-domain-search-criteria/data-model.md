# Data Model Specification: Inventory Domain Search Criteria & PageResponse Refactoring

**Feature Branch**: `20260728131955-domain-search-criteria`

## Base Classes & Criteria Models

### 1. `PageRequest` (`fpt.qn.mes.common.dto.request.PageRequest`)
- `page`: `int` (default 0, `@Min(0)`)
- `size`: `int` (default 20, `@Min(1) @Max(100)`)
- **Layer**: Presentation / Application DTOs

### 2. `BaseSearchCriteria` (`fpt.qn.mes.common.dto.BaseSearchCriteria`)
- `page`: `int` (default 0)
- `size`: `int` (default 20)
- **Layer**: Pure Java Base Class for Domain Criteria (No validation annotations)

### 3. `PageResponse<T>` (`fpt.qn.mes.common.dto.response.PageResponse<T>`)
- `totalElements`: `long`
- `totalPages`: `int`
- `pageNumber`: `int`
- `pageSize`: `int`
- `items`: `List<T>`
- Method: `map(Function<T, U> mapper)`

---

## Application Request DTOs (`application/dto/request/`)

### 1. `StockBalanceSearchRequest` (extends `PageRequest`)
- `warehouseId` (`UUID`): Optional filter by warehouse.
- `locationId` (`UUID`): Optional filter by location.
- `productId` (`UUID`): Optional filter by product.
- `lotId` (`UUID`): Optional filter by lot.
- `stockStatusId` (`UUID`): Optional filter by status.

### 2. `StockLotSearchRequest` (extends `PageRequest`)
- `productId` (`UUID`): Optional filter by product.
- `lotTypeId` (`UUID`): Optional filter by lot type.
- `lotNumber` (`String`): Optional filter by lot number (contains/case-insensitive).
- `expiryBefore` (`LocalDate`): Optional filter by expiry date threshold.

### 3. `StockMovementSearchRequest` (extends `PageRequest`)
- `movementTypeId` (`UUID`): Optional filter by movement type.
- `productId` (`UUID`): Optional filter by product.
- `lotId` (`UUID`): Optional filter by lot.
- `warehouseId` (`UUID`): Optional filter by warehouse.
- `locationId` (`UUID`): Optional filter by location.
- `referenceNo` (`String`): Optional filter by reference number.

---

## Domain Search Criteria Models (`domain/repository/`)

### 1. `StockBalanceSearchCriteria` (extends `BaseSearchCriteria`)
- `warehouseId` (`UUID`): Optional filter by warehouse.
- `locationId` (`UUID`): Optional filter by location.
- `productId` (`UUID`): Optional filter by product.
- `lotId` (`UUID`): Optional filter by lot.
- `stockStatusId` (`UUID`): Optional filter by status.

---

### 2. `StockLotSearchCriteria` (extends `BaseSearchCriteria`)
- `productId` (`UUID`): Optional filter by product.
- `lotTypeId` (`UUID`): Optional filter by lot type.
- `lotNumber` (`String`): Optional filter by lot number.
- `expiryBefore` (`LocalDate`): Optional filter by expiry date threshold.

---

### 3. `StockMovementSearchCriteria` (extends `BaseSearchCriteria`)
- `movementTypeId` (`UUID`): Optional filter by movement type.
- `productId` (`UUID`): Optional filter by product.
- `lotId` (`UUID`): Optional filter by lot.
- `warehouseId` (`UUID`): Optional filter by warehouse.
- `locationId` (`UUID`): Optional filter by location.
- `referenceNo` (`String`): Optional filter by reference number.

---

## Repository Interface Contract Refactoring

```java
public interface StockBalanceRepository {
    PageResponse<StockBalance> search(StockBalanceSearchCriteria criteria);
}

public interface StockLotRepository {
    PageResponse<StockLot> search(StockLotSearchCriteria criteria);
}

public interface StockMovementRepository {
    PageResponse<StockMovement> search(StockMovementSearchCriteria criteria);
}
```
