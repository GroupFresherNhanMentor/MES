# Research & Architectural Decisions: Inventory Application Services

**Feature Branch**: `20260728084808-inventory-services`

## Decision 1: Concurrency Control Strategy for Stock Balance Operations

### Decision
Use pessimistic locking (`SELECT ... FOR UPDATE`) in `StockBalanceRepositoryImpl` when fetching stock balance records prior to stock movement deductions (e.g. `ISSUE` or `TRANSFER` movements).

### Rationale
- Simultaneous manufacturing work orders or warehouse issue transactions target the same product lot and location.
- Pessimistic locking at the database row level prevents race conditions and quantity balance drift under high concurrent throughput.
- Guarantees immediate verification of `onHandQuantity >= requestedQuantity` before mutating balances.

### Alternatives Considered
- **Optimistic Locking with Version Field**: Rejected as the primary mechanism because frequent retries under high concurrent issuing lead to `OptimisticLockingFailureException` and poor user experience.

---

## Decision 2: Stock Movement Transaction Boundary & Balance Aggregation Rules

### Decision
Enclose stock movement recording (`recordMovement`) and balance updates in a single `@Transactional` Spring service method.

### Balance Mutators by Movement Type:
1. **RECEIPT / IN**: Creates or increments `StockBalance.onHandQuantity` for target warehouse location.
2. **ISSUE / OUT**: Decrements `StockBalance.onHandQuantity` for source location after verifying sufficient balance.
3. **TRANSFER**: Atomically decrements source location balance and increments target location balance.
4. **ADJUSTMENT**: Sets `StockBalance.onHandQuantity` directly to physical count and logs diff quantity in `StockMovement`.

### Rationale
Ensures 100% atomic consistency between audit logs (`StockMovement`) and current stock positions (`StockBalance`). If balance deduction fails (e.g., negative quantity exception), the entire transaction rolls back.

---

## Decision 3: Data Mapping Architecture via MapStruct

### Decision
Implement declarative MapStruct mappers (`StockLotDtoMapper`, `StockMovementDtoMapper`, `StockBalanceDtoMapper`) configured with `componentModel = "spring"`.

### Rationale
- Complies strictly with MES Constitution Principle III (MapStruct Only for Mapping).
- Cleanly separates jOOQ persistence records, domain entities (`StockLot`, `StockMovement`, `StockBalance`), and API response/request DTOs.
