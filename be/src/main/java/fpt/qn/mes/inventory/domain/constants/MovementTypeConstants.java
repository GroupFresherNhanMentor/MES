package fpt.qn.mes.inventory.domain.constants;

/**
 * Predefined movement type name constants matching database seed data.
 * <p>
 * <b>Usage Example:</b>
 * <pre>{@code
 * UUID qcReleaseTypeId = movementTypeRepository.findIdByName(MovementTypeConstants.QC_RELEASE)
 *         .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "QC_RELEASE type not found"));
 * }</pre>
 */
public final class MovementTypeConstants {

    public static final String PURCHASE_IN = "PURCHASE_IN";
    public static final String TRANSFER_IN = "TRANSFER_IN";
    public static final String TRANSFER_OUT = "TRANSFER_OUT";
    public static final String RESERVE = "RESERVE";
    public static final String RELEASE_RESERVATION = "RELEASE_RESERVATION";
    public static final String ISSUE_TO_PRODUCTION = "ISSUE_TO_PRODUCTION";
    public static final String CONSUME_IN_PRODUCTION = "CONSUME_IN_PRODUCTION";
    public static final String PRODUCTION_OUTPUT = "PRODUCTION_OUTPUT";
    public static final String QC_HOLD = "QC_HOLD";
    public static final String QC_RELEASE = "QC_RELEASE";
    public static final String SCRAP = "SCRAP";
    public static final String ADJUSTMENT = "ADJUSTMENT";
    public static final String SHIP_OUT = "SHIP_OUT";
    public static final String RETURN_TO_WAREHOUSE = "RETURN_TO_WAREHOUSE";

    private MovementTypeConstants() {
        // Prevent instantiation
    }
}
