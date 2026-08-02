ALTER TABLE stock_lots
    ADD COLUMN updated_at TIMESTAMPTZ,
    ADD COLUMN created_by UUID REFERENCES users(id),
    ADD COLUMN updated_by UUID REFERENCES users(id);
