package fpt.qn.mes.inventory.domain.repository;

import java.util.Optional;
import java.util.UUID;

import fpt.qn.mes.common.dto.PaginationResult;
import fpt.qn.mes.inventory.domain.entities.StockLot;

public interface StockLotRepository {
    Optional<StockLot> findById(UUID id);
    StockLot save(StockLot lot);
    PaginationResult<StockLot> findAll(int page, int size);
}
