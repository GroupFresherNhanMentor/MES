# Research & Decisions

## 1. Response Type at Controller & Application Layers
- **Decision**: Use `PageResponse<StockBalanceDto>` for `GET /api/stock-balances`.
- **Rationale**: Standardizes all query list endpoints across MES to return paginated envelopes with metadata (`pageNumber`, `pageSize`, `totalElements`, `totalPages`).

## 2. Converting Domain List to PageResponse
- **Decision**: In `InventoryService.getStockBalances(int page, int size, UUID warehouseId, UUID productId)`, retrieve `List<StockBalance>` from repository, map to `List<StockBalanceDto>`, and wrap in `PageResponse`.
- **Rationale**: Keeps repository clean (returning `List<StockBalance>`) while meeting REST API pagination requirements at the presentation/application boundaries.
