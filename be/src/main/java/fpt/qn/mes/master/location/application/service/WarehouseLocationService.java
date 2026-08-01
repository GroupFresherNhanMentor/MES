package fpt.qn.mes.master.location.application.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fpt.qn.mes.auth.application.port.out.CurrentUserPort;
import fpt.qn.mes.common.dto.response.PageResponse;
import fpt.qn.mes.common.util.PaginationUtils;
import fpt.qn.mes.master.location.application.dto.warehouselocation.WarehouseLocationResponse;
import fpt.qn.mes.master.location.application.dto.warehouselocation.create.CreateWarehouseLocationRequest;
import fpt.qn.mes.master.location.application.dto.warehouselocation.search.WarehouseLocationSearchRequest;
import fpt.qn.mes.master.location.application.dto.warehouselocation.update.UpdateWarehouseLocationRequest;
import fpt.qn.mes.master.location.application.exception.LocationStatusNotFoundException;
import fpt.qn.mes.master.location.application.exception.WarehouseLocationConflictException;
import fpt.qn.mes.master.location.application.exception.WarehouseLocationNotFoundException;
import fpt.qn.mes.master.location.application.mapper.WarehouseLocationDtoMapper;
import fpt.qn.mes.master.location.application.port.in.WarehouseLocationUseCase;
import fpt.qn.mes.master.location.domain.entities.WarehouseLocation;
import fpt.qn.mes.master.location.domain.repository.WarehouseLocationRepository;
import fpt.qn.mes.master.location.domain.repository.LocationStatusRepository;
import fpt.qn.mes.master.location.domain.constants.LocationStatusConstants;
import fpt.qn.mes.master.location.domain.repository.criteria.WarehouseLocationSearchCriteria;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class WarehouseLocationService implements WarehouseLocationUseCase {

    WarehouseLocationRepository warehouseLocationRepository;
    LocationStatusRepository locationStatusRepository;
    WarehouseLocationDtoMapper mapper;
    CurrentUserPort currentUserPort;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<WarehouseLocationResponse> getWarehouseLocations(UUID warehouseId, WarehouseLocationSearchRequest request) {
        WarehouseLocationSearchCriteria criteria = WarehouseLocationSearchCriteria.builder()
                .warehouseId(warehouseId)
                .code(request.getCode())
                .name(request.getName())
                .locationStatusId(request.getLocationStatusId())
                .page(request.getPage())
                .size(request.getSize())
                .sort(request.getSort())
                .build();

        var result = warehouseLocationRepository.search(criteria);
        var items = result.getItems().stream().map(loc -> mapper.toDto(loc)).toList();
        return PageResponse.<WarehouseLocationResponse>builder()
                .items(items).totalElements(result.getTotal())
                .pageNumber(request.getPage()).pageSize(request.getSize())
                .totalPages(PaginationUtils.calculateTotalPages(result.getTotal(), request.getSize()))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public WarehouseLocationResponse getWarehouseLocationById(UUID id) {
        return warehouseLocationRepository.findById(id)
                .map(loc -> mapper.toDto(loc))
                .orElseThrow(() -> new WarehouseLocationNotFoundException("WarehouseLocation not found: " + id));
    }

    @Override
    @Transactional
    public void createWarehouseLocation(UUID warehouseId, CreateWarehouseLocationRequest request) {
        if (warehouseLocationRepository.existsByWarehouseIdAndCode(warehouseId, request.getCode())) {
            throw new WarehouseLocationConflictException("WarehouseLocation code already exists: " + request.getCode());
        }
        var activeStatus = locationStatusRepository.findByName(LocationStatusConstants.ACTIVE)
                .orElseThrow(() -> new LocationStatusNotFoundException("ACTIVE status not found in location_statuses"));
        UUID currentUserId = currentUserPort.getCurrentUserId();
        warehouseLocationRepository.save(WarehouseLocation.create(warehouseId, request.getCode(), request.getName(), activeStatus.getId(), currentUserId));
    }

    @Override
    @Transactional
    public void updateWarehouseLocation(UUID id, UpdateWarehouseLocationRequest request) {
        var existing = warehouseLocationRepository.findById(id)
                .orElseThrow(() -> new WarehouseLocationNotFoundException("WarehouseLocation not found: " + id));

        if (request.getLocationStatusId() != null && !locationStatusRepository.existsById(request.getLocationStatusId())) {
            throw new LocationStatusNotFoundException("Location status not found: " + request.getLocationStatusId());
        }
        UUID currentUserId = currentUserPort.getCurrentUserId();
        warehouseLocationRepository.update(WarehouseLocation.update(existing, request.getName(), request.getLocationStatusId(), currentUserId));
    }

    @Override
    @Transactional
    public void activateWarehouseLocation(UUID id) {
        var existing = warehouseLocationRepository.findById(id)
                .orElseThrow(() -> new WarehouseLocationNotFoundException("WarehouseLocation not found: " + id));
        var activeStatus = locationStatusRepository.findByName(LocationStatusConstants.ACTIVE)
                .orElseThrow(() -> new LocationStatusNotFoundException("ACTIVE status not found in location_statuses"));
        UUID currentUserId = currentUserPort.getCurrentUserId();
        warehouseLocationRepository.update(WarehouseLocation.activate(existing, activeStatus.getId(), currentUserId));
    }

    @Override
    @Transactional
    public void deactivateWarehouseLocation(UUID id) {
        var existing = warehouseLocationRepository.findById(id)
                .orElseThrow(() -> new WarehouseLocationNotFoundException("WarehouseLocation not found: " + id));

        var inactiveStatus = locationStatusRepository.findByName(LocationStatusConstants.INACTIVE)
                .orElseThrow(() -> new LocationStatusNotFoundException("INACTIVE status not found in location_statuses"));

        UUID currentUserId = currentUserPort.getCurrentUserId();
        warehouseLocationRepository.update(WarehouseLocation.deactivate(existing, inactiveStatus.getId(), currentUserId));
    }
}
