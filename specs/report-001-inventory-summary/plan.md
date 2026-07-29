# Implementation Plan: Inventory Summary Report

**Branch**: `feature/report-001-inventory-summary` | **Date**: 2026-07-29 | **Spec**: [spec.md](file:///e:/Fresher/ojt/MES/specs/report-001-inventory-summary/spec.md)

**Input**: Feature specification from `/specs/report-001-inventory-summary/spec.md` (FR-RPT-001)

## Summary

This feature implements the `GET /api/reports/inventory-summary` endpoint to deliver an aggregated stock summary report across products and warehouses. It queries `stock_balances`, `products`, `warehouses`, and `stock_statuses` using jOOQ group-by aggregation to calculate `availableQuantity`, `reservedQuantity`, `qualityInspectionQuantity`, `onHoldQuantity`, `scrappedQuantity`, and `totalOnHand`. Access is restricted to `ADMIN`, `FACTORY_MANAGER`, and `AUDITOR`.

## Technical Context

**Language/Version**: Java 25 / Spring Boot 4.1.0  
**Primary Dependencies**: Spring Web, Spring Security, jOOQ 3.21.5, Lombok  
**Storage**: PostgreSQL 18 (read queries from `stock_balances`, `products`, `warehouses`, `stock_statuses`)  
**Testing**: JUnit 5, Mockito  
**Target Platform**: Java Virtual Machine (JVM) backend service  
**Project Type**: Spring Boot Web REST Service (Clean Architecture)  
**Performance Goals**: Query execution completes in < 100ms  
**Constraints**: Read-only (`@Transactional(readOnly = true)`), role restriction `@PreAuthorize("hasAnyRole('ADMIN', 'FACTORY_MANAGER', 'AUDITOR')")`  
**Scale/Scope**: Aggregated result set (optional filtering by `warehouseId`, `productTypeId`, `productCode`)  

## Constitution Check

- Clean Architecture isolation: Package `fpt.qn.mes.report` with `presentation`, `application`, `domain`, `infrastructure` layers
- No JPA/Hibernate: jOOQ query aggregation via `ReportPersistenceAdapter`
- Security: Role check `@PreAuthorize("hasAnyRole('ADMIN', 'FACTORY_MANAGER', 'AUDITOR')")`

## Project Structure

### Documentation (this feature)

```text
specs/report-001-inventory-summary/
├── plan.md              # This file
├── research.md          # Phase 0 research findings
├── data-model.md        # Data model and DTO specs
├── quickstart.md        # Validation scenarios guide
└── contracts/
    └── inventory-summary-report-api.json  # OpenAPI contract specification
```

### Source Code

```text
be/src/main/java/fpt/qn/mes/report/
├── application/
│   ├── dto/
│   │   └── response/
│   │       └── InventorySummaryReportDto.java
│   ├── port/
│   │   └── in/
│   │       └── ReportUseCase.java
│   └── service/
│       └── ReportService.java
├── domain/
│   └── repository/
│       └── ReportRepository.java
├── infrastructure/
│   └── persistence/
│       └── ReportPersistenceAdapter.java
└── presentation/
    └── ReportController.java
```

**Structure Decision**: A new `report` package following 4-layer Clean Architecture.
