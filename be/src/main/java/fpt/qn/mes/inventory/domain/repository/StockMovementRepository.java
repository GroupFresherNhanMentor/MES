package fpt.qn.mes.inventory.domain.repository;

import java.util.UUID;

import fpt.qn.mes.common.dto.PaginationResult;
import fpt.qn.mes.inventory.domain.entities.StockMovement;

public interface StockMovementRepository {
    StockMovement save(StockMovement movement);
    PaginationResult<StockMovement> findAll(int page, int size);
}
