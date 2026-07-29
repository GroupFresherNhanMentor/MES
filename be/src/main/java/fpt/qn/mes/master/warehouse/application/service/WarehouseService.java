package fpt.qn.mes.master.warehouse.application.service;

import java.time.Instant;
import java.util.UUID;

import org.jooq.DSLContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fpt.qn.mes.common.dto.response.PageResponse;
import fpt.qn.mes.master.warehouse.application.dto.request.CreateWarehouseRequest;
import fpt.qn.mes.master.warehouse.application.dto.request.UpdateWarehouseRequest;
import fpt.qn.mes.master.warehouse.application.dto.response.WarehouseDto;
import fpt.qn.mes.master.warehouse.application.exception.WarehouseConflictException;
import fpt.qn.mes.master.warehouse.application.exception.WarehouseNotFoundException;
import fpt.qn.mes.master.warehouse.application.mapper.WarehouseDtoMapper;
import fpt.qn.mes.master.warehouse.application.port.in.WarehouseUseCase;
import fpt.qn.mes.master.warehouse.domain.entities.Warehouse;
import fpt.qn.mes.master.warehouse.domain.repository.WarehouseRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import static fpt.qn.mes.jooq.Tables.STOCK_BALANCES;
import static fpt.qn.mes.jooq.Tables.WAREHOUSE_STATUSES;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class WarehouseService implements WarehouseUseCase {

    WarehouseRepository warehouseRepository;
    WarehouseDtoMapper mapper;
    DSLContext ctx;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<WarehouseDto> getWarehouses(int page, int size) {
        UUID activeStatusId = getActiveStatusId();
        return getWarehousesByStatus(page, size, activeStatusId);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<WarehouseDto> getWarehousesByStatus(int page, int size, UUID statusId) {
        var result = warehouseRepository.findAllByStatus(page, size, statusId);
        var items = result.getItems().stream().map(mapper::toDto).toList();
        return PageResponse.<WarehouseDto>builder()
                .items(items)
                .totalElements(result.getTotal())
                .pageNumber(page)
                .pageSize(size)
                .totalPages((int) Math.ceil((double) result.getTotal() / size))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public WarehouseDto getWarehouseById(UUID id) {
        return warehouseRepository.findById(id)
                .map(mapper::toDto)
                .orElseThrow(() -> new WarehouseNotFoundException("Warehouse not found: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public java.util.Map<UUID, WarehouseDto> getWarehousesByIds(java.util.Collection<UUID> ids) {
        if (ids == null || ids.isEmpty()) return java.util.Map.of();
        var warehouses = warehouseRepository.findByIds(ids);
        return warehouses.stream()
                .collect(java.util.stream.Collectors.toMap(Warehouse::getId, mapper::toDto, (w1, w2) -> w1));
    }

    @Override
    @Transactional
    public WarehouseDto createWarehouse(CreateWarehouseRequest request, UUID currentUserId) {
        if (warehouseRepository.existsByCode(request.getCode())) {
            throw new WarehouseConflictException("Warehouse code already exists: " + request.getCode());
        }
        var warehouse = Warehouse.create(
                request.getCode(), request.getName(), request.getAddress(),
                request.getWarehouseStatusId(), currentUserId);
        return mapper.toDto(warehouseRepository.save(warehouse));
    }

    @Override
    @Transactional
    public WarehouseDto updateWarehouse(UUID id, UpdateWarehouseRequest request, UUID currentUserId) {
        var existing = warehouseRepository.findById(id)
                .orElseThrow(() -> new WarehouseNotFoundException("Warehouse not found: " + id));
        var updated = Warehouse.builder()
                .id(existing.getId())
                .code(existing.getCode())
                .name(request.getName() != null ? request.getName() : existing.getName())
                .address(request.getAddress() != null ? request.getAddress() : existing.getAddress())
                .warehouseStatusId(request.getWarehouseStatusId() != null ? request.getWarehouseStatusId() : existing.getWarehouseStatusId())
                .createdAt(existing.getCreatedAt())
                .createdBy(existing.getCreatedBy())
                .updatedAt(Instant.now())
                .updatedBy(currentUserId)
                .build();
        return mapper.toDto(warehouseRepository.update(updated));
    }

    @Override
    @Transactional
    public void deleteWarehouse(UUID id) {
        var existing = warehouseRepository.findById(id)
                .orElseThrow(() -> new WarehouseNotFoundException("Warehouse not found: " + id));

        boolean hasStock = ctx.fetchExists(
                ctx.selectFrom(STOCK_BALANCES).where(STOCK_BALANCES.WAREHOUSE_ID.eq(id)));
        if (hasStock) {
            throw new WarehouseConflictException("Cannot deactivate warehouse — contains active stock: " + id);
        }

        UUID inactiveStatusId = ctx.select(WAREHOUSE_STATUSES.ID)
                .from(WAREHOUSE_STATUSES)
                .where(WAREHOUSE_STATUSES.NAME.eq("INACTIVE"))
                .fetchOptionalInto(UUID.class)
                .orElseThrow(() -> new IllegalStateException("INACTIVE status not found in warehouse_statuses"));

        var deactivated = Warehouse.builder()
                .id(existing.getId()).code(existing.getCode()).name(existing.getName())
                .address(existing.getAddress()).warehouseStatusId(inactiveStatusId)
                .createdAt(existing.getCreatedAt()).createdBy(existing.getCreatedBy())
                .updatedAt(Instant.now()).updatedBy(existing.getUpdatedBy())
                .build();
        warehouseRepository.update(deactivated);
    }

    private UUID getActiveStatusId() {
        return ctx.select(WAREHOUSE_STATUSES.ID)
                .from(WAREHOUSE_STATUSES)
                .where(WAREHOUSE_STATUSES.NAME.eq("ACTIVE"))
                .fetchOptionalInto(UUID.class)
                .orElseThrow(() -> new IllegalStateException("ACTIVE status not found in warehouse_statuses"));
    }
}
