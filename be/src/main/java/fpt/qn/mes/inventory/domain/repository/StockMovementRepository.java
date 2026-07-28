package fpt.qn.mes.inventory.domain.repository;

import fpt.qn.mes.common.dto.response.PageResponse;
import fpt.qn.mes.inventory.domain.entities.StockMovement;
import fpt.qn.mes.inventory.domain.repository.criteria.StockMovementSearchCriteria;

public interface StockMovementRepository {
    StockMovement save(StockMovement movement);

    PageResponse<StockMovement> search(StockMovementSearchCriteria criteria);
}
