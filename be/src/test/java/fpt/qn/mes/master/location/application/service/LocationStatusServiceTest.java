package fpt.qn.mes.master.location.application.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import fpt.qn.mes.common.domainQuery.PaginationResult;
import fpt.qn.mes.master.location.application.dto.locationstatus.LocationStatusResponse;
import fpt.qn.mes.master.location.application.dto.locationstatus.create.CreateLocationStatusRequest;
import fpt.qn.mes.master.location.application.dto.locationstatus.search.LocationStatusSearchRequest;
import fpt.qn.mes.master.location.application.exception.LocationStatusConflictException;
import fpt.qn.mes.master.location.application.mapper.LocationStatusDtoMapper;
import fpt.qn.mes.master.location.domain.entities.LocationStatus;
import fpt.qn.mes.master.location.domain.repository.LocationStatusRepository;

@ExtendWith(MockitoExtension.class)
class LocationStatusServiceTest {

    @Mock LocationStatusRepository locationstatusRepository;
    @Mock LocationStatusDtoMapper mapper;
    @InjectMocks LocationStatusService locationstatusService;

    @Test
    void getLocationStatuss_Success() {
        LocationStatus status = LocationStatus.builder().id(UUID.randomUUID()).name("ACTIVE").build();
        LocationStatusResponse response = LocationStatusResponse.builder().id(status.getId()).name("ACTIVE").build();
        PaginationResult<LocationStatus> page = PaginationResult.<LocationStatus>builder().total(1).items(List.of(status)).build();

        when(locationstatusRepository.search(any())).thenReturn(page);
        when(mapper.toDto(status)).thenReturn(response);

        var result = locationstatusService.getLocationStatuses(new LocationStatusSearchRequest());
        
        assertEquals(1, result.getItems().size());
        assertEquals("ACTIVE", result.getItems().get(0).getName());
    }
    
    @Test
    void createLocationStatus_Success() {
        when(locationstatusRepository.existsByName("ACTIVE")).thenReturn(false);
        var req = new CreateLocationStatusRequest();
        req.setName("ACTIVE");
        req.setDescription("Desc");
        
        assertDoesNotThrow(() -> locationstatusService.createLocationStatus(req));
        verify(locationstatusRepository).save(any());
    }
    
    @Test
    void createLocationStatus_Duplicate_ThrowsConflict() {
        when(locationstatusRepository.existsByName("ACTIVE")).thenReturn(true);
        var req = new CreateLocationStatusRequest();
        req.setName("ACTIVE");
        
        assertThrows(LocationStatusConflictException.class, () -> locationstatusService.createLocationStatus(req));
    }
}
