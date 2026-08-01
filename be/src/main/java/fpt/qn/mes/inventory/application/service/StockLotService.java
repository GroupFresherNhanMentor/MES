package fpt.qn.mes.inventory.application.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fpt.qn.mes.common.dto.response.PageResponse;
import fpt.qn.mes.common.util.PaginationUtils;
import fpt.qn.mes.inventory.application.dto.stocklot.StockLotResponse;
import fpt.qn.mes.inventory.application.dto.stocklot.create.CreateStockLotRequest;
import fpt.qn.mes.inventory.application.dto.stocklot.search.StockLotSearchRequest;
import fpt.qn.mes.auth.application.port.out.CurrentUserPort;
import fpt.qn.mes.inventory.application.exception.InventoryNotFoundException;
import fpt.qn.mes.inventory.application.exception.LotTypeNotFoundException;
import fpt.qn.mes.inventory.application.exception.StockLotConflictException;
import fpt.qn.mes.inventory.application.exception.StockLotNotFoundException;
import fpt.qn.mes.inventory.application.mapper.StockLotDtoMapper;
import fpt.qn.mes.inventory.application.port.in.StockLotUseCase;
import fpt.qn.mes.inventory.domain.entities.StockLot;
import fpt.qn.mes.inventory.application.port.out.ProductCheckPort;
import fpt.qn.mes.inventory.domain.repository.LotTypeRepository;
import fpt.qn.mes.inventory.domain.repository.StockLotRepository;
import fpt.qn.mes.inventory.domain.repository.criteria.StockLotSearchCriteria;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class StockLotService implements StockLotUseCase {

    StockLotRepository stockLotRepository;
    LotTypeRepository lotTypeRepository;
    ProductCheckPort productCheckPort;
    StockLotDtoMapper mapper;
    CurrentUserPort currentUserPort;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<StockLotResponse> getStockLots(StockLotSearchRequest request) {
        StockLotSearchCriteria criteria = StockLotSearchCriteria.builder()
                .productId(request.getProductId())
                .lotTypeId(request.getLotTypeId())
                .lotNumber(request.getLotNumber())
                .page(request.getPage())
                .size(request.getSize())
                .sort(request.getSort())
                .build();

        var result = stockLotRepository.search(criteria);
        var items = result.getItems().stream().map(lot -> mapper.toDto(lot)).toList();
        return PageResponse.<StockLotResponse>builder()
                .items(items)
                .totalElements(result.getTotal())
                .pageNumber(request.getPage())
                .pageSize(request.getSize())
                .totalPages(PaginationUtils.calculateTotalPages(result.getTotal(), request.getSize()))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public StockLotResponse getStockLotById(UUID id) {
        return stockLotRepository.findById(id)
                .map(lot -> mapper.toDto(lot))
                .orElseThrow(() -> new StockLotNotFoundException("Stock lot not found: " + id));
    }

    @Override
    @Transactional
    public void createStockLot(CreateStockLotRequest request) {
        if (!productCheckPort.existsById(request.getProductId())) {
            throw new InventoryNotFoundException("Product not found: " + request.getProductId());
        }
        if (request.getLotTypeId() != null && !lotTypeRepository.existsById(request.getLotTypeId())) {
            throw new LotTypeNotFoundException("Lot type not found: " + request.getLotTypeId());
        }
        var existing = stockLotRepository.findByLotNumber(request.getLotNumber());
        if (existing.isPresent()) {
            throw new StockLotConflictException(
                    "Stock lot '" + request.getLotNumber() + "' already exists");
        }
        stockLotRepository.save(StockLot.create(
                request.getLotNumber(),
                request.getProductId(),
                request.getLotTypeId(),
                request.getExpiryDate(),
                currentUserPort.getCurrentUserId()
        ));
    }
}
