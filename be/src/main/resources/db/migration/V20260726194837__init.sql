

-- ============================================================
-- 1. AUTHENTICATION & AUTHORIZATION
-- ============================================================

CREATE TABLE users (
    id              UUID PRIMARY KEY,
    username        VARCHAR(100) NOT NULL,
    password_hash   VARCHAR(255) NOT NULL,
    full_name       VARCHAR(255),
    active          BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_users_username UNIQUE (username)
);

CREATE TABLE roles (
    id              UUID PRIMARY KEY,
    name            VARCHAR(50) NOT NULL,
    description     VARCHAR(255),
    CONSTRAINT uq_roles_name UNIQUE (name)
);

CREATE TABLE permissions (
    id              UUID PRIMARY KEY,
    name            VARCHAR(100) NOT NULL,
    description     VARCHAR(255),
    CONSTRAINT uq_permissions_name UNIQUE (name)
);

CREATE TABLE user_roles (
    user_id         UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id         UUID NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

CREATE TABLE role_permissions (
    role_id         UUID NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    permission_id   UUID NOT NULL REFERENCES permissions(id) ON DELETE CASCADE,
    PRIMARY KEY (role_id, permission_id)
);

-- ============================================================
-- 2. MASTER DATA
-- ============================================================

CREATE TABLE product_types (
    id              UUID PRIMARY KEY,
    name            VARCHAR(50) NOT NULL,
    description     VARCHAR(255),
    CONSTRAINT uq_product_types_name UNIQUE (name)
);

CREATE TABLE product_statuses (
    id              UUID PRIMARY KEY,
    name            VARCHAR(50) NOT NULL,
    description     VARCHAR(255),
    CONSTRAINT uq_product_statuses_name UNIQUE (name)
);

CREATE TABLE units_of_measure (
    id              UUID PRIMARY KEY,
    name            VARCHAR(50) NOT NULL,
    description     VARCHAR(255),
    CONSTRAINT uq_units_of_measure_name UNIQUE (name)
);

CREATE TABLE products (
    id                  UUID PRIMARY KEY,
    code                VARCHAR(100) NOT NULL,
    name                VARCHAR(255) NOT NULL,
    product_type_id     UUID NOT NULL REFERENCES product_types(id),
    unit_id             UUID NOT NULL REFERENCES units_of_measure(id),
    product_status_id   UUID NOT NULL REFERENCES product_statuses(id),
    version             BIGINT NOT NULL DEFAULT 0,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by          UUID REFERENCES users(id),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_by          UUID REFERENCES users(id),
    CONSTRAINT uq_products_code UNIQUE (code)
);

CREATE TABLE warehouse_statuses (
    id              UUID PRIMARY KEY,
    name            VARCHAR(50) NOT NULL,
    description     VARCHAR(255),
    CONSTRAINT uq_warehouse_statuses_name UNIQUE (name)
);

CREATE TABLE warehouses (
    id                      UUID PRIMARY KEY,
    code                    VARCHAR(100) NOT NULL,
    name                    VARCHAR(255) NOT NULL,
    address                 VARCHAR(255),
    warehouse_status_id     UUID NOT NULL REFERENCES warehouse_statuses(id),
    created_at              TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by              UUID REFERENCES users(id),
    updated_at              TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_by              UUID REFERENCES users(id),
    CONSTRAINT uq_warehouses_code UNIQUE (code)
);

-- Bổ sung ngoài URS/diagram.puml: quan hệ many-to-many giữa Warehouse và User
-- (1 user quản lý nhiều warehouse, 1 warehouse có nhiều manager)
CREATE TABLE warehouse_managers (
    warehouse_id    UUID NOT NULL REFERENCES warehouses(id) ON DELETE CASCADE,
    user_id         UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    assigned_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
    assigned_by     UUID REFERENCES users(id),
    PRIMARY KEY (warehouse_id, user_id)
);

CREATE TABLE location_statuses (
    id              UUID PRIMARY KEY,
    name            VARCHAR(50) NOT NULL,
    description     VARCHAR(255),
    CONSTRAINT uq_location_statuses_name UNIQUE (name)
);

CREATE TABLE warehouse_locations (
    id                      UUID PRIMARY KEY,
    warehouse_id            UUID NOT NULL REFERENCES warehouses(id) ON DELETE CASCADE,
    code                    VARCHAR(100) NOT NULL,
    name                    VARCHAR(255),
    location_status_id      UUID NOT NULL REFERENCES location_statuses(id),
    created_at              TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by              UUID REFERENCES users(id),
    updated_at              TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_by              UUID REFERENCES users(id),
    CONSTRAINT uq_warehouse_locations_wh_code UNIQUE (warehouse_id, code)
);

CREATE TABLE line_statuses (
    id              UUID PRIMARY KEY,
    name            VARCHAR(50) NOT NULL,
    description     VARCHAR(255),
    CONSTRAINT uq_line_statuses_name UNIQUE (name)
);

CREATE TABLE production_lines (
    id              UUID PRIMARY KEY,
    code            VARCHAR(100) NOT NULL,
    name            VARCHAR(255) NOT NULL,
    line_status_id  UUID NOT NULL REFERENCES line_statuses(id),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by      UUID REFERENCES users(id),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_by      UUID REFERENCES users(id),
    CONSTRAINT uq_production_lines_code UNIQUE (code)
);

CREATE TABLE machine_statuses (
    id              UUID PRIMARY KEY,
    name            VARCHAR(50) NOT NULL,
    description     VARCHAR(255),
    CONSTRAINT uq_machine_statuses_name UNIQUE (name)
);

CREATE TABLE machines (
    id                      UUID PRIMARY KEY,
    production_line_id      UUID REFERENCES production_lines(id),
    code                    VARCHAR(100) NOT NULL,
    name                    VARCHAR(255) NOT NULL,
    machine_status_id       UUID NOT NULL REFERENCES machine_statuses(id),
    created_at              TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by              UUID REFERENCES users(id),
    updated_at              TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_by              UUID REFERENCES users(id),
    CONSTRAINT uq_machines_code UNIQUE (code)
);

-- ============================================================
-- 3. INVENTORY & STOCK MOVEMENT LEDGER
-- ============================================================

CREATE TABLE lot_types (
    id              UUID PRIMARY KEY,
    name            VARCHAR(50) NOT NULL,
    description     VARCHAR(255),
    CONSTRAINT uq_lot_types_name UNIQUE (name)
);

CREATE TABLE stock_lots (
    id              UUID PRIMARY KEY,
    lot_number      VARCHAR(100) NOT NULL,
    product_id      UUID NOT NULL REFERENCES products(id),
    lot_type_id     UUID REFERENCES lot_types(id),
    expiry_date     DATE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_stock_lots_lot_number UNIQUE (lot_number)
);

CREATE TABLE stock_statuses (
    id              UUID PRIMARY KEY,
    name            VARCHAR(50) NOT NULL,
    description     VARCHAR(255),
    CONSTRAINT uq_stock_statuses_name UNIQUE (name)
);

CREATE TABLE stock_balances (
    id                  UUID PRIMARY KEY,
    warehouse_id        UUID NOT NULL REFERENCES warehouses(id),
    location_id         UUID NOT NULL REFERENCES warehouse_locations(id),
    product_id          UUID NOT NULL REFERENCES products(id),
    lot_id              UUID NOT NULL REFERENCES stock_lots(id),
    stock_status_id     UUID NOT NULL REFERENCES stock_statuses(id),
    quantity            NUMERIC(18,4) NOT NULL DEFAULT 0,
    version             BIGINT NOT NULL DEFAULT 0,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT chk_stock_balances_qty_non_negative CHECK (quantity >= 0),
    CONSTRAINT uq_stock_balances_slot UNIQUE (warehouse_id, location_id, product_id, lot_id, stock_status_id)
);

CREATE TABLE movement_types (
    id              UUID PRIMARY KEY,
    name            VARCHAR(50) NOT NULL,
    description     VARCHAR(255),
    CONSTRAINT uq_movement_types_name UNIQUE (name)
);

-- stock_movements được đặt sau section Work Order (bên dưới), vì có FK work_order_id
-- trỏ tới bảng work_orders — bảng đó phải tồn tại trước.

-- ============================================================
-- 4. BOM MANAGEMENT
-- ============================================================

CREATE TABLE bom_statuses (
    id              UUID PRIMARY KEY,
    name            VARCHAR(50) NOT NULL,
    description     VARCHAR(255),
    CONSTRAINT uq_bom_statuses_name UNIQUE (name)
);

CREATE TABLE boms (
    id                      UUID PRIMARY KEY,
    finished_product_id     UUID NOT NULL REFERENCES products(id),
    version                 INTEGER NOT NULL,
    bom_status_id           UUID NOT NULL REFERENCES bom_statuses(id),
    created_by              UUID REFERENCES users(id),
    created_at              TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_boms_product_version UNIQUE (finished_product_id, version)
);

CREATE TABLE bom_items (
    id                      UUID PRIMARY KEY,
    bom_id                  UUID NOT NULL REFERENCES boms(id) ON DELETE CASCADE,
    material_product_id     UUID NOT NULL REFERENCES products(id),
    quantity_per_unit       NUMERIC(18,4) NOT NULL,
    unit_id                 UUID REFERENCES units_of_measure(id),
    scrap_rate              NUMERIC(6,4) NOT NULL DEFAULT 0,
    CONSTRAINT chk_bom_items_qty_positive CHECK (quantity_per_unit > 0)
);

-- ============================================================
-- 5. WORK ORDER & EXECUTION
-- ============================================================

CREATE TABLE work_order_statuses (
    id              UUID PRIMARY KEY,
    name            VARCHAR(50) NOT NULL,
    description     VARCHAR(255),
    is_initial      BOOLEAN NOT NULL DEFAULT FALSE,
    is_final        BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT uq_work_order_statuses_name UNIQUE (name)
);

CREATE TABLE work_order_status_transitions (
    id                  UUID PRIMARY KEY,
    from_status_id      UUID NOT NULL REFERENCES work_order_statuses(id),
    to_status_id        UUID NOT NULL REFERENCES work_order_statuses(id),
    is_active           BOOLEAN NOT NULL DEFAULT TRUE,
    description         VARCHAR(255),
    CONSTRAINT uq_wo_status_transition UNIQUE (from_status_id, to_status_id)
);

CREATE TABLE work_order_priorities (
    id              UUID PRIMARY KEY,
    name            VARCHAR(50) NOT NULL,
    description     VARCHAR(255),
    CONSTRAINT uq_work_order_priorities_name UNIQUE (name)
);

CREATE TABLE work_orders (
    id                      UUID PRIMARY KEY,
    code                    VARCHAR(100) NOT NULL,
    finished_product_id     UUID NOT NULL REFERENCES products(id),
    bom_id                  UUID NOT NULL REFERENCES boms(id),
    planned_quantity        NUMERIC(18,4) NOT NULL,
    planned_start_date      TIMESTAMPTZ,
    planned_end_date        TIMESTAMPTZ,
    priority_id             UUID REFERENCES work_order_priorities(id),
    work_order_status_id    UUID NOT NULL REFERENCES work_order_statuses(id),
    created_by              UUID REFERENCES users(id),
    created_at              TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_work_orders_code UNIQUE (code),
    CONSTRAINT chk_work_orders_qty_positive CHECK (planned_quantity > 0)
);

CREATE TABLE stock_movements (
    id                  UUID PRIMARY KEY,
    movement_type_id    UUID NOT NULL REFERENCES movement_types(id),
    product_id          UUID NOT NULL REFERENCES products(id),
    lot_id              UUID REFERENCES stock_lots(id),
    work_order_id       UUID REFERENCES work_orders(id),
    from_warehouse_id   UUID REFERENCES warehouses(id),
    from_location_id    UUID REFERENCES warehouse_locations(id),
    to_warehouse_id     UUID REFERENCES warehouses(id),
    to_location_id      UUID REFERENCES warehouse_locations(id),
    quantity            NUMERIC(18,4) NOT NULL,
    from_status_id      UUID REFERENCES stock_statuses(id),
    to_status_id        UUID REFERENCES stock_statuses(id),
    reason              VARCHAR(255),
    created_by          UUID REFERENCES users(id),
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT chk_stock_movements_qty_positive CHECK (quantity > 0)
);

CREATE TABLE work_order_materials (
    id                      UUID PRIMARY KEY,
    work_order_id           UUID NOT NULL REFERENCES work_orders(id) ON DELETE CASCADE,
    material_product_id     UUID NOT NULL REFERENCES products(id),
    required_quantity       NUMERIC(18,4) NOT NULL,
    reserved_quantity       NUMERIC(18,4) NOT NULL DEFAULT 0,
    consumed_quantity       NUMERIC(18,4) NOT NULL DEFAULT 0,
    CONSTRAINT uq_wom_wo_product UNIQUE (work_order_id, material_product_id)
);

CREATE TABLE work_order_event_types (
    id              UUID PRIMARY KEY,
    name            VARCHAR(50) NOT NULL,
    description     VARCHAR(255),
    CONSTRAINT uq_wo_event_types_name UNIQUE (name)
);

-- Bổ sung đúng theo URS mục 9 (Database Entity Draft): production_runs vốn bị
-- diagram.puml gộp vào work_order_events — tách lại thành 2 bảng độc lập:
-- production_runs = snapshot 1 lần thực thi (machine, thời gian, kết quả tổng)
-- work_order_events = ledger từng thao tác rời rạc (START/PAUSE/RESUME/COMPLETE)
CREATE TABLE production_runs (
    id                  UUID PRIMARY KEY,
    work_order_id       UUID NOT NULL REFERENCES work_orders(id) ON DELETE CASCADE,
    machine_id          UUID NOT NULL REFERENCES machines(id),
    production_line_id  UUID REFERENCES production_lines(id),
    operator_id         UUID REFERENCES users(id),
    start_time          TIMESTAMPTZ NOT NULL,
    end_time            TIMESTAMPTZ,
    actual_quantity     NUMERIC(18,4),
    good_quantity       NUMERIC(18,4),
    defect_quantity     NUMERIC(18,4),
    scrap_quantity      NUMERIC(18,4)
);

CREATE TABLE work_order_events (
    id                      UUID PRIMARY KEY,
    work_order_id           UUID NOT NULL REFERENCES work_orders(id) ON DELETE CASCADE,
    production_run_id       UUID REFERENCES production_runs(id),
    event_type_id           UUID NOT NULL REFERENCES work_order_event_types(id),
    operator_id             UUID REFERENCES users(id),
    event_timestamp         TIMESTAMPTZ NOT NULL DEFAULT now(),
    note                    VARCHAR(500)
);

-- ============================================================
-- 6. QUALITY CONTROL
-- ============================================================

CREATE TABLE qc_statuses (
    id              UUID PRIMARY KEY,
    name            VARCHAR(50) NOT NULL,
    description     VARCHAR(255),
    CONSTRAINT uq_qc_statuses_name UNIQUE (name)
);

CREATE TABLE quality_inspections (
    id                      UUID PRIMARY KEY,
    work_order_id           UUID NOT NULL REFERENCES work_orders(id),
    product_id              UUID NOT NULL REFERENCES products(id),
    lot_id                  UUID NOT NULL REFERENCES stock_lots(id),
    quantity                NUMERIC(18,4) NOT NULL,
    qc_status_id            UUID NOT NULL REFERENCES qc_statuses(id),
    created_at              TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT chk_qi_qty_positive CHECK (quantity > 0)
);

CREATE TABLE defect_types (
    id              UUID PRIMARY KEY,
    code            VARCHAR(50) NOT NULL,
    name            VARCHAR(255) NOT NULL,
    description     VARCHAR(255),
    CONSTRAINT uq_defect_types_code UNIQUE (code)
);

CREATE TABLE quality_inspection_results (
    id                      UUID PRIMARY KEY,
    inspection_id           UUID NOT NULL REFERENCES quality_inspections(id) ON DELETE CASCADE,
    is_pass                 BOOLEAN NOT NULL,
    quantity                NUMERIC(18,4) NOT NULL,
    defect_type_id          UUID REFERENCES defect_types(id),
    reason                  VARCHAR(500),
    action                  VARCHAR(50),
    inspector_id            UUID NOT NULL REFERENCES users(id),
    inspected_at            TIMESTAMPTZ NOT NULL DEFAULT now(),
    note                    VARCHAR(500),
    CONSTRAINT chk_qir_qty_positive CHECK (quantity > 0),
    CONSTRAINT chk_qir_fail_requires_reason CHECK (
        is_pass = TRUE OR (defect_type_id IS NOT NULL AND reason IS NOT NULL)
    )
);

-- ============================================================
-- 7. MAINTENANCE MANAGEMENT
-- ============================================================

CREATE TABLE maintenance_ticket_types (
    id              UUID PRIMARY KEY,
    name            VARCHAR(50) NOT NULL,
    description     VARCHAR(255),
    CONSTRAINT uq_mtt_name UNIQUE (name)
);

CREATE TABLE maintenance_ticket_statuses (
    id              UUID PRIMARY KEY,
    name            VARCHAR(50) NOT NULL,
    description     VARCHAR(255),
    CONSTRAINT uq_mts_name UNIQUE (name)
);

CREATE TABLE maintenance_ticket_priorities (
    id              UUID PRIMARY KEY,
    name            VARCHAR(50) NOT NULL,
    description     VARCHAR(255),
    CONSTRAINT uq_maintenance_ticket_priorities_name UNIQUE (name)
);

CREATE TABLE maintenance_tickets (
    id                      UUID PRIMARY KEY,
    machine_id              UUID NOT NULL REFERENCES machines(id),
    ticket_type_id          UUID NOT NULL REFERENCES maintenance_ticket_types(id),
    priority_id             UUID REFERENCES maintenance_ticket_priorities(id),
    description             VARCHAR(500),
    ticket_status_id        UUID NOT NULL REFERENCES maintenance_ticket_statuses(id),
    assigned_engineer_id    UUID REFERENCES users(id),
    created_by              UUID REFERENCES users(id),
    created_at              TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE machine_downtimes (
    id                      UUID PRIMARY KEY,
    ticket_id               UUID NOT NULL REFERENCES maintenance_tickets(id),
    machine_id              UUID NOT NULL REFERENCES machines(id),
    start_time              TIMESTAMPTZ NOT NULL,
    end_time                TIMESTAMPTZ,
    total_downtime_minutes  BIGINT,
    root_cause              VARCHAR(500),
    action_taken            VARCHAR(500),
    CONSTRAINT uq_downtime_per_ticket UNIQUE (ticket_id)
);

-- ============================================================
-- 8. AUDIT LOG & IDEMPOTENCY
-- ============================================================

CREATE TABLE audit_logs (
    id              UUID PRIMARY KEY,
    actor_id        UUID NOT NULL REFERENCES users(id),
    action          VARCHAR(100) NOT NULL,
    entity_type     VARCHAR(100) NOT NULL,
    entity_id       UUID,
    old_value       TEXT,
    new_value       TEXT,
    ip_address      VARCHAR(50),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE idempotency_keys (
    id              UUID PRIMARY KEY,
    key             VARCHAR(255) NOT NULL,
    request_path    VARCHAR(255) NOT NULL,
    response_body   TEXT,
    status_code     INTEGER,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_idempotency_key UNIQUE (key)
);

-- ============================================================
-- INDEXES cho query/filter chính (mục 6.5 & 8 URS)
-- ============================================================

CREATE INDEX idx_stock_balances_product ON stock_balances(product_id);
CREATE INDEX idx_stock_balances_warehouse ON stock_balances(warehouse_id);
CREATE INDEX idx_stock_movements_product ON stock_movements(product_id);
CREATE INDEX idx_stock_movements_work_order ON stock_movements(work_order_id);
CREATE INDEX idx_stock_movements_from_warehouse ON stock_movements(from_warehouse_id);
CREATE INDEX idx_stock_movements_to_warehouse ON stock_movements(to_warehouse_id);
CREATE INDEX idx_stock_movements_type ON stock_movements(movement_type_id);
CREATE INDEX idx_stock_movements_created_at ON stock_movements(created_at);
CREATE INDEX idx_work_orders_status ON work_orders(work_order_status_id);
CREATE INDEX idx_production_runs_work_order ON production_runs(work_order_id);
CREATE INDEX idx_work_orders_finished_product ON work_orders(finished_product_id);
CREATE INDEX idx_quality_inspections_wo ON quality_inspections(work_order_id);
CREATE INDEX idx_quality_inspections_status ON quality_inspections(qc_status_id);
CREATE INDEX idx_maintenance_tickets_machine ON maintenance_tickets(machine_id);
CREATE INDEX idx_maintenance_tickets_assigned_engineer ON maintenance_tickets(assigned_engineer_id);
CREATE INDEX idx_machine_downtimes_machine ON machine_downtimes(machine_id);
CREATE INDEX idx_audit_logs_actor ON audit_logs(actor_id);
CREATE INDEX idx_audit_logs_entity ON audit_logs(entity_type, entity_id);
CREATE INDEX idx_audit_logs_created_at ON audit_logs(created_at);
CREATE INDEX idx_boms_finished_product ON boms(finished_product_id);