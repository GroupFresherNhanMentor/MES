package fpt.qn.mes.inventory.domain.repository;

import java.util.List;
import fpt.qn.mes.common.dto.response.PaginationResult;
import fpt.qn.mes.inventory.domain.entities.StockMovement;
import fpt.qn.mes.inventory.domain.repository.criteria.StockMovementSearchCriteria;

public interface StockMovementRepository {
    StockMovement save(StockMovement movement);

    List<StockMovement> search(StockMovementSearchCriteria criteria);
}
