package fpt.qn.mes.master.warehouse.application.service;

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
import fpt.qn.mes.master.warehouse.application.dto.warehousestatus.WarehouseStatusResponse;
import fpt.qn.mes.master.warehouse.application.dto.warehousestatus.create.CreateWarehouseStatusRequest;
import fpt.qn.mes.master.warehouse.application.dto.warehousestatus.search.WarehouseStatusSearchRequest;
import fpt.qn.mes.master.warehouse.application.exception.WarehouseStatusConflictException;
import fpt.qn.mes.master.warehouse.application.mapper.WarehouseStatusDtoMapper;
import fpt.qn.mes.master.warehouse.domain.entities.WarehouseStatus;
import fpt.qn.mes.master.warehouse.domain.repository.WarehouseStatusRepository;

@ExtendWith(MockitoExtension.class)
class WarehouseStatusServiceTest {

    @Mock WarehouseStatusRepository warehousestatusRepository;
    @Mock WarehouseStatusDtoMapper mapper;
    @InjectMocks WarehouseStatusService warehousestatusService;

    @Test
    void getWarehouseStatuss_Success() {
        WarehouseStatus status = WarehouseStatus.builder().id(UUID.randomUUID()).name("ACTIVE").build();
        WarehouseStatusResponse response = WarehouseStatusResponse.builder().id(status.getId()).name("ACTIVE").build();
        PaginationResult<WarehouseStatus> page = PaginationResult.<WarehouseStatus>builder().total(1).items(List.of(status)).build();

        when(warehousestatusRepository.search(any())).thenReturn(page);
        when(mapper.toDto(status)).thenReturn(response);

        var result = warehousestatusService.getWarehouseStatuses(new WarehouseStatusSearchRequest());
        
        assertEquals(1, result.getItems().size());
        assertEquals("ACTIVE", result.getItems().get(0).getName());
    }
    
    @Test
    void createWarehouseStatus_Success() {
        when(warehousestatusRepository.existsByName("ACTIVE")).thenReturn(false);
        var req = new CreateWarehouseStatusRequest();
        req.setName("ACTIVE");
        req.setDescription("Desc");
        
        assertDoesNotThrow(() -> warehousestatusService.createWarehouseStatus(req));
        verify(warehousestatusRepository).save(any());
    }
    
    @Test
    void createWarehouseStatus_Duplicate_ThrowsConflict() {
        when(warehousestatusRepository.existsByName("ACTIVE")).thenReturn(true);
        var req = new CreateWarehouseStatusRequest();
        req.setName("ACTIVE");
        
        assertThrows(WarehouseStatusConflictException.class, () -> warehousestatusService.createWarehouseStatus(req));
    }
}
