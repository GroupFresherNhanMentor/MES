CREATE TABLE stock_adjustment_approvals (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    product_id UUID NOT NULL REFERENCES products(id),
    warehouse_id UUID NOT NULL REFERENCES warehouses(id),
    location_id UUID REFERENCES warehouse_locations(id),
    stock_balance_id UUID REFERENCES stock_balances(id) ON DELETE CASCADE,
    quantity_adjustment NUMERIC(15, 4) NOT NULL,
    reason TEXT NOT NULL,
    reference_no VARCHAR(100),
    created_by UUID NOT NULL REFERENCES users(id),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_stock_adjustment_approvals_product ON stock_adjustment_approvals(product_id);
CREATE INDEX idx_stock_adjustment_approvals_warehouse ON stock_adjustment_approvals(warehouse_id);
CREATE INDEX idx_stock_adjustment_approvals_balance ON stock_adjustment_approvals(stock_balance_id);
CREATE INDEX idx_stock_adjustment_approvals_created_by ON stock_adjustment_approvals(created_by);
