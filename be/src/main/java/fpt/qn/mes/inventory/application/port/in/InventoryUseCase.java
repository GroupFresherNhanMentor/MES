package fpt.qn.mes.inventory.application.port.in;

import java.util.UUID;

import fpt.qn.mes.common.dto.response.PageResponse;


import fpt.qn.mes.inventory.application.dto.stockadjustmentapproval.StockAdjustmentApprovalResponse;
import fpt.qn.mes.inventory.application.dto.stockadjustmentapproval.search.StockAdjustmentApprovalSearchRequest;
import fpt.qn.mes.inventory.application.dto.stockmovement.create.CreateStockMovementRequest;
import fpt.qn.mes.inventory.application.dto.stockmovement.create.StockInRequest;
import fpt.qn.mes.inventory.application.dto.stockadjustment.create.CreateStockAdjustmentRequest;
import fpt.qn.mes.inventory.application.dto.stockmovement.StockMovementResponse;
import fpt.qn.mes.inventory.application.dto.stockbalance.search.StockBalanceSearchRequest;
import fpt.qn.mes.inventory.application.dto.stockmovement.search.StockMovementSearchRequest;
import fpt.qn.mes.inventory.application.dto.stockbalance.StockBalanceResponse;

import fpt.qn.mes.inventory.application.dto.stockmovement.create.StockTransferRequest;
import fpt.qn.mes.inventory.application.dto.response.StockTransferResponse;

public interface InventoryUseCase {
    PageResponse<StockMovementResponse> getMovements(StockMovementSearchRequest request);
    void recordMovement(CreateStockMovementRequest request);
    PageResponse<StockBalanceResponse> getStockBalances(StockBalanceSearchRequest request);
    void recordStockIn(StockInRequest request);
    void adjustStock(CreateStockAdjustmentRequest request);
    PageResponse<StockAdjustmentApprovalResponse> getPendingAdjustments(StockAdjustmentApprovalSearchRequest request);
    StockMovementResponse approveAdjustment(UUID approvalId);
    void rejectAdjustment(UUID approvalId);
    StockTransferResponse transferStock(StockTransferRequest request);
}
