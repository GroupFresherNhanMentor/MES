# Quickstart: QC Inspection — Pass / Fail

## Prerequisites

- Docker services running: `docker-compose up -d`
- Backend started: `cd be && ./mvnw spring-boot:run`
- JDK 25, Maven 3.9+
- Admin JWT token ready (from `POST /api/auth/login`)

---

## Validation Scenarios

### Scenario 1: Pass QC — happy path

```bash
# 1. Authenticate as QC_INSPECTOR
TOKEN=$(curl -s -X POST /api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"qc_inspector","password":"password"}' | jq -r '.data.accessToken')

# 2. View pending inspections
curl -s -H "Authorization: Bearer $TOKEN" /api/quality-inspections?statusId=PENDING_INSPECTION_ID

# 3. Pass 95 out of 100 units
INSPECTION_ID="<uuid from step 2>"
curl -s -X POST "/api/quality-inspections/$INSPECTION_ID/pass" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"passedQuantity": 95.0, "note": "All within tolerance"}'
```

**Expected**: Status 200, `"qcStatusName": "PASSED"`, stock movement QC_RELEASE created.

---

### Scenario 2: Fail QC with SCRAP

```bash
# 1. Fail 5 units with SCRAP
curl -s -X POST "/api/quality-inspections/$INSPECTION_ID/fail" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "failedQuantity": 5.0,
    "actionId": "SCRAP_UUID",
    "defectTypeId": "SCRATCH_UUID",
    "reason": "Surface scratch"
  }'
```

**Expected**: Status 200, `"qcStatusName": "FAILED"`, stock → SCRAPPED, movement SCRAP created.

---

### Scenario 3: Partial inspection

```bash
# Pass 30, then pass 30, then fail 40
curl -s -X POST "/api/quality-inspections/$INSPECTION_ID/pass" -d '{"passedQuantity": 30}'
curl -s -X POST "/api/quality-inspections/$INSPECTION_ID/pass" -d '{"passedQuantity": 30}'
curl -s -X POST "/api/quality-inspections/$INSPECTION_ID/fail" \
  -d '{"failedQuantity": 40, "actionId": "SCRAP_UUID", "defectTypeId": "...", "reason": "..."}'
```

**Expected**: After first 2 passes → status still PENDING_INSPECTION (60/100 done). After fail → FAILED (100/100).

---

### Scenario 4: Validation error

```bash
# Fail without reason — should reject
curl -s -X POST "/api/quality-inspections/$INSPECTION_ID/fail" \
  -d '{"failedQuantity": 5, "actionId": "SCRAP_UUID", "defectTypeId": null, "reason": null}'
```

**Expected**: Status 400, error code `DEFECT_TYPE_REQUIRED` or `REASON_REQUIRED`.

---

### Scenario 5: Exceed remaining quantity

```bash
# Try to pass 200 when only 100 exist
curl -s -X POST "/api/quality-inspections/$INSPECTION_ID/pass" -d '{"passedQuantity": 200}'
```

**Expected**: Status 400, error code `INSUFFICIENT_REMAINING_QUANTITY`.

---

## Verification

After running scenarios, verify:

```bash
# Check stock balance moved correctly
curl -s -H "Authorization: Bearer $ADMIN_TOKEN" /api/stock-balances?productId=$PRODUCT_ID

# Check stock movements created
curl -s -H "Authorization: Bearer $ADMIN_TOKEN" /api/stock-movements?productId=$PRODUCT_ID
```

---

## References

- [Data model](data-model.md) — entities and relationships
- [API contracts](contracts/api.md) — endpoint details and roles
- [Spec](spec.md) — feature specification