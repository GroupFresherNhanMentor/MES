package fpt.qn.mes.master.warehouse.application.service;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fpt.qn.mes.audit.domain.entities.AuditAction;
import fpt.qn.mes.audit.domain.events.AuditEvent;
import fpt.qn.mes.auth.application.port.out.CurrentUserPort;
import fpt.qn.mes.common.port.out.JsonSerializerPort;
import fpt.qn.mes.common.dto.response.PageResponse;
import fpt.qn.mes.common.util.PaginationUtils;
import fpt.qn.mes.master.warehouse.application.dto.warehouse.WarehouseManagerResponse;
import fpt.qn.mes.master.warehouse.application.dto.warehouse.WarehouseResponse;
import fpt.qn.mes.master.warehouse.application.dto.warehouse.assign.AssignManagerRequest;
import fpt.qn.mes.master.warehouse.application.dto.warehouse.create.CreateWarehouseRequest;
import fpt.qn.mes.master.warehouse.application.dto.warehouse.search.WarehouseSearchRequest;
import fpt.qn.mes.master.warehouse.application.dto.warehouse.update.UpdateWarehouseRequest;
import fpt.qn.mes.master.warehouse.application.exception.WarehouseConflictException;
import fpt.qn.mes.master.warehouse.application.exception.WarehouseManagerAlreadyAssignedException;
import fpt.qn.mes.master.warehouse.application.exception.WarehouseNotFoundException;
import fpt.qn.mes.master.warehouse.application.exception.WarehouseStatusNotFoundException;
import fpt.qn.mes.master.warehouse.application.mapper.WarehouseDtoMapper;
import fpt.qn.mes.master.warehouse.application.port.in.WarehouseUseCase;
import fpt.qn.mes.master.warehouse.application.port.out.WarehouseLocationPort;
import fpt.qn.mes.master.warehouse.domain.entities.Warehouse;
import fpt.qn.mes.master.warehouse.domain.repository.WarehouseRepository;
import fpt.qn.mes.master.warehouse.domain.repository.WarehouseStatusRepository;
import fpt.qn.mes.master.warehouse.domain.constants.WarehouseStatusConstants;
import fpt.qn.mes.master.warehouse.domain.repository.criteria.WarehouseSearchCriteria;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class WarehouseService implements WarehouseUseCase {

    WarehouseRepository warehouseRepository;
    WarehouseStatusRepository warehouseStatusRepository;
    WarehouseLocationPort warehouseLocationPort;
    WarehouseDtoMapper mapper;
    CurrentUserPort currentUserPort;
    ApplicationEventPublisher eventPublisher;
    JsonSerializerPort jsonSerializer;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<WarehouseResponse> getWarehouses(WarehouseSearchRequest request) {
        WarehouseSearchCriteria criteria = WarehouseSearchCriteria.builder()
                .code(request.getCode())
                .name(request.getName())
                .warehouseStatusId(request.getWarehouseStatusId())
                .page(request.getPage())
                .size(request.getSize())
                .sort(request.getSort())
                .build();

        var result = warehouseRepository.search(criteria);
        var items = result.getItems().stream().map(w -> mapper.toDto(w)).toList();
        return PageResponse.<WarehouseResponse>builder()
                .items(items).totalElements(result.getTotal())
                .pageNumber(request.getPage()).pageSize(request.getSize())
                .totalPages(PaginationUtils.calculateTotalPages(result.getTotal(), request.getSize()))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public WarehouseResponse getWarehouseById(UUID id) {
        return warehouseRepository.findById(id)
                .map(w -> mapper.toDto(w))
                .orElseThrow(() -> new WarehouseNotFoundException("Warehouse not found: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public WarehouseResponse getWarehouseByCode(String code) {
        return warehouseRepository.findByCode(code)
                .map(mapper::toDto)
                .orElseThrow(() -> new WarehouseNotFoundException("Warehouse not found with code: " + code));
    }

    @Override
    @Transactional(readOnly = true)
    public Map<UUID, WarehouseResponse> getWarehousesByIds(Collection<UUID> ids) {
        if (ids == null || ids.isEmpty()) return Map.of();
        var warehouses = warehouseRepository.findByIds(ids);
        return warehouses.stream()
                .collect(Collectors.toMap(Warehouse::getId, mapper::toDto, (w1, w2) -> w1));
    }

    @Override
    @Transactional
    public void createWarehouse(CreateWarehouseRequest request) {
        if (warehouseRepository.existsByCode(request.getCode())) {
            throw new WarehouseConflictException("Warehouse code already exists: " + request.getCode());
        }
        var activeStatus = warehouseStatusRepository.findByName(WarehouseStatusConstants.ACTIVE)
                .orElseThrow(() -> new WarehouseStatusNotFoundException("ACTIVE status not found in warehouse_statuses"));
        UUID currentUserId = currentUserPort.getCurrentUserId();
        var warehouse = Warehouse.create(request.getCode(), request.getName(), request.getAddress(), activeStatus.getId(), currentUserId);
        warehouseRepository.save(warehouse);
        eventPublisher.publishEvent(AuditEvent.create(currentUserId, AuditAction.CREATE_WAREHOUSE,
                "WAREHOUSE", warehouse.getId(), null, jsonSerializer.toJson(warehouse), null));
    }

    @Override
    @Transactional
    public void updateWarehouse(UUID id, UpdateWarehouseRequest request) {
        var existing = warehouseRepository.findById(id)
                .orElseThrow(() -> new WarehouseNotFoundException("Warehouse not found: " + id));

        if (request.getWarehouseStatusId() != null && !warehouseStatusRepository.existsById(request.getWarehouseStatusId())) {
            throw new WarehouseStatusNotFoundException("Warehouse status not found: " + request.getWarehouseStatusId());
        }
        UUID currentUserId = currentUserPort.getCurrentUserId();
        var updated = Warehouse.update(existing, request.getName(), request.getAddress(), request.getWarehouseStatusId(), currentUserId);
        warehouseRepository.update(updated);
        eventPublisher.publishEvent(AuditEvent.create(currentUserId, AuditAction.UPDATE_WAREHOUSE,
                "WAREHOUSE", id, jsonSerializer.toJson(existing), jsonSerializer.toJson(updated), null));
    }

    @Override
    @Transactional
    public void activateWarehouse(UUID id) {
        var existing = warehouseRepository.findById(id)
                .orElseThrow(() -> new WarehouseNotFoundException("Warehouse not found: " + id));
        var activeStatus = warehouseStatusRepository.findByName(WarehouseStatusConstants.ACTIVE)
                .orElseThrow(() -> new WarehouseStatusNotFoundException("ACTIVE status not found in warehouse_statuses"));
        UUID currentUserId = currentUserPort.getCurrentUserId();
        var activated = Warehouse.activate(existing, activeStatus.getId(), currentUserId);
        warehouseRepository.update(activated);
        eventPublisher.publishEvent(AuditEvent.create(currentUserId, AuditAction.ACTIVATE_WAREHOUSE,
                "WAREHOUSE", id, jsonSerializer.toJson(existing), jsonSerializer.toJson(activated), null));
    }

    @Override
    @Transactional
    public void deactivateWarehouse(UUID id) {
        var existing = warehouseRepository.findById(id)
                .orElseThrow(() -> new WarehouseNotFoundException("Warehouse not found: " + id));

        var inactiveStatus = warehouseStatusRepository.findByName(WarehouseStatusConstants.INACTIVE)
                .orElseThrow(() -> new WarehouseStatusNotFoundException("INACTIVE status not found in warehouse_statuses"));

        UUID currentUserId = currentUserPort.getCurrentUserId();
        var deactivated = Warehouse.deactivate(existing, inactiveStatus.getId(), currentUserId);
        warehouseRepository.update(deactivated);
        warehouseLocationPort.deactivateAllByWarehouseId(id, currentUserId);
        eventPublisher.publishEvent(AuditEvent.create(currentUserId, AuditAction.DEACTIVATE_WAREHOUSE,
                "WAREHOUSE", id, jsonSerializer.toJson(existing), jsonSerializer.toJson(deactivated), null));
    }

    @Override
    @Transactional
    public void assignManager(UUID warehouseId, AssignManagerRequest request) {
        if (!warehouseRepository.existsById(warehouseId)) {
            throw new WarehouseNotFoundException("Warehouse not found: " + warehouseId);
        }
        if (warehouseRepository.isManagerAssigned(warehouseId, request.getUserId())) {
            throw new WarehouseManagerAlreadyAssignedException(
                "User " + request.getUserId() + " is already a manager of warehouse " + warehouseId);
        }
        UUID currentUserId = currentUserPort.getCurrentUserId();
        warehouseRepository.assignManager(warehouseId, request.getUserId(), currentUserId);
        eventPublisher.publishEvent(AuditEvent.create(currentUserId, AuditAction.ASSIGN_WAREHOUSE_MANAGER,
                "WAREHOUSE", warehouseId, null, jsonSerializer.toJson(request), null));
    }

    @Override
    @Transactional
    public void removeManager(UUID warehouseId, UUID userId) {
        if (!warehouseRepository.existsById(warehouseId)) {
            throw new WarehouseNotFoundException("Warehouse not found: " + warehouseId);
        }
        warehouseRepository.removeManager(warehouseId, userId);
        UUID currentUserId = currentUserPort.getCurrentUserId();
        eventPublisher.publishEvent(AuditEvent.create(currentUserId, AuditAction.REMOVE_WAREHOUSE_MANAGER,
                "WAREHOUSE", warehouseId, null, jsonSerializer.toJson(userId), null));
    }

    @Override
    @Transactional(readOnly = true)
    public List<WarehouseManagerResponse> getManagers(UUID warehouseId) {
        if (!warehouseRepository.existsById(warehouseId)) {
            throw new WarehouseNotFoundException("Warehouse not found: " + warehouseId);
        }
        return warehouseRepository.findManagersByWarehouseId(warehouseId).stream()
                .map(ref -> mapper.toManagerResponse(ref))
                .toList();
    }
}
