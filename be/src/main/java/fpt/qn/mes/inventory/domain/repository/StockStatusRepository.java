package fpt.qn.mes.inventory.domain.repository;

import java.util.Optional;
import java.util.UUID;

import fpt.qn.mes.inventory.domain.constants.StockStatusConstants;

public interface StockStatusRepository {

    /**
     * Find stock status ID by its unique name string.
     * <p>
     * <b>Usage Hint:</b> Pass predefined string constants from {@link StockStatusConstants}.<br>
     * Example: <code>stockStatusRepository.findIdByName(StockStatusConstants.AVAILABLE)</code>
     *
     * @param name Stock status name constant (e.g. {@link StockStatusConstants#AVAILABLE})
     * @return Optional containing the UUID if found
     * @see StockStatusConstants
     */
    Optional<UUID> findIdByName(String name);
}
