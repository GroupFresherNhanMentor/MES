package fpt.qn.mes.bom.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import fpt.qn.mes.auth.application.port.out.CurrentUserPort;
import fpt.qn.mes.bom.application.dto.request.CreateBomRequest;
import fpt.qn.mes.bom.application.dto.response.BomDto;
import fpt.qn.mes.bom.application.exception.BomAlreadyExistsException;
import fpt.qn.mes.bom.application.exception.BomNotFoundException;
import fpt.qn.mes.bom.application.mapper.BomDtoMapper;
import fpt.qn.mes.bom.domain.entities.Bom;
import fpt.qn.mes.bom.domain.repository.BomRepository;
import fpt.qn.mes.master.product.application.dto.response.ProductDto;
import fpt.qn.mes.master.product.application.exception.ProductNotFoundException;
import fpt.qn.mes.master.product.application.port.in.ProductUseCase;

@ExtendWith(MockitoExtension.class)
class BomServiceTest {

    @Mock
    BomRepository bomRepository;

    @Mock
    BomDtoMapper mapper;

    @Mock
    ProductUseCase productUseCase;

    @Mock
    CurrentUserPort currentUserPort;

    @InjectMocks
    BomService bomService;

    UUID finishedProductId;
    UUID userId;
    UUID statusId;

    @BeforeEach
    void setUp() {
        finishedProductId = UUID.randomUUID();
        userId = UUID.randomUUID();
        statusId = UUID.randomUUID();
    }

    @Test
    void createBom_HappyPath_Success() {
        CreateBomRequest request = new CreateBomRequest();
        request.setFinishedProductId(finishedProductId);
        request.setVersion(1);

        ProductDto productDto = ProductDto.builder().id(finishedProductId).build();
        when(productUseCase.getProductById(finishedProductId)).thenReturn(productDto);
        when(bomRepository.existsByFinishedProductIdAndVersion(finishedProductId, 1)).thenReturn(false);
        when(bomRepository.findStatusIdByName("DRAFT")).thenReturn(Optional.of(statusId));
        when(currentUserPort.getCurrentUserId()).thenReturn(userId);

        Bom savedBom = Bom.builder()
                .id(UUID.randomUUID())
                .finishedProductId(finishedProductId)
                .version(1)
                .bomStatusId(statusId)
                .createdBy(userId)
                .createdAt(Instant.now())
                .items(Collections.emptyList())
                .build();

        BomDto bomDto = BomDto.builder()
                .id(savedBom.getId())
                .finishedProductId(finishedProductId)
                .version(1)
                .bomStatusId(statusId)
                .createdBy(userId)
                .createdAt(savedBom.getCreatedAt())
                .items(Collections.emptyList())
                .build();

        when(bomRepository.save(any(Bom.class))).thenReturn(savedBom);
        when(mapper.toDto(savedBom)).thenReturn(bomDto);

        BomDto result = bomService.createBom(request);

        assertNotNull(result);
        assertEquals(finishedProductId, result.getFinishedProductId());
        assertEquals(1, result.getVersion());
        verify(bomRepository).save(any(Bom.class));
    }

    @Test
    void createBom_DuplicateVersion_ThrowsBomAlreadyExistsException() {
        CreateBomRequest request = new CreateBomRequest();
        request.setFinishedProductId(finishedProductId);
        request.setVersion(1);

        ProductDto productDto = ProductDto.builder().id(finishedProductId).build();
        when(productUseCase.getProductById(finishedProductId)).thenReturn(productDto);
        when(bomRepository.existsByFinishedProductIdAndVersion(finishedProductId, 1)).thenReturn(true);
        when(currentUserPort.getCurrentUserId()).thenReturn(userId);

        assertThrows(BomAlreadyExistsException.class, () -> bomService.createBom(request));
        verify(bomRepository, never()).save(any(Bom.class));
    }

    @Test
    void createBom_ProductNotFound_ThrowsProductNotFoundException() {
        CreateBomRequest request = new CreateBomRequest();
        request.setFinishedProductId(finishedProductId);
        request.setVersion(1);

        when(productUseCase.getProductById(finishedProductId))
                .thenThrow(new ProductNotFoundException("Product not found: " + finishedProductId));
        when(currentUserPort.getCurrentUserId()).thenReturn(userId);

        assertThrows(ProductNotFoundException.class, () -> bomService.createBom(request));
        verify(bomRepository, never()).save(any(Bom.class));
    }

    @Test
    void getBomById_NotFound_ThrowsBomNotFoundException() {
        UUID randomId = UUID.randomUUID();
        when(bomRepository.findById(randomId)).thenReturn(Optional.empty());

        assertThrows(BomNotFoundException.class, () -> bomService.getBomById(randomId));
    }
}
