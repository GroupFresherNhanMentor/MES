-- ============================================================
-- Add qc_actions lookup table + FK constraint
-- ============================================================

CREATE TABLE qc_actions (
    id              UUID PRIMARY KEY,
    name            VARCHAR(50) NOT NULL,
    description     VARCHAR(255),
    CONSTRAINT uq_qc_actions_name UNIQUE (name)
);

-- Change action column from VARCHAR to FK
ALTER TABLE quality_inspection_results
    ADD COLUMN action_id UUID REFERENCES qc_actions(id);

UPDATE quality_inspection_results SET action_id = NULL WHERE action IS NULL;

-- Drop old VARCHAR column after migrating data
ALTER TABLE quality_inspection_results
    DROP COLUMN action;

-- ============================================================
-- Indexes for QC query performance
-- ============================================================
CREATE INDEX IF NOT EXISTS idx_qi_results_inspection ON quality_inspection_results(inspection_id);
CREATE INDEX IF NOT EXISTS idx_qi_results_inspector ON quality_inspection_results(inspector_id);