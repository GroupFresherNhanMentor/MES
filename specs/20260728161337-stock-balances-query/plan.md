# Implementation Plan: Query Stock Balances Endpoint

**Branch**: `20260728161337-stock-balances-query` | **Date**: 2026-07-28 | **Spec**: [spec.md](spec.md)

## Summary

Implement `GET /api/stock-balances` in `InventoryController` accepting `StockBalanceSearchRequest request` directly without `@ModelAttribute`. `InventoryService` queries `balanceRepository.search(criteria)` using `StockBalanceSearchCriteria` mapped from `request`, returning `PageResponse<StockBalanceDto>`.

## Technical Context

**Language/Version**: Java 25
**Primary Dependencies**: Spring Boot 4.1.0, jOOQ 3.21
**Storage**: PostgreSQL 18
**Testing**: JUnit 5, Mockito, SpringBootTest

## Constitution Check

- [x] Controller parameter uses `StockBalanceSearchRequest request` without `@ModelAttribute`.
- [x] Application Service maps domain `List<StockBalance>` from `balanceRepository.search(...)` to `PageResponse<StockBalanceDto>`.
- [x] Clean Architecture boundaries are maintained.

## Project Structure

### Documentation (this feature)

```text
specs/20260728161337-stock-balances-query/
├── plan.md              
├── research.md          
├── data-model.md        
├── quickstart.md        
└── tasks.md             
```
