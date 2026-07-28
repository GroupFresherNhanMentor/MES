# Quickstart & Verification Guide: Inventory Application Services

**Feature Branch**: `20260728084808-inventory-services`

This document details how to run unit and integration tests to verify the `InventoryServiceImpl` implementation without needing presentation controllers.

## Prerequisites

- Java 25 JDK
- Maven Wrapper (`./mvnw`)
- Docker Desktop or Docker Engine running (for PostgreSQL testcontainers in integration tests)

## Setup Commands

Ensure database container is active (if running integration tests against local environment):

```bash
cd be
docker-compose up -d postgres
```

## Service Layer Unit Test Verification

To execute isolated unit tests for `InventoryServiceImpl` (using Mockito for repository mocks):

```bash
cd be
./mvnw test -Dtest=InventoryServiceImplTest
```

### Expected Output:
- `getStockLotById_ShouldReturnDto_WhenFound`: PASSED
- `createStockLot_ShouldSaveAndReturnDto`: PASSED
- `recordMovement_ShouldUpdateBalanceAndSaveMovement_WhenValid`: PASSED
- `recordMovement_ShouldThrowInsufficientStockException_WhenStockIsLow`: PASSED

---

## Service Layer Integration & Concurrency Test Verification

To execute full database integration tests validating jOOQ repository mapping and concurrency locks:

```bash
cd be
./mvnw test -Dtest=InventoryIntegrationTest
```

### Expected Output:
- `recordMovement_ConcurrentDeductions_ShouldMaintainStockBalanceIntegrity`: PASSED
- `recordMovement_InsufficientStock_ShouldRollbackTransaction`: PASSED
- `createStockLot_UniqueLotNumberConstraint_ShouldFail`: PASSED

---

## Code Coverage Audit

Run full test suite with coverage report:

```bash
cd be
./mvnw clean test
```

Inspect coverage under `be/target/site/jacoco/index.html` to confirm `InventoryServiceImpl` exceeds 90% instruction and branch coverage.
