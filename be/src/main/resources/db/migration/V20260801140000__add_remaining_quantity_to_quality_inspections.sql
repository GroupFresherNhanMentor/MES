ALTER TABLE quality_inspections
    ADD COLUMN remaining_quantity NUMERIC(18,4) NOT NULL DEFAULT 0;

UPDATE quality_inspections qi
SET remaining_quantity = qi.quantity - COALESCE(
    (SELECT SUM(qir.quantity) FROM quality_inspection_results qir WHERE qir.inspection_id = qi.id),
    0
);
