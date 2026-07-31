package fpt.qn.mes.inventory.application.port.in;

import java.util.UUID;

import fpt.qn.mes.common.dto.response.PageResponse;
import fpt.qn.mes.inventory.application.dto.stocklot.StockLotResponse;
import fpt.qn.mes.inventory.application.dto.stocklot.create.CreateStockLotRequest;
import fpt.qn.mes.inventory.application.dto.stocklot.search.StockLotSearchRequest;

public interface StockLotUseCase {
    PageResponse<StockLotResponse> getStockLots(StockLotSearchRequest request);
    StockLotResponse getStockLotById(UUID id);
    void createStockLot(CreateStockLotRequest request);
}
