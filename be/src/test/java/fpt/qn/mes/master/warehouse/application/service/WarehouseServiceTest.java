package fpt.qn.mes.master.warehouse.application.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.UUID;

import org.jooq.DSLContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import fpt.qn.mes.master.warehouse.application.dto.request.CreateWarehouseRequest;
import fpt.qn.mes.master.warehouse.application.dto.request.UpdateWarehouseRequest;
import fpt.qn.mes.master.warehouse.application.dto.response.WarehouseDto;
import fpt.qn.mes.master.warehouse.application.exception.WarehouseConflictException;
import fpt.qn.mes.master.warehouse.application.exception.WarehouseNotFoundException;
import fpt.qn.mes.master.warehouse.application.mapper.WarehouseDtoMapper;
import fpt.qn.mes.master.warehouse.domain.entities.Warehouse;
import fpt.qn.mes.master.warehouse.domain.repository.WarehouseRepository;

@ExtendWith(MockitoExtension.class)
class WarehouseServiceTest {

    @Mock WarehouseRepository warehouseRepository;
    @Mock WarehouseDtoMapper mapper;
    @Mock DSLContext ctx;
    @InjectMocks WarehouseService warehouseService;

    UUID id = UUID.randomUUID();
    UUID userId = UUID.randomUUID();
    UUID statusId = UUID.randomUUID();
    Warehouse warehouse;
    WarehouseDto dto;

    @BeforeEach
    void setUp() {
        warehouse = Warehouse.create("WH-TEST", "Test WH", "Addr", statusId, userId);
        dto = WarehouseDto.builder().id(id).code("WH-TEST").name("Test WH").build();
    }

    @Test
    void createWarehouse_Success() {
        when(warehouseRepository.existsByCode("WH-TEST")).thenReturn(false);
        when(warehouseRepository.save(any())).thenReturn(warehouse);
        when(mapper.toDto(any())).thenReturn(dto);

        var req = new CreateWarehouseRequest();
        req.setCode("WH-TEST"); req.setName("Test WH"); req.setAddress("Addr");
        req.setWarehouseStatusId(statusId);

        var result = warehouseService.createWarehouse(req, userId);
        assertNotNull(result);
        assertEquals("WH-TEST", result.getCode());
    }

    @Test
    void createWarehouse_DuplicateCode_ThrowsConflict() {
        when(warehouseRepository.existsByCode("WH-TEST")).thenReturn(true);
        var req = new CreateWarehouseRequest();
        req.setCode("WH-TEST"); req.setName("Test"); req.setWarehouseStatusId(statusId);
        assertThrows(WarehouseConflictException.class, () -> warehouseService.createWarehouse(req, userId));
    }

    @Test
    void getWarehouseById_NotFound_Throws404() {
        when(warehouseRepository.findById(id)).thenReturn(Optional.empty());
        assertThrows(WarehouseNotFoundException.class, () -> warehouseService.getWarehouseById(id));
    }

    @Test
    void getWarehouseById_Success() {
        when(warehouseRepository.findById(id)).thenReturn(Optional.of(warehouse));
        when(mapper.toDto(warehouse)).thenReturn(dto);
        var result = warehouseService.getWarehouseById(id);
        assertNotNull(result);
    }
}
