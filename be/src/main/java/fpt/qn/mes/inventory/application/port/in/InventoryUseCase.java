package fpt.qn.mes.inventory.application.port.in;

import java.util.UUID;

import fpt.qn.mes.common.dto.response.PageResponse;


import fpt.qn.mes.inventory.application.dto.stockadjustmentapproval.StockAdjustmentApprovalResponse;
import fpt.qn.mes.inventory.domain.repository.criteria.StockAdjustmentApprovalSearchCriteria;
import fpt.qn.mes.inventory.application.dto.stockmovement.create.CreateStockMovementRequest;
import fpt.qn.mes.inventory.application.dto.stockmovement.create.StockInRequest;
import fpt.qn.mes.inventory.application.dto.stockadjustment.create.CreateStockAdjustmentRequest;
import fpt.qn.mes.inventory.application.dto.stockmovement.StockMovementResponse;
import fpt.qn.mes.inventory.application.dto.stockbalance.search.StockBalanceSearchRequest;
import fpt.qn.mes.inventory.application.dto.stocklot.create.CreateStockLotRequest;
import fpt.qn.mes.inventory.application.dto.stockmovement.search.StockMovementSearchRequest;
import fpt.qn.mes.inventory.application.dto.stocklot.search.StockLotSearchRequest;
import fpt.qn.mes.inventory.application.dto.stockbalance.StockBalanceResponse;
import fpt.qn.mes.inventory.application.dto.stocklot.StockLotResponse;


import fpt.qn.mes.inventory.application.dto.stockmovement.create.StockTransferRequest;
import fpt.qn.mes.inventory.application.dto.response.StockTransferResponse;

public interface InventoryUseCase {
    PageResponse<StockLotResponse> getStockLots(StockLotSearchRequest request);
    StockLotResponse getStockLotById(UUID id);
    void createStockLot(CreateStockLotRequest request);
    PageResponse<StockMovementResponse> getMovements(StockMovementSearchRequest request);
    void recordMovement(CreateStockMovementRequest request, UUID currentUserId);
    PageResponse<StockBalanceResponse> getStockBalances(StockBalanceSearchRequest request);
    void recordStockIn(StockInRequest request, UUID currentUserId);
    void adjustStock(CreateStockAdjustmentRequest request, UUID currentUserId);
    PageResponse<StockAdjustmentApprovalResponse> getPendingAdjustments(StockAdjustmentApprovalSearchCriteria criteria);
    StockMovementResponse approveAdjustment(UUID approvalId, UUID currentUserId);
    void rejectAdjustment(UUID approvalId, UUID currentUserId);
    StockTransferResponse transferStock(StockTransferRequest request, UUID currentUserId);
}
