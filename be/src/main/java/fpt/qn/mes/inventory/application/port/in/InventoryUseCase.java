package fpt.qn.mes.inventory.application.port.in;

import java.util.List;
import java.util.UUID;

import fpt.qn.mes.common.dto.response.PageResponse;
import fpt.qn.mes.inventory.application.dto.request.CreateMovementRequest;
import fpt.qn.mes.inventory.application.dto.request.CreateStockLotRequest;
import fpt.qn.mes.inventory.application.dto.response.StockBalanceDto;
import fpt.qn.mes.inventory.application.dto.response.StockLotDto;
import fpt.qn.mes.inventory.application.dto.response.StockMovementDto;

public interface InventoryUseCase {
    PageResponse<StockLotDto> getStockLots(int page, int size);
    StockLotDto getStockLotById(UUID id);
    StockLotDto createStockLot(CreateStockLotRequest request);
    PageResponse<StockMovementDto> getMovements(int page, int size);
    StockMovementDto recordMovement(CreateMovementRequest request, UUID currentUserId);
    List<StockBalanceDto> getStockBalances(UUID warehouseId, UUID productId);
}
