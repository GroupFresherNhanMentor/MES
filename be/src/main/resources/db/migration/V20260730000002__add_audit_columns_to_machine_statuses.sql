ALTER TABLE machine_statuses
    ADD COLUMN created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    ADD COLUMN updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    ADD COLUMN created_by UUID REFERENCES users(id),
    ADD COLUMN updated_by UUID REFERENCES users(id);
