package fpt.qn.mes.master.warehouse.application.port.in;

import java.util.Collection;
import java.util.Map;
import java.util.List;
import java.util.UUID;
import fpt.qn.mes.common.dto.response.PageResponse;
import fpt.qn.mes.master.warehouse.application.dto.warehouse.WarehouseManagerResponse;
import fpt.qn.mes.master.warehouse.application.dto.warehouse.WarehouseResponse;
import fpt.qn.mes.master.warehouse.application.dto.warehouse.assign.AssignManagerRequest;
import fpt.qn.mes.master.warehouse.application.dto.warehouse.create.CreateWarehouseRequest;
import fpt.qn.mes.master.warehouse.application.dto.warehouse.search.WarehouseSearchRequest;
import fpt.qn.mes.master.warehouse.application.dto.warehouse.update.UpdateWarehouseRequest;

public interface WarehouseUseCase {
<<<<<<< HEAD
    PageResponse<WarehouseDto> getWarehouses(int page, int size);
    PageResponse<WarehouseDto> getWarehousesByStatus(int page, int size, UUID statusId);
    WarehouseDto getWarehouseById(UUID id);
    WarehouseDto getWarehouseByCode(String code);
    Map<UUID, WarehouseDto> getWarehousesByIds(Collection<UUID> ids);
    WarehouseDto createWarehouse(CreateWarehouseRequest request, UUID currentUserId);
    WarehouseDto updateWarehouse(UUID id, UpdateWarehouseRequest request, UUID currentUserId);
    void deleteWarehouse(UUID id);
=======
    PageResponse<WarehouseResponse> getWarehouses(WarehouseSearchRequest request);
    WarehouseResponse getWarehouseById(UUID id);
    WarehouseResponse getWarehouseByCode(String code);
    Map<UUID, WarehouseResponse> getWarehousesByIds(Collection<UUID> ids);
    void createWarehouse(CreateWarehouseRequest request);
    void updateWarehouse(UUID id, UpdateWarehouseRequest request);
    void activateWarehouse(UUID id);
    void deactivateWarehouse(UUID id);

    void assignManager(UUID warehouseId, AssignManagerRequest request);
    void removeManager(UUID warehouseId, UUID userId);
    List<WarehouseManagerResponse> getManagers(UUID warehouseId);
>>>>>>> origin/develop
}
