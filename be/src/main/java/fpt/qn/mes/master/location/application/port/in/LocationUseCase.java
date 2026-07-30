package fpt.qn.mes.master.location.application.port.in;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import fpt.qn.mes.master.location.application.dto.request.CreateLocationRequest;
import fpt.qn.mes.master.location.application.dto.request.UpdateLocationRequest;
import fpt.qn.mes.master.location.application.dto.response.WarehouseLocationDto;

public interface LocationUseCase {
    List<WarehouseLocationDto> getLocations(UUID warehouseId);
    WarehouseLocationDto getLocationById(UUID id);
    Map<UUID, WarehouseLocationDto> getLocationsByIds(Collection<UUID> ids);
    WarehouseLocationDto createLocation(UUID warehouseId, CreateLocationRequest request, UUID currentUserId);
    WarehouseLocationDto updateLocation(UUID id, UpdateLocationRequest request, UUID currentUserId);
    void deleteLocation(UUID id);
}
