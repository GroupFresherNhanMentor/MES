package fpt.qn.mes.quality.application.port.out;

import java.math.BigDecimal;
import java.util.UUID;

public interface QcStockPort {
    /**
     * Deduct from QUALITY_INSPECTION balance and add to target status balance.
     * Returns the stock movement ID created.
     */
    UUID transferStock(UUID lotId, UUID productId, BigDecimal quantity, UUID fromStatusId, UUID toStatusId, UUID movementTypeId, UUID workOrderId, UUID inspectorId);

    /**
     * Get stock status ID for QUALITY_INSPECTION.
     */
    UUID getQualityInspectionStatusId();

    /**
     * Get stock status ID for AVAILABLE.
     */
    UUID getAvailableStatusId();

    /**
     * Get stock status ID for ON_HOLD.
     */
    UUID getOnHoldStatusId();

    /**
     * Get stock status ID for SCRAPPED.
     */
    UUID getScrappedStatusId();

    /**
     * Get movement type ID for QC_RELEASE.
     */
    UUID getQcReleaseMovementTypeId();

    /**
     * Get movement type ID for QC_HOLD.
     */
    UUID getQcHoldMovementTypeId();

    /**
     * Get movement type ID for SCRAP.
     */
    UUID getScrapMovementTypeId();
}
