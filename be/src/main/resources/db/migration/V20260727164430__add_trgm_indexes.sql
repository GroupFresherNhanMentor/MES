-- Full-text fuzzy search support via pg_trgm
-- Enables ILIKE '%keyword%' queries to use GIN indexes instead of sequential scans.
-- Covers all list pages where users search by name or code.

CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- Products & Materials (most-searched entity)
CREATE INDEX idx_products_name_trgm ON products USING GIN (name gin_trgm_ops);
CREATE INDEX idx_products_code_trgm ON products USING GIN (code gin_trgm_ops);

-- Work Orders (operators search by code)
CREATE INDEX idx_work_orders_code_trgm ON work_orders USING GIN (code gin_trgm_ops);

-- Stock Lots (warehouse staff search by lot number)
CREATE INDEX idx_stock_lots_lot_number_trgm ON stock_lots USING GIN (lot_number gin_trgm_ops);

-- Machines & Production Lines
CREATE INDEX idx_machines_name_trgm ON machines USING GIN (name gin_trgm_ops);
CREATE INDEX idx_machines_code_trgm ON machines USING GIN (code gin_trgm_ops);
CREATE INDEX idx_production_lines_name_trgm ON production_lines USING GIN (name gin_trgm_ops);
CREATE INDEX idx_production_lines_code_trgm ON production_lines USING GIN (code gin_trgm_ops);

-- Warehouses & Locations
CREATE INDEX idx_warehouses_name_trgm ON warehouses USING GIN (name gin_trgm_ops);
CREATE INDEX idx_warehouses_code_trgm ON warehouses USING GIN (code gin_trgm_ops);
CREATE INDEX idx_warehouse_locations_name_trgm ON warehouse_locations USING GIN (name gin_trgm_ops);
CREATE INDEX idx_warehouse_locations_code_trgm ON warehouse_locations USING GIN (code gin_trgm_ops);
