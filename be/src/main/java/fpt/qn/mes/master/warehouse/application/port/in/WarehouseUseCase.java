package fpt.qn.mes.master.warehouse.application.port.in;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

import fpt.qn.mes.common.dto.response.PageResponse;
import fpt.qn.mes.master.warehouse.application.dto.request.CreateWarehouseRequest;
import fpt.qn.mes.master.warehouse.application.dto.request.UpdateWarehouseRequest;
import fpt.qn.mes.master.warehouse.application.dto.response.WarehouseDto;

public interface WarehouseUseCase {
    PageResponse<WarehouseDto> getWarehouses(int page, int size);
    PageResponse<WarehouseDto> getWarehousesByStatus(int page, int size, UUID statusId);
    WarehouseDto getWarehouseById(UUID id);
    Map<UUID, WarehouseDto> getWarehousesByIds(Collection<UUID> ids);
    WarehouseDto createWarehouse(CreateWarehouseRequest request, UUID currentUserId);
    WarehouseDto updateWarehouse(UUID id, UpdateWarehouseRequest request, UUID currentUserId);
    void deleteWarehouse(UUID id);
}
