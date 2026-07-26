package fpt.qn.mes.master.warehouse.application.port.in;

import java.util.UUID;

import fpt.qn.mes.common.dto.PageResponse;
import fpt.qn.mes.master.warehouse.application.dto.CreateWarehouseRequest;
import fpt.qn.mes.master.warehouse.application.dto.UpdateWarehouseRequest;
import fpt.qn.mes.master.warehouse.application.dto.WarehouseDto;

public interface WarehouseUseCase {
    PageResponse<WarehouseDto> getWarehouses(int page, int size);
    WarehouseDto getWarehouseById(UUID id);
    WarehouseDto createWarehouse(CreateWarehouseRequest request, UUID currentUserId);
    WarehouseDto updateWarehouse(UUID id, UpdateWarehouseRequest request, UUID currentUserId);
    void deleteWarehouse(UUID id);
}
