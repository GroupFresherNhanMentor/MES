package fpt.qn.mes.inventory.application.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fpt.qn.mes.common.dto.response.PageResponse;
import fpt.qn.mes.inventory.application.dto.request.CreateMovementRequest;
import fpt.qn.mes.inventory.application.dto.request.CreateStockLotRequest;
import fpt.qn.mes.inventory.application.dto.response.StockBalanceDto;
import fpt.qn.mes.inventory.application.dto.response.StockLotDto;
import fpt.qn.mes.inventory.application.dto.response.StockMovementDto;
import fpt.qn.mes.inventory.application.mapper.InventoryDtoMapper;
import fpt.qn.mes.inventory.application.port.in.InventoryUseCase;
import fpt.qn.mes.inventory.domain.repository.StockBalanceRepository;
import fpt.qn.mes.inventory.domain.repository.StockLotRepository;
import fpt.qn.mes.inventory.domain.repository.StockMovementRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class InventoryService implements InventoryUseCase {

    StockLotRepository lotRepository;
    StockMovementRepository movementRepository;
    StockBalanceRepository balanceRepository;
    InventoryDtoMapper mapper;

    @Override @Transactional(readOnly = true)
    public PageResponse<StockLotDto> getStockLots(int page, int size) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override @Transactional(readOnly = true)
    public StockLotDto getStockLotById(UUID id) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override @Transactional
    public StockLotDto createStockLot(CreateStockLotRequest request) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override @Transactional(readOnly = true)
    public PageResponse<StockMovementDto> getMovements(int page, int size) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override @Transactional
    public StockMovementDto recordMovement(CreateMovementRequest request, UUID currentUserId) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override @Transactional(readOnly = true)
    public List<StockBalanceDto> getStockBalances(UUID warehouseId, UUID productId) {
        throw new UnsupportedOperationException("Not implemented");
    }
}
