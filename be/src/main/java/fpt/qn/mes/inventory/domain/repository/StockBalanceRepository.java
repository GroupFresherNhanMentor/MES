package fpt.qn.mes.inventory.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import fpt.qn.mes.common.dto.response.PageResponse;
import fpt.qn.mes.inventory.domain.entities.StockBalance;

public interface StockBalanceRepository {
    Optional<StockBalance> findForUpdate(UUID warehouseId, UUID locationId, UUID productId,
            UUID lotId);

    StockBalance save(StockBalance balance);

    List<StockBalance> findByWarehouseAndProduct(UUID warehouseId, UUID productId);

    PageResponse<StockBalance> search(StockBalanceSearchCriteria criteria);
}
