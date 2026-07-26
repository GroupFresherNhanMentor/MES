package fpt.qn.mes.master.location.application.port.in;

import java.util.List;
import java.util.UUID;

import fpt.qn.mes.master.location.application.dto.CreateLocationRequest;
import fpt.qn.mes.master.location.application.dto.UpdateLocationRequest;
import fpt.qn.mes.master.location.application.dto.WarehouseLocationDto;

public interface LocationUseCase {
    List<WarehouseLocationDto> getLocations(UUID warehouseId);
    WarehouseLocationDto getLocationById(UUID id);
    WarehouseLocationDto createLocation(UUID warehouseId, CreateLocationRequest request, UUID currentUserId);
    WarehouseLocationDto updateLocation(UUID id, UpdateLocationRequest request, UUID currentUserId);
    void deleteLocation(UUID id);
}
