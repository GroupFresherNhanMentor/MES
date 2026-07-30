package fpt.qn.mes.master.location.application.port.in;

import java.util.UUID;
import fpt.qn.mes.common.dto.response.PageResponse;
import fpt.qn.mes.master.location.application.dto.warehouselocation.WarehouseLocationResponse;
import fpt.qn.mes.master.location.application.dto.warehouselocation.create.CreateWarehouseLocationRequest;
import fpt.qn.mes.master.location.application.dto.warehouselocation.search.WarehouseLocationSearchRequest;
import fpt.qn.mes.master.location.application.dto.warehouselocation.update.UpdateWarehouseLocationRequest;

public interface WarehouseLocationUseCase {
    PageResponse<WarehouseLocationResponse> getWarehouseLocations(UUID warehouseId, WarehouseLocationSearchRequest request);
    WarehouseLocationResponse getWarehouseLocationById(UUID id);
    void createWarehouseLocation(UUID warehouseId, CreateWarehouseLocationRequest request);
    void updateWarehouseLocation(UUID id, UpdateWarehouseLocationRequest request);
    void activateWarehouseLocation(UUID id);
    void deactivateWarehouseLocation(UUID id);
}
