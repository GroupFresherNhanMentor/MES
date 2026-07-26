package fpt.qn.mes.inventory.domain.repository;

import java.util.List;
import java.util.UUID;

import fpt.qn.mes.inventory.domain.entities.StockBalance;

public interface StockBalanceRepository {
    List<StockBalance> findByWarehouseAndProduct(UUID warehouseId, UUID productId);
}
