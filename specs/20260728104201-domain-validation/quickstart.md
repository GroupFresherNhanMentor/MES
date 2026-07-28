# Quickstart & Domain Test Verification Guide: Domain Entity Validation

**Feature Branch**: `20260728104201-domain-validation`

This document outlines how to execute pure domain unit tests to verify domain entity invariants, Value Objects, and validation rules without starting Spring Boot or Docker.

## Prerequisites

- Java 25 JDK
- Maven Wrapper (`./mvnw`)

## Domain Unit Test Verification Commands

Run all isolated domain unit tests (`StockBalanceTest`, `StockLotTest`, `StockMovementTest`, `StockStatusTest`, `LotTypeTest`, `MovementTypeTest`):

```bash
cd be
./mvnw test -Dtest=*Test
```

### Expected Output:
- `StockBalanceTest.deductQuantity_UnissuableStatus_ThrowsIllegalStateException`: PASSED
- `StockBalanceTest.deductQuantity_ExceedsOnHand_ThrowsIllegalArgumentException`: PASSED
- `StockLotTest.create_MissingExpiryDateForPerishableType_ThrowsIllegalArgumentException`: PASSED
- `StockLotTest.create_BlankLotNumber_ThrowsIllegalArgumentException`: PASSED
- `StockMovementTest.create_NegativeQuantity_ThrowsIllegalArgumentException`: PASSED

---

## Zero Framework Coupling Audit

Verify that `domain/entities/` contains zero framework annotations (no `@Entity`, no `@Table`, no `@Transactional`, no `jOOQ` classes):

```bash
grep -rn "org.springframework" be/src/main/java/fpt/qn/mes/inventory/domain/entities/
grep -rn "org.jooq" be/src/main/java/fpt/qn/mes/inventory/domain/entities/
```

Expected output: **No results found** (0 imports).
