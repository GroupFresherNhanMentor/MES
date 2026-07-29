package fpt.qn.mes.inventory.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import fpt.qn.mes.inventory.domain.entities.StockLot;
import fpt.qn.mes.inventory.domain.repository.criteria.StockLotSearchCriteria;

public interface StockLotRepository {
    Optional<StockLot> findById(UUID id);

    Optional<StockLot> findByLotNumber(String lotNumber);

    StockLot save(StockLot lot);

    List<StockLot> search(StockLotSearchCriteria criteria);

    long count(StockLotSearchCriteria criteria);
}
