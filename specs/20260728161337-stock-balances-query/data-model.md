# Data Model

No schema changes required. Uses `stock_balances` and `StockBalanceSearchRequest`.

## Interface Signatures

### `InventoryUseCase`
```java
PageResponse<StockBalanceDto> getStockBalances(StockBalanceSearchRequest request);
```

### `InventoryController`
```java
@GetMapping("/api/stock-balances")
public ResponseEntity<ApiResponse<PageResponse<StockBalanceDto>>> getStockBalances(
        @Valid StockBalanceSearchRequest request) {
    return ResponseEntity.ok(ApiResponse.success(inventoryUseCase.getStockBalances(request), "OK"));
}
```
