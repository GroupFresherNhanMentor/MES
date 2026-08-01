ALTER TABLE audit_logs
    ALTER COLUMN old_value TYPE JSONB USING old_value::jsonb,
    ALTER COLUMN new_value TYPE JSONB USING new_value::jsonb;