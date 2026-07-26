package fpt.qn.mes.master.location.application.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fpt.qn.mes.master.location.application.dto.CreateLocationRequest;
import fpt.qn.mes.master.location.application.dto.UpdateLocationRequest;
import fpt.qn.mes.master.location.application.dto.WarehouseLocationDto;
import fpt.qn.mes.master.location.application.mapper.LocationDtoMapper;
import fpt.qn.mes.master.location.application.port.in.LocationUseCase;
import fpt.qn.mes.master.location.domain.repository.WarehouseLocationRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class LocationService implements LocationUseCase {

    WarehouseLocationRepository locationRepository;
    LocationDtoMapper mapper;

    @Override @Transactional(readOnly = true)
    public List<WarehouseLocationDto> getLocations(UUID warehouseId) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override @Transactional(readOnly = true)
    public WarehouseLocationDto getLocationById(UUID id) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override @Transactional
    public WarehouseLocationDto createLocation(UUID warehouseId, CreateLocationRequest request, UUID currentUserId) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override @Transactional
    public WarehouseLocationDto updateLocation(UUID id, UpdateLocationRequest request, UUID currentUserId) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override @Transactional
    public void deleteLocation(UUID id) {}
}
