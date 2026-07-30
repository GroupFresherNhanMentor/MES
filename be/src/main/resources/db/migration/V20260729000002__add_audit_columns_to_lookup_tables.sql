ALTER TABLE qc_statuses
    ADD COLUMN created_by UUID REFERENCES users(id),
    ADD COLUMN updated_by UUID REFERENCES users(id);

ALTER TABLE qc_actions
    ADD COLUMN created_by UUID REFERENCES users(id),
    ADD COLUMN updated_by UUID REFERENCES users(id);

ALTER TABLE defect_types
    ADD COLUMN created_by UUID REFERENCES users(id),
    ADD COLUMN updated_by UUID REFERENCES users(id);
