package fpt.qn.mes.inventory.domain.constants;

/**
 * Predefined stock status name constants matching database seed data.
 * <p>
 * <b>Usage Example:</b>
 * <pre>{@code
 * UUID availableStatusId = stockStatusRepository.findIdByName(StockStatusConstants.AVAILABLE)
 *         .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "AVAILABLE status not found"));
 * }</pre>
 */
public final class StockStatusConstants {

    public static final String AVAILABLE = "AVAILABLE";
    public static final String RESERVED = "RESERVED";
    public static final String ON_HOLD = "ON_HOLD";
    public static final String QUALITY_INSPECTION = "QUALITY_INSPECTION";
    public static final String DAMAGED = "DAMAGED";
    public static final String SCRAPPED = "SCRAPPED";
    public static final String CONSUMED = "CONSUMED";
    public static final String SHIPPED = "SHIPPED";

    private StockStatusConstants() {
        // Prevent instantiation
    }
}
