package fpt.qn.mes.inventory.domain.repository;

import java.util.Optional;
import java.util.UUID;

import fpt.qn.mes.common.dto.response.PageResponse;
import fpt.qn.mes.inventory.domain.entities.StockLot;
import fpt.qn.mes.inventory.domain.repository.criteria.StockLotSearchCriteria;

public interface StockLotRepository {
    Optional<StockLot> findById(UUID id);

    StockLot save(StockLot lot);

    PageResponse<StockLot> search(StockLotSearchCriteria criteria);
}
