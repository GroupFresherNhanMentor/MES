package fpt.qn.mes.master.warehouse.application.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fpt.qn.mes.common.dto.response.PageResponse;
import fpt.qn.mes.master.warehouse.application.dto.request.CreateWarehouseRequest;
import fpt.qn.mes.master.warehouse.application.dto.request.UpdateWarehouseRequest;
import fpt.qn.mes.master.warehouse.application.dto.response.WarehouseDto;
import fpt.qn.mes.master.warehouse.application.mapper.WarehouseDtoMapper;
import fpt.qn.mes.master.warehouse.application.port.in.WarehouseUseCase;
import fpt.qn.mes.master.warehouse.domain.repository.WarehouseRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class WarehouseService implements WarehouseUseCase {

    WarehouseRepository warehouseRepository;
    WarehouseDtoMapper mapper;

    @Override @Transactional(readOnly = true)
    public PageResponse<WarehouseDto> getWarehouses(int page, int size) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override @Transactional(readOnly = true)
    public WarehouseDto getWarehouseById(UUID id) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override @Transactional
    public WarehouseDto createWarehouse(CreateWarehouseRequest request, UUID currentUserId) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override @Transactional
    public WarehouseDto updateWarehouse(UUID id, UpdateWarehouseRequest request, UUID currentUserId) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override @Transactional
    public void deleteWarehouse(UUID id) {}
}
