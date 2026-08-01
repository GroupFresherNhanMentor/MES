package fpt.qn.mes.master.warehouse.application.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import fpt.qn.mes.auth.application.port.out.CurrentUserPort;
import fpt.qn.mes.master.warehouse.application.dto.warehouse.create.CreateWarehouseRequest;
import fpt.qn.mes.master.warehouse.application.dto.warehouse.WarehouseResponse;
import fpt.qn.mes.master.warehouse.application.exception.WarehouseConflictException;
import fpt.qn.mes.master.warehouse.application.exception.WarehouseStatusNotFoundException;
import fpt.qn.mes.master.warehouse.application.exception.WarehouseNotFoundException;
import fpt.qn.mes.master.warehouse.application.mapper.WarehouseDtoMapper;
import fpt.qn.mes.master.warehouse.application.port.out.WarehouseLocationPort;
import fpt.qn.mes.master.warehouse.domain.constants.WarehouseStatusConstants;
import fpt.qn.mes.master.warehouse.domain.entities.Warehouse;
import fpt.qn.mes.master.warehouse.domain.entities.WarehouseStatus;
import fpt.qn.mes.master.warehouse.domain.repository.WarehouseRepository;
import fpt.qn.mes.master.warehouse.domain.repository.WarehouseStatusRepository;

@ExtendWith(MockitoExtension.class)
class WarehouseServiceTest {

    @Mock WarehouseRepository warehouseRepository;
    @Mock WarehouseStatusRepository warehouseStatusRepository;
    @Mock WarehouseLocationPort warehouseLocationPort;
    @Mock WarehouseDtoMapper mapper;
    @Mock CurrentUserPort currentUserPort;
    @InjectMocks WarehouseService warehouseService;

    UUID id = UUID.randomUUID();
    UUID userId = UUID.randomUUID();
    UUID statusId = UUID.randomUUID();
    Warehouse warehouse;
    WarehouseResponse response;

    @BeforeEach
    void setUp() {
        warehouse = Warehouse.create("WH-TEST", "Test Warehouse", "123 Test St", statusId, userId);
        response = WarehouseResponse.builder().id(id).code("WH-TEST").build();
    }

    @Test
    void createWarehouse_Success() {
        WarehouseStatus activeStatus = WarehouseStatus.builder().id(statusId).name(WarehouseStatusConstants.ACTIVE).build();
        when(warehouseRepository.existsByCode("WH-TEST")).thenReturn(false);
        when(warehouseStatusRepository.findByName(WarehouseStatusConstants.ACTIVE)).thenReturn(Optional.of(activeStatus));
        when(currentUserPort.getCurrentUserId()).thenReturn(userId);
        when(warehouseRepository.save(any(Warehouse.class))).thenReturn(warehouse);

        var req = new CreateWarehouseRequest();
        req.setCode("WH-TEST");
        req.setName("Test");
        req.setAddress("123 Test St");

        assertDoesNotThrow(() -> warehouseService.createWarehouse(req));
        verify(warehouseRepository).save(any(Warehouse.class));
    }

    @Test
    void createWarehouse_DuplicateCode_ThrowsConflict() {
        when(warehouseRepository.existsByCode("WH-TEST")).thenReturn(true);
        var req = new CreateWarehouseRequest();
        req.setCode("WH-TEST");
        req.setName("Test");
        req.setAddress("123 Test St");
        assertThrows(WarehouseConflictException.class, () -> warehouseService.createWarehouse(req));
    }

    @Test
    void createWarehouse_ActiveStatusNotFound_ThrowsStatusNotFoundException() {
        when(warehouseRepository.existsByCode("WH-TEST")).thenReturn(false);
        when(warehouseStatusRepository.findByName(WarehouseStatusConstants.ACTIVE)).thenReturn(Optional.empty());
        var req = new CreateWarehouseRequest();
        req.setCode("WH-TEST");
        req.setName("Test");
        req.setAddress("123 Test St");
        assertThrows(WarehouseStatusNotFoundException.class, () -> warehouseService.createWarehouse(req));
    }

    @Test
    void getWarehouseById_NotFound_Throws404() {
        when(warehouseRepository.findById(id)).thenReturn(Optional.empty());
        assertThrows(WarehouseNotFoundException.class, () -> warehouseService.getWarehouseById(id));
    }

    @Test
    void getWarehouseById_Success() {
        when(warehouseRepository.findById(id)).thenReturn(Optional.of(warehouse));
        when(mapper.toDto(warehouse)).thenReturn(response);
        var result = warehouseService.getWarehouseById(id);
        assertNotNull(result);
    }

    @Test
    void deactivateWarehouse_AlsoDeactivatesAllLocations() {
        UUID inactiveStatusId = UUID.randomUUID();
        WarehouseStatus inactiveStatus = WarehouseStatus.builder().id(inactiveStatusId).name(WarehouseStatusConstants.INACTIVE).build();
        when(warehouseRepository.findById(id)).thenReturn(Optional.of(warehouse));
        when(warehouseStatusRepository.findByName(WarehouseStatusConstants.INACTIVE)).thenReturn(Optional.of(inactiveStatus));
        when(currentUserPort.getCurrentUserId()).thenReturn(userId);
        when(warehouseRepository.update(any(Warehouse.class))).thenReturn(warehouse);

        assertDoesNotThrow(() -> warehouseService.deactivateWarehouse(id));

        verify(warehouseRepository).update(any(Warehouse.class));
        verify(warehouseLocationPort).deactivateAllByWarehouseId(id, userId);
    }
}
