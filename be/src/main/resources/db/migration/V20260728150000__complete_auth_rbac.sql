DO $$
BEGIN
    IF EXISTS (
        SELECT upper(btrim(name))
        FROM roles
        GROUP BY upper(btrim(name))
        HAVING count(*) > 1
    ) THEN
        RAISE EXCEPTION 'Cannot normalize roles: duplicate names after trim/uppercase';
    END IF;

    IF EXISTS (
        SELECT upper(btrim(name))
        FROM permissions
        GROUP BY upper(btrim(name))
        HAVING count(*) > 1
    ) THEN
        RAISE EXCEPTION 'Cannot normalize permissions: duplicate names after trim/uppercase';
    END IF;
END
$$;

CREATE UNIQUE INDEX uq_roles_normalized_name
    ON roles (upper(btrim(name)));

CREATE UNIQUE INDEX uq_permissions_normalized_name
    ON permissions (upper(btrim(name)));

ALTER TABLE user_roles
    DROP CONSTRAINT user_roles_role_id_fkey,
    ADD CONSTRAINT user_roles_role_id_fkey
        FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE RESTRICT;

ALTER TABLE role_permissions
    DROP CONSTRAINT role_permissions_role_id_fkey,
    DROP CONSTRAINT role_permissions_permission_id_fkey,
    ADD CONSTRAINT role_permissions_role_id_fkey
        FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE RESTRICT,
    ADD CONSTRAINT role_permissions_permission_id_fkey
        FOREIGN KEY (permission_id) REFERENCES permissions(id) ON DELETE RESTRICT;

CREATE TABLE rbac_mutation_guard (
    id SMALLINT PRIMARY KEY,
    CONSTRAINT chk_rbac_mutation_guard_singleton CHECK (id = 1)
);

INSERT INTO rbac_mutation_guard(id) VALUES (1);
