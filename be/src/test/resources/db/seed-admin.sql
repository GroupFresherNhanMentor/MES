-- Seed minimal data for integration tests
INSERT INTO users (id, username, password_hash, active)
VALUES ('00000000-0000-0000-0000-000000000001', 'admin', 'placeholder-hash-not-used', true)
ON CONFLICT DO NOTHING;

INSERT INTO roles (id, name)
VALUES ('00000000-0000-0000-0000-000000000002', 'ADMIN')
ON CONFLICT DO NOTHING;

INSERT INTO user_roles (user_id, role_id)
VALUES ('00000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000002')
ON CONFLICT DO NOTHING;
