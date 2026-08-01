ALTER TABLE quality_inspections
    DROP CONSTRAINT chk_qi_qty_positive;

ALTER TABLE quality_inspections
    ADD CONSTRAINT chk_qi_qty_non_negative CHECK (quantity >= 0);
