# Research Findings: Stock Transfers API

## 1. Transfer Stock Endpoint Design & Data Flow

### Decision
Implement `transferStock(StockTransferRequest request, UUID currentUserId)` in `InventoryService`, exposed via `POST /api/stock-transfers` in `InventoryController`.

### Rationale
- Clean Architecture requires domain/service logic in `InventoryService` implementing `InventoryUseCase`.
- Stock transfer requires atomic modification of two stock balances (source & destination) and recording two movements (`TRANSFER_OUT`, `TRANSFER_IN`).
- `@Transactional` ensures atomic updates: if any step fails (e.g. insufficient balance, missing destination location, movement failure), all DB changes roll back.

### Alternatives Considered
- *Separate API calls for transfer out and transfer in*: Rejected because client-side multi-step calls introduce inconsistency risk if second call fails.
- *Single movement record*: Rejected because requirement explicitly mandates two movements (`TRANSFER_OUT` and `TRANSFER_IN`) for audit and per-location traceability.

---

## 2. Movement Type & Stock Status Resolution

### Decision
Look up `MovementType` by name (`TRANSFER_OUT` and `TRANSFER_IN`) and `StockStatus` by name (`AVAILABLE`) using existing repositories (`MovementTypeRepository`, `StockStatusRepository`) and constants (`MovementTypeConstants`, `StockStatusConstants`).

### Rationale
- Movement types and stock statuses are stored in DB tables (`movement_types`, `stock_statuses`).
- `MovementTypeConstants.TRANSFER_OUT = "TRANSFER_OUT"`, `MovementTypeConstants.TRANSFER_IN = "TRANSFER_IN"`, `StockStatusConstants.AVAILABLE = "AVAILABLE"` are standard domain constants.

---

## 3. Response DTO Structure

### Decision
Define `StockTransferResponse` containing:
- `StockMovementDto transferOutMovement`
- `StockMovementDto transferInMovement`
- `StockBalanceDto sourceBalance`
- `StockBalanceDto destinationBalance`

### Rationale
- Gives clients full visibility into both generated movements and updated source/destination balances in a single clean payload.
