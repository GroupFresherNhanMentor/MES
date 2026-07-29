# Quickstart & Verification Guide: Domain Search Criteria & PageResponse Refactoring

**Feature Branch**: `20260728131955-domain-search-criteria`

## Prerequisites

- JDK 25 installed
- Maven (`./mvnw`)
- Docker (for Testcontainers PostgreSQL tests)

## Runnable Validation Scenarios

### Scenario 1: Execute Domain Search Criteria Unit Tests

Run unit tests verifying search criteria extension from `PageRequest` and `PageResponse` output:

```bash
cd be && ./mvnw test -Dtest="*SearchCriteriaTest"
```

**Expected Outcome**:
All tests pass cleanly verifying `StockBalanceSearchCriteria`, `StockLotSearchCriteria`, and `StockMovementSearchCriteria` fields and `PageResponse<T>` return wrappers.

---

### Scenario 2: Execute Persistence Adapter Search Tests

Run persistence adapter integration tests with PostgreSQL:

```bash
cd be && ./mvnw test -Dtest="*PersistenceAdapterTest"
```

**Expected Outcome**:
jOOQ dynamic query building produces valid SQL queries with `PageResponse<T>` metadata (`totalElements`, `totalPages`, `pageNumber`, `pageSize`).
