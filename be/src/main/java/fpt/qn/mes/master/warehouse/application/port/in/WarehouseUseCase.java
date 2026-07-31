package fpt.qn.mes.master.warehouse.application.port.in;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import fpt.qn.mes.common.dto.response.PageResponse;
import fpt.qn.mes.master.warehouse.application.dto.warehouse.WarehouseResponse;
import fpt.qn.mes.master.warehouse.application.dto.warehouse.create.CreateWarehouseRequest;
import fpt.qn.mes.master.warehouse.application.dto.warehouse.search.WarehouseSearchRequest;
import fpt.qn.mes.master.warehouse.application.dto.warehouse.update.UpdateWarehouseRequest;

public interface WarehouseUseCase {
    PageResponse<WarehouseResponse> getWarehouses(WarehouseSearchRequest request);
    WarehouseResponse getWarehouseById(UUID id);
    WarehouseResponse getWarehouseByCode(String code);
    Map<UUID, WarehouseResponse> getWarehousesByIds(Collection<UUID> ids);
    void createWarehouse(CreateWarehouseRequest request);
    void updateWarehouse(UUID id, UpdateWarehouseRequest request);
    void activateWarehouse(UUID id);
    void deactivateWarehouse(UUID id);
}
