package fpt.qn.mes.inventory.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import fpt.qn.mes.inventory.domain.entities.StockMovement;
import fpt.qn.mes.inventory.domain.repository.criteria.StockMovementSearchCriteria;

public interface StockMovementRepository {
    StockMovement save(StockMovement movement);
    Optional<StockMovement> findById(UUID id);
    List<StockMovement> search(StockMovementSearchCriteria criteria);
    long count(StockMovementSearchCriteria criteria);
}
