package fpt.qn.mes.inventory.application.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fpt.qn.mes.common.dto.response.PageResponse;
import fpt.qn.mes.common.util.PaginationUtils;
import fpt.qn.mes.inventory.application.dto.stockstatus.StockStatusResponse;
import fpt.qn.mes.inventory.application.dto.stockstatus.create.CreateStockStatusRequest;
import fpt.qn.mes.inventory.application.dto.stockstatus.search.StockStatusSearchRequest;
import fpt.qn.mes.auth.application.port.out.CurrentUserPort;
import fpt.qn.mes.inventory.application.exception.StockStatusConflictException;
import fpt.qn.mes.inventory.application.mapper.StockStatusDtoMapper;
import fpt.qn.mes.inventory.application.port.in.StockStatusUseCase;
import fpt.qn.mes.inventory.domain.entities.StockStatus;
import fpt.qn.mes.inventory.domain.repository.StockStatusRepository;
import fpt.qn.mes.inventory.domain.repository.criteria.StockStatusSearchCriteria;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class StockStatusService implements StockStatusUseCase {

    StockStatusRepository stockStatusRepository;
    StockStatusDtoMapper mapper;
    CurrentUserPort currentUserPort;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<StockStatusResponse> getStockStatuses(StockStatusSearchRequest request) {
        var criteria = StockStatusSearchCriteria.builder()
                .query(request.getQuery())
                .name(request.getName())
                .page(request.getPage())
                .size(request.getSize())
                .sort(request.getSort())
                .build();
        var result = stockStatusRepository.search(criteria);
        var items = result.getItems().stream().map(ss -> mapper.toDto(ss)).toList();
        return PageResponse.<StockStatusResponse>builder()
                .items(items).totalElements(result.getTotal())
                .pageNumber(request.getPage()).pageSize(request.getSize())
                .totalPages(PaginationUtils.calculateTotalPages(result.getTotal(), request.getSize()))
                .build();
    }

    @Override
    @Transactional
    public void createStockStatus(CreateStockStatusRequest request) {
        if (stockStatusRepository.existsByName(request.getName())) {
            throw new StockStatusConflictException("StockStatus with name already exists: " + request.getName());
        }
        stockStatusRepository.save(StockStatus.create(request.getName(), request.getDescription(), currentUserPort.getCurrentUserId()));
    }

    @Override
    public UUID getStockStatusIdByName(String name) {
        return stockStatusRepository.findIdByName(name).orElse(null);
    }
}
