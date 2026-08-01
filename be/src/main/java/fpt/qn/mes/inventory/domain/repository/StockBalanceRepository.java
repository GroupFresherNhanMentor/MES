package fpt.qn.mes.inventory.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import fpt.qn.mes.common.domainQuery.PaginationResult;
import fpt.qn.mes.inventory.domain.entities.StockBalance;
import fpt.qn.mes.inventory.domain.repository.criteria.StockBalanceSearchCriteria;

public interface StockBalanceRepository {
    Optional<StockBalance> findById(UUID id);
    Optional<StockBalance> findForUpdate(UUID warehouseId, UUID locationId, UUID productId,
            UUID lotId, UUID stockStatusId);
    StockBalance save(StockBalance balance);
    List<StockBalance> findByWarehouseAndProduct(UUID warehouseId, UUID productId);
    PaginationResult<StockBalance> search(StockBalanceSearchCriteria criteria);
}
