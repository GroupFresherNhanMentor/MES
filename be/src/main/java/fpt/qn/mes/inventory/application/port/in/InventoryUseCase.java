package fpt.qn.mes.inventory.application.port.in;

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
import fpt.qn.mes.inventory.application.dto.request.StockMovementSearchRequest;

import fpt.qn.mes.inventory.application.dto.request.StockAdjustmentRequest;
import fpt.qn.mes.inventory.application.dto.response.StockAdjustmentApprovalDto;
import fpt.qn.mes.inventory.application.dto.response.StockAdjustmentResponse;
import fpt.qn.mes.inventory.domain.repository.criteria.StockAdjustmentApprovalSearchCriteria;

import fpt.qn.mes.inventory.application.dto.request.LotTypeSearchRequest;
import fpt.qn.mes.inventory.application.dto.request.MovementTypeSearchRequest;
import fpt.qn.mes.inventory.application.dto.request.StockStatusSearchRequest;
import fpt.qn.mes.inventory.application.dto.response.LotTypeSummaryDto;
import fpt.qn.mes.inventory.application.dto.response.MovementTypeSummaryDto;
import fpt.qn.mes.inventory.application.dto.response.StockStatusSummaryDto;

import fpt.qn.mes.inventory.application.dto.request.StockTransferRequest;
import fpt.qn.mes.inventory.application.dto.response.StockTransferResponse;

public interface InventoryUseCase {
    PageResponse<StockLotDto> getStockLots(StockLotSearchRequest request);
    StockLotDto getStockLotById(UUID id);
    StockLotDto createStockLot(CreateStockLotRequest request);
    PageResponse<StockMovementDto> getMovements(StockMovementSearchRequest request);
    StockMovementDto recordMovement(CreateMovementRequest request, UUID currentUserId);
    PageResponse<StockBalanceDto> getStockBalances(StockBalanceSearchRequest request);
    StockMovementDto recordStockIn(StockInRequest request, UUID currentUserId);
    StockAdjustmentResponse adjustStock(StockAdjustmentRequest request, UUID currentUserId);
    PageResponse<StockAdjustmentApprovalDto> getPendingAdjustments(StockAdjustmentApprovalSearchCriteria criteria);
    StockMovementDto approveAdjustment(UUID approvalId, UUID currentUserId);
    void rejectAdjustment(UUID approvalId, UUID currentUserId);
    PageResponse<LotTypeSummaryDto> getLotTypes(LotTypeSearchRequest request);
    PageResponse<StockStatusSummaryDto> getStockStatuses(StockStatusSearchRequest request);
    PageResponse<MovementTypeSummaryDto> getMovementTypes(MovementTypeSearchRequest request);
    StockTransferResponse transferStock(StockTransferRequest request, UUID currentUserId);
}
