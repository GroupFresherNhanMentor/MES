# Feature Specification: Stock Transfers API

**Feature Branch**: `20260730132400-stock-transfers-api`

**Created**: 2026-07-30

**Status**: Draft

**Input**: User description: "POST /api/stock-transfers and these are requirements that I have to do. Input: fromWarehouseId fromLocationId toWarehouseId toLocationId productId lotId quantity. Business rule: Chỉ transfer stock AVAILABLE Quantity phải > 0 Nguồn phải đủ quantity Transfer phải tạo 2 movement: TRANSFER_OUT TRANSFER_IN. Acceptance criteria: Given WH1/A01 có 50 item A When transfer 20 item A sang WH1/A02 Then WH1/A01 còn 30 And WH1/A02 tăng 20 And có movement TRANSFER_OUT, TRANSFER_IN"

## Clarifications

### Session 2026-07-30
- Q: What is the exact self-transfer validation rule and reference number format? → A: Validate `fromLocationId != toLocationId` only (transfers between different locations are allowed regardless of warehouse IDs). Movement `reference_no` MUST be generated with prefix `TRF-YYYYMMDD-` formatted as `TRF-YYYYMMDD-{from_location_code}->{to_location_code}` (e.g. `TRF-20260730-WH1/A01->WH1/A02`).

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Transfer Available Inventory Between Warehouse Locations (Priority: P1)

As a Warehouse Operator or Warehouse Manager, I want to transfer an available quantity of a specific product lot from a source warehouse location to a destination warehouse location so that inventory counts and location tracking remain accurate across the factory.

**Why this priority**: Core inventory management capability required to move physical goods between storage locations and maintain precise stock balance traceability.

**Independent Test**: Can be tested independently by submitting a valid stock transfer request from a source location with sufficient available balance to a destination location, then verifying that source balance decreases, destination balance increases, and both TRANSFER_OUT and TRANSFER_IN movements are generated with reference number `TRF-YYYYMMDD-{from_location_code}->{to_location_code}`.

**Acceptance Scenarios**:

1. **Given** warehouse location WH1/A01 has an available stock balance of 50 units for Product A (Lot L1), **When** transferring 20 units of Product A to warehouse location WH1/A02 on date 2026-07-30, **Then** WH1/A01 available balance decreases to 30 units, **And** WH1/A02 available balance increases by 20 units, **And** two stock movement records (`TRANSFER_OUT` for WH1/A01 and `TRANSFER_IN` for WH1/A02) are created with `reference_no = "TRF-20260730-WH1/A01->WH1/A02"`.
2. **Given** a source location with 10 available units of Product A, **When** attempting to transfer 15 units of Product A, **Then** the transfer is rejected with an error indicating insufficient stock.
3. **Given** stock balance in non-AVAILABLE status (e.g. ON_HOLD, RESERVED, QUALITY_INSPECTION), **When** attempting to transfer the stock, **Then** the transfer is rejected with an error indicating only AVAILABLE stock can be transferred.

---

### Edge Cases

- What happens when transferring stock to the exact same warehouse location (`fromLocationId == toLocationId`)? The system must reject self-transfers with a validation error. No check is enforced on warehouse IDs.
- How does the system handle transfer requests where `quantity <= 0`? The system must reject the request with a validation error.
- What happens if the destination stock balance record does not exist yet? The system must automatically create a new stock balance record at the destination location with status `AVAILABLE`.
- What happens if specified warehouse, location, product, or lot IDs do not exist in the system? The system must return an appropriate resource not found exception.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: System MUST expose an endpoint `POST /api/stock-transfers` accepting `fromWarehouseId`, `fromLocationId`, `toWarehouseId`, `toLocationId`, `productId`, `lotId`, and `quantity`.
- **FR-002**: System MUST validate that transfer `quantity` is strictly greater than zero (`quantity > 0`).
- **FR-003**: System MUST validate that the stock status is `AVAILABLE`. Non-available stock cannot be transferred via this endpoint.
- **FR-004**: System MUST check that the source location (`fromWarehouseId`, `fromLocationId`, `productId`, `lotId`) possesses sufficient `AVAILABLE` quantity prior to completing the transfer.
- **FR-005**: System MUST deduct the transfer quantity from the source stock balance and add the transfer quantity to the destination stock balance atomically.
- **FR-006**: System MUST create exactly two stock movement log records for every executed transfer: one `TRANSFER_OUT` movement for the source location and one `TRANSFER_IN` movement for the destination location.
- **FR-007**: System MUST validate that `fromLocationId != toLocationId`. Same-location transfers are rejected. No restriction is placed on warehouse IDs.
- **FR-008**: System MUST generate movement `reference_no` formatted as `TRF-YYYYMMDD-{from_location_code}->{to_location_code}` (e.g., `TRF-20260730-WH1/A01->WH1/A02`).

### Key Entities *(include if feature involves data)*

- **StockTransferRequest**: Request payload containing source warehouse/location, destination warehouse/location, product ID, lot ID, and transfer quantity.
- **StockBalance**: Entity tracking quantity of a product lot at a specific warehouse location for a given stock status (`AVAILABLE`).
- **StockMovement**: Log record representing inventory transactions (`TRANSFER_OUT`, `TRANSFER_IN`) with reference number `TRF-YYYYMMDD-{from_location_code}->{to_location_code}`, product, lot, quantity, and location details.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Successful transfer updates source and destination balances accurately in 100% of valid transactions.
- **SC-002**: 100% of completed stock transfers generate corresponding paired `TRANSFER_OUT` and `TRANSFER_IN` movement logs with reference number formatted as `TRF-YYYYMMDD-{from_location_code}->{to_location_code}`.
- **SC-003**: Invalid requests (insufficient stock, non-positive quantity, non-AVAILABLE status, identical `fromLocationId == toLocationId`) are blocked with descriptive 400/409 error responses.

## Assumptions

- Stock transfer operation applies specifically to stock in `AVAILABLE` status.
- Reference number for both `TRANSFER_OUT` and `TRANSFER_IN` movements is generated as `TRF-YYYYMMDD-{from_location_code}->{to_location_code}` using current date and source/destination location codes.
- Destination stock balance record will be automatically created if it does not already exist at the destination warehouse location.
