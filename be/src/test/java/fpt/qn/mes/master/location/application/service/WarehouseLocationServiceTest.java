package fpt.qn.mes.master.location.application.service;

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
import fpt.qn.mes.master.location.application.dto.warehouselocation.create.CreateWarehouseLocationRequest;
import fpt.qn.mes.master.location.application.dto.warehouselocation.WarehouseLocationResponse;
import fpt.qn.mes.master.location.application.exception.LocationStatusNotFoundException;
import fpt.qn.mes.master.location.application.exception.WarehouseLocationConflictException;
import fpt.qn.mes.master.location.application.exception.WarehouseLocationNotFoundException;
import fpt.qn.mes.master.location.application.mapper.WarehouseLocationDtoMapper;
import fpt.qn.mes.master.location.domain.entities.WarehouseLocation;
import fpt.qn.mes.master.location.domain.entities.LocationStatus;
import fpt.qn.mes.master.location.domain.repository.WarehouseLocationRepository;
import fpt.qn.mes.master.location.domain.repository.LocationStatusRepository;

@ExtendWith(MockitoExtension.class)
class WarehouseLocationServiceTest {

    @Mock WarehouseLocationRepository warehouseLocationRepository;
    @Mock LocationStatusRepository locationStatusRepository;
    @Mock WarehouseLocationDtoMapper mapper;
    @Mock CurrentUserPort currentUserPort;
    @InjectMocks WarehouseLocationService warehouseLocationService;

    UUID id = UUID.randomUUID();
    UUID warehouseId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();
    UUID statusId = UUID.randomUUID();
    WarehouseLocation location;
    WarehouseLocationResponse response;

    @BeforeEach
    void setUp() {
        location = WarehouseLocation.create(warehouseId, "LOC-TEST", "Test Location", statusId, userId);
        response = WarehouseLocationResponse.builder().id(id).code("LOC-TEST").build();
    }

    @Test
    void createWarehouseLocation_Success() {
        when(warehouseLocationRepository.existsByWarehouseIdAndCode(warehouseId, "LOC-TEST")).thenReturn(false);
        when(locationStatusRepository.existsById(statusId)).thenReturn(true);
        when(currentUserPort.getCurrentUserId()).thenReturn(userId);
        when(warehouseLocationRepository.save(any(WarehouseLocation.class))).thenReturn(location);

        var req = new CreateWarehouseLocationRequest();
        req.setCode("LOC-TEST");
        req.setName("Test");
        req.setLocationStatusId(statusId);

        assertDoesNotThrow(() -> warehouseLocationService.createWarehouseLocation(warehouseId, req));
        verify(warehouseLocationRepository).save(any(WarehouseLocation.class));
    }

    @Test
    void createWarehouseLocation_DuplicateCode_ThrowsConflict() {
        when(warehouseLocationRepository.existsByWarehouseIdAndCode(warehouseId, "LOC-TEST")).thenReturn(true);
        var req = new CreateWarehouseLocationRequest();
        req.setCode("LOC-TEST");
        req.setName("Test");
        req.setLocationStatusId(statusId);
        assertThrows(WarehouseLocationConflictException.class, () -> warehouseLocationService.createWarehouseLocation(warehouseId, req));
    }

    @Test
    void createWarehouseLocation_StatusNotFound_ThrowsConflict() {
        when(warehouseLocationRepository.existsByWarehouseIdAndCode(warehouseId, "LOC-TEST")).thenReturn(false);
        when(locationStatusRepository.existsById(statusId)).thenReturn(false);
        var req = new CreateWarehouseLocationRequest();
        req.setCode("LOC-TEST");
        req.setName("Test");
        req.setLocationStatusId(statusId);
        assertThrows(LocationStatusNotFoundException.class, () -> warehouseLocationService.createWarehouseLocation(warehouseId, req));
    }

    @Test
    void getWarehouseLocationById_NotFound_Throws404() {
        when(warehouseLocationRepository.findById(id)).thenReturn(Optional.empty());
        assertThrows(WarehouseLocationNotFoundException.class, () -> warehouseLocationService.getWarehouseLocationById(id));
    }

    @Test
    void getWarehouseLocationById_Success() {
        when(warehouseLocationRepository.findById(id)).thenReturn(Optional.of(location));
        when(mapper.toDto(location)).thenReturn(response);
        var result = warehouseLocationService.getWarehouseLocationById(id);
        assertNotNull(result);
    }
}
