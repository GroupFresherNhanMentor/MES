package fpt.qn.mes.inventory.application.port.in;

import java.util.List;
import java.util.UUID;

import fpt.qn.mes.common.dto.response.PageResponse;
import fpt.qn.mes.inventory.application.dto.request.CreateMovementRequest;
import fpt.qn.mes.inventory.application.dto.request.CreateStockLotRequest;
import fpt.qn.mes.inventory.application.dto.response.StockBalanceDto;
import fpt.qn.mes.inventory.application.dto.response.StockLotDto;
import fpt.qn.mes.inventory.application.dto.response.StockMovementDto;

import fpt.qn.mes.inventory.application.dto.request.StockBalanceSearchRequest;
import fpt.qn.mes.inventory.application.dto.request.StockInRequest;
import fpt.qn.mes.inventory.application.dto.request.StockLotSearchRequest;

public interface InventoryUseCase {
    PageResponse<StockLotDto> getStockLots(StockLotSearchRequest request);
    StockLotDto getStockLotById(UUID id);
    StockLotDto createStockLot(CreateStockLotRequest request);
    PageResponse<StockMovementDto> getMovements(int page, int size);
    StockMovementDto recordMovement(CreateMovementRequest request, UUID currentUserId);
    PageResponse<StockBalanceDto> getStockBalances(StockBalanceSearchRequest request);
    StockMovementDto recordStockIn(StockInRequest request, UUID currentUserId);
}
