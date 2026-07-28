ALTER TABLE defect_types
    DROP CONSTRAINT uq_defect_types_code,
    DROP COLUMN code,
    ADD CONSTRAINT uq_defect_types_name UNIQUE (name);
