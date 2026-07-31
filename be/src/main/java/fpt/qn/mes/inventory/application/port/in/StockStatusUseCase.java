package fpt.qn.mes.inventory.application.port.in;

import java.util.UUID;

import fpt.qn.mes.common.dto.response.PageResponse;
import fpt.qn.mes.inventory.application.dto.stockstatus.StockStatusResponse;
import fpt.qn.mes.inventory.application.dto.stockstatus.create.CreateStockStatusRequest;
import fpt.qn.mes.inventory.application.dto.stockstatus.search.StockStatusSearchRequest;

public interface StockStatusUseCase {
    PageResponse<StockStatusResponse> getStockStatuses(StockStatusSearchRequest request);
    void createStockStatus(CreateStockStatusRequest request);
    UUID getStockStatusIdByName(String name);
}
