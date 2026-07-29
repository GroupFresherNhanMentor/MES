# Phase 0 Research Findings: Inventory Summary Report

## 1. jOOQ Aggregation Query Pattern

- **Decision**: Perform jOOQ conditional `SUM` aggregation grouped by `products.code`, `products.name`, and `warehouses.name`.
- **SQL Pattern**:
  ```sql
  SELECT 
      p.code AS product_code,
      p.name AS product_name,
      w.name AS warehouse_name,
      SUM(CASE WHEN ss.name = 'AVAILABLE' THEN sb.quantity ELSE 0 END) AS available_quantity,
      SUM(CASE WHEN ss.name = 'RESERVED' THEN sb.quantity ELSE 0 END) AS reserved_quantity,
      SUM(CASE WHEN ss.name = 'QUALITY_INSPECTION' THEN sb.quantity ELSE 0 END) AS quality_inspection_quantity,
      SUM(CASE WHEN ss.name = 'ON_HOLD' THEN sb.quantity ELSE 0 END) AS on_hold_quantity,
      SUM(CASE WHEN ss.name = 'SCRAPPED' THEN sb.quantity ELSE 0 END) AS scrapped_quantity,
      SUM(sb.quantity) AS total_on_hand
  FROM stock_balances sb
  JOIN products p ON sb.product_id = p.id
  JOIN warehouses w ON sb.warehouse_id = w.id
  JOIN stock_statuses ss ON sb.stock_status_id = ss.id
  WHERE (sb.warehouse_id = ? OR ? IS NULL)
    AND (p.product_type_id = ? OR ? IS NULL)
    AND (p.code ILIKE ? OR ? IS NULL)
  GROUP BY p.code, p.name, w.name;
  ```
- **Rationale**: Single round-trip database query for high performance (< 100ms) with zero N+1 overhead.

## 2. API Endpoint & Security

- **Decision**: `GET /api/reports/inventory-summary`
- **Security**: `@PreAuthorize("hasAnyRole('ADMIN', 'FACTORY_MANAGER', 'AUDITOR')")`
- **Rationale**: Restricts report access to managerial and audit personnel as specified in URS/SRS.
