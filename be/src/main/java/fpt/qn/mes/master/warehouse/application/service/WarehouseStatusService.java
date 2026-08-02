package fpt.qn.mes.master.warehouse.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fpt.qn.mes.common.dto.response.PageResponse;
import fpt.qn.mes.common.util.PaginationUtils;
import fpt.qn.mes.master.warehouse.application.dto.warehousestatus.WarehouseStatusResponse;
import fpt.qn.mes.master.warehouse.application.dto.warehousestatus.create.CreateWarehouseStatusRequest;
import fpt.qn.mes.master.warehouse.application.dto.warehousestatus.search.WarehouseStatusSearchRequest;
import fpt.qn.mes.master.warehouse.application.exception.WarehouseStatusConflictException;
import fpt.qn.mes.master.warehouse.application.mapper.WarehouseStatusDtoMapper;
import fpt.qn.mes.master.warehouse.application.port.in.WarehouseStatusUseCase;
import fpt.qn.mes.master.warehouse.domain.entities.WarehouseStatus;
import fpt.qn.mes.master.warehouse.domain.repository.WarehouseStatusRepository;
import fpt.qn.mes.master.warehouse.domain.repository.criteria.WarehouseStatusSearchCriteria;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class WarehouseStatusService implements WarehouseStatusUseCase {

    WarehouseStatusRepository warehousestatusRepository;
    WarehouseStatusDtoMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<WarehouseStatusResponse> getWarehouseStatuses(WarehouseStatusSearchRequest request) {
        var criteria = WarehouseStatusSearchCriteria.builder()
                .name(request.getName())
                .page(request.getPage())
                .size(request.getSize())
                .sort(request.getSort())
                .build();
        var result = warehousestatusRepository.search(criteria);
        var items = result.getItems().stream().map(s -> mapper.toDto(s)).toList();
        return PageResponse.<WarehouseStatusResponse>builder()
                .items(items).totalElements(result.getTotal())
                .pageNumber(request.getPage()).pageSize(request.getSize())
                .totalPages(PaginationUtils.calculateTotalPages(result.getTotal(), request.getSize()))
                .build();
    }

    @Override
    @Transactional
    public void createWarehouseStatus(CreateWarehouseStatusRequest request) {
        if (warehousestatusRepository.existsByName(request.getName())) {
            throw new WarehouseStatusConflictException("Name already exists: " + request.getName());
        }
        var status = WarehouseStatus.create(request.getName(), request.getDescription());
        warehousestatusRepository.save(status);
    }
}
