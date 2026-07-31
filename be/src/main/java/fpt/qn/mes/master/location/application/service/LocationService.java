package fpt.qn.mes.master.location.application.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.jooq.DSLContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fpt.qn.mes.master.location.application.dto.request.CreateLocationRequest;
import fpt.qn.mes.master.location.application.dto.request.UpdateLocationRequest;
import fpt.qn.mes.master.location.application.dto.response.WarehouseLocationDto;
import fpt.qn.mes.master.location.application.exception.LocationConflictException;
import fpt.qn.mes.master.location.application.exception.LocationNotFoundException;
import fpt.qn.mes.master.location.application.mapper.LocationDtoMapper;
import fpt.qn.mes.master.location.application.port.in.LocationUseCase;
import fpt.qn.mes.master.location.domain.entities.WarehouseLocation;
import fpt.qn.mes.master.location.domain.repository.WarehouseLocationRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import static fpt.qn.mes.jooq.Tables.LOCATION_STATUSES;
import static fpt.qn.mes.jooq.Tables.STOCK_BALANCES;
import static fpt.qn.mes.jooq.Tables.WAREHOUSES;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class LocationService implements LocationUseCase {

    WarehouseLocationRepository locationRepository;
    LocationDtoMapper mapper;
    DSLContext ctx;

    @Override
    @Transactional(readOnly = true)
    public List<WarehouseLocationDto> getLocations(UUID warehouseId) {
        return locationRepository.findByWarehouseId(warehouseId).stream().map(this::enrichDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public WarehouseLocationDto getLocationById(UUID id) {
        return locationRepository.findById(id)
                .map(this::enrichDto)
                .orElseThrow(() -> new LocationNotFoundException("Location not found: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public java.util.Map<UUID, WarehouseLocationDto> getLocationsByIds(java.util.Collection<UUID> ids) {
        if (ids == null || ids.isEmpty()) return java.util.Map.of();
        var locations = locationRepository.findByIds(ids);
        return locations.stream()
                .collect(java.util.stream.Collectors.toMap(WarehouseLocation::getId, mapper::toDto, (l1, l2) -> l1));
    }

    @Override
    @Transactional
    public WarehouseLocationDto createLocation(UUID warehouseId, CreateLocationRequest request, UUID currentUserId) {
        if (locationRepository.existsByWarehouseIdAndCode(warehouseId, request.getCode())) {
            throw new LocationConflictException("Location code already exists in this warehouse: " + request.getCode());
        }
        var location = WarehouseLocation.create(
                warehouseId, request.getCode(), request.getName(),
                request.getLocationStatusId(), currentUserId);
        return mapper.toDto(locationRepository.save(location));
    }

    @Override
    @Transactional
    public WarehouseLocationDto updateLocation(UUID id, UpdateLocationRequest request, UUID currentUserId) {
        var existing = locationRepository.findById(id)
                .orElseThrow(() -> new LocationNotFoundException("Location not found: " + id));
        var updated = WarehouseLocation.builder()
                .id(existing.getId()).warehouseId(existing.getWarehouseId()).code(existing.getCode())
                .name(request.getName() != null ? request.getName() : existing.getName())
                .locationStatusId(request.getLocationStatusId() != null ? request.getLocationStatusId() : existing.getLocationStatusId())
                .createdAt(existing.getCreatedAt()).createdBy(existing.getCreatedBy())
                .updatedAt(Instant.now()).updatedBy(currentUserId)
                .build();
        return mapper.toDto(locationRepository.update(updated));
    }

    @Override
    @Transactional
    public void deleteLocation(UUID id) {
        var existing = locationRepository.findById(id)
                .orElseThrow(() -> new LocationNotFoundException("Location not found: " + id));

        boolean hasStock = ctx.fetchExists(
                ctx.selectFrom(STOCK_BALANCES).where(STOCK_BALANCES.LOCATION_ID.eq(id)));
        if (hasStock) {
            throw new LocationConflictException("Cannot deactivate location — contains active stock: " + id);
        }

        UUID inactiveStatusId = ctx.select(LOCATION_STATUSES.ID)
                .from(LOCATION_STATUSES)
                .where(LOCATION_STATUSES.NAME.eq("INACTIVE"))
                .fetchOptionalInto(UUID.class)
                .orElseThrow(() -> new IllegalStateException("INACTIVE status not found in location_statuses"));

        var deactivated = WarehouseLocation.builder()
                .id(existing.getId()).warehouseId(existing.getWarehouseId()).code(existing.getCode())
                .name(existing.getName()).locationStatusId(inactiveStatusId)
                .createdAt(existing.getCreatedAt()).createdBy(existing.getCreatedBy())
                .updatedAt(Instant.now()).updatedBy(existing.getUpdatedBy())
                .build();
        locationRepository.update(deactivated);
    }

    private WarehouseLocationDto enrichDto(WarehouseLocation location) {
        var dto = mapper.toDto(location);
        if (dto.getLocationStatusId() != null) {
            String statusName = ctx.select(LOCATION_STATUSES.NAME).from(LOCATION_STATUSES)
                    .where(LOCATION_STATUSES.ID.eq(dto.getLocationStatusId()))
                    .fetchOneInto(String.class);
            dto.setLocationStatusName(statusName);
        }
        if (dto.getWarehouseId() != null) {
            String warehouseName = ctx.select(WAREHOUSES.NAME).from(WAREHOUSES)
                    .where(WAREHOUSES.ID.eq(dto.getWarehouseId()))
                    .fetchOneInto(String.class);
            dto.setWarehouseName(warehouseName);
        }
        return dto;
    }
}
