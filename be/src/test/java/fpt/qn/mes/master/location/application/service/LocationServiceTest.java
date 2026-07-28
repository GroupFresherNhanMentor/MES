package fpt.qn.mes.master.location.application.service;

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

import fpt.qn.mes.master.location.application.dto.request.CreateLocationRequest;
import fpt.qn.mes.master.location.application.dto.request.UpdateLocationRequest;
import fpt.qn.mes.master.location.application.dto.response.WarehouseLocationDto;
import fpt.qn.mes.master.location.application.exception.LocationConflictException;
import fpt.qn.mes.master.location.application.exception.LocationNotFoundException;
import fpt.qn.mes.master.location.application.mapper.LocationDtoMapper;
import fpt.qn.mes.master.location.domain.entities.WarehouseLocation;
import fpt.qn.mes.master.location.domain.repository.WarehouseLocationRepository;

@ExtendWith(MockitoExtension.class)
class LocationServiceTest {

    @Mock WarehouseLocationRepository locationRepository;
    @Mock LocationDtoMapper mapper;
    @Mock DSLContext ctx;
    @InjectMocks LocationService locationService;

    UUID id = UUID.randomUUID();
    UUID whId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();
    UUID statusId = UUID.randomUUID();
    WarehouseLocation location;
    WarehouseLocationDto dto;

    @BeforeEach
    void setUp() {
        location = WarehouseLocation.create(whId, "LOC-TEST", "Test Loc", statusId, userId);
        dto = WarehouseLocationDto.builder().id(id).code("LOC-TEST").build();
    }

    @Test
    void createLocation_Success() {
        when(locationRepository.existsByWarehouseIdAndCode(whId, "LOC-TEST")).thenReturn(false);
        when(locationRepository.save(any())).thenReturn(location);
        when(mapper.toDto(any())).thenReturn(dto);

        var req = new CreateLocationRequest();
        req.setCode("LOC-TEST"); req.setName("Test"); req.setLocationStatusId(statusId);

        var result = locationService.createLocation(whId, req, userId);
        assertNotNull(result);
    }

    @Test
    void createLocation_DuplicateCode_ThrowsConflict() {
        when(locationRepository.existsByWarehouseIdAndCode(whId, "LOC-TEST")).thenReturn(true);
        var req = new CreateLocationRequest();
        req.setCode("LOC-TEST"); req.setLocationStatusId(statusId);
        assertThrows(LocationConflictException.class, () -> locationService.createLocation(whId, req, userId));
    }

    @Test
    void getLocationById_NotFound_Throws404() {
        when(locationRepository.findById(id)).thenReturn(Optional.empty());
        assertThrows(LocationNotFoundException.class, () -> locationService.getLocationById(id));
    }
}
