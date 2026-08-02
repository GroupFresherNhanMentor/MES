package fpt.qn.mes.inventory.domain.repository;

import java.util.Optional;
import java.util.UUID;

import fpt.qn.mes.common.domainQuery.PaginationResult;
import fpt.qn.mes.inventory.domain.entities.StockLot;
import fpt.qn.mes.inventory.domain.repository.criteria.StockLotSearchCriteria;

public interface StockLotRepository {
    Optional<StockLot> findById(UUID id);
    Optional<StockLot> findByLotNumber(String lotNumber);
    boolean existsByLotNumber(String lotNumber);
    StockLot save(StockLot lot);
    PaginationResult<StockLot> search(StockLotSearchCriteria criteria);
}
