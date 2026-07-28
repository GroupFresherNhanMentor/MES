# Implementation Tasks: Exception and List-Only Repository Refactoring

## Phase 1: Exceptions Refactoring
- [X] T1: Replace `IllegalArgumentException` in `StockBalance.java`, `StockLot.java`, `StockMovement.java`, and `MachineService.java` with domain exceptions extending `AppException` (e.g. `InvalidStockMovementException` / `DomainException`).

## Phase 2: Repository List-Only Refactoring
- [X] T2: Refactor `StockBalanceRepository.java` search method to return `List<StockBalance>`.
- [X] T3: Refactor `StockLotRepository.java` search method to return `List<StockLot>`.
- [X] T4: Refactor `StockMovementRepository.java` search method to return `List<StockMovement>`.
- [X] T5: Update `StockBalancePersistenceAdapter.java` search method to return `List<StockBalance>`.
- [X] T6: Update `StockLotPersistenceAdapter.java` search method to return `List<StockLot>`.
- [X] T7: Update `StockMovementPersistenceAdapter.java` search method to return `List<StockMovement>`.
- [X] T8: Update `InventoryService.java` to build `PageResponse<Dto>` from `List<T>` returned by repositories.

## Phase 3: Verification
- [X] T9: Run test suite (`mvn clean test`) to ensure all tests pass cleanly.
