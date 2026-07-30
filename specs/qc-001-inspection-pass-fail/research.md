# Research: QC Inspection — Pass / Fail

**Date**: 2026-07-28

**Status**: No unknowns — all technical decisions resolved before planning.

---

## Decisions

| Decision | Choice | Rationale | Alternatives |
|----------|--------|-----------|-------------|
| fail(action=REWORK) counting | Counted as processed (reduces remaining) | Simplest for quantity tracking; stock stays at QUALITY_INSPECTION | Not counting (Option A) — harder to implement; separate rework_quantity column (Option C) — overengineered for current scope |
| Remaining quantity | Computed dynamically (SUM of results) | No DB column needed, always accurate | DB column — risk of drift |
| Action FK | action_id → qc_actions(id) | Referential integrity | VARCHAR — no constraint |
| Movement QC_RELEASE name | QC_RELEASE | Tên chuẩn của movement pass QC trong seed data | QC_PASS — tên cũ từ spec draft |
| BigDecimal | NUMERIC(18,4) for all qty | Team standard per existing schema | BigInteger — rejected by team lead |