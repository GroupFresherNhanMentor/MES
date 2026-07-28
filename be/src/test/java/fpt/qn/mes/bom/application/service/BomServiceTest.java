package fpt.qn.mes.bom.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import fpt.qn.mes.auth.application.port.out.CurrentUserPort;
import fpt.qn.mes.bom.application.dto.request.CreateBomItemRequest;
import fpt.qn.mes.bom.application.dto.request.CreateBomRequest;
import fpt.qn.mes.bom.application.dto.response.BomDto;
import fpt.qn.mes.bom.application.exception.BomAlreadyExistsException;
import fpt.qn.mes.bom.application.exception.BomNotFoundException;
import fpt.qn.mes.bom.application.exception.EmptyBomException;
import fpt.qn.mes.bom.application.exception.InvalidBomStatusException;
import fpt.qn.mes.bom.application.mapper.BomDtoMapper;
import fpt.qn.mes.bom.domain.entities.Bom;
import fpt.qn.mes.bom.domain.entities.BomItem;
import fpt.qn.mes.bom.domain.repository.BomRepository;
import fpt.qn.mes.common.dto.response.PageResponse;
import fpt.qn.mes.common.dto.response.PaginationResult;
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
    UUID draftStatusId;
    UUID activeStatusId;
    UUID inactiveStatusId;

    @BeforeEach
    void setUp() {
        finishedProductId = UUID.randomUUID();
        userId = UUID.randomUUID();
        draftStatusId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        activeStatusId = UUID.fromString("00000000-0000-0000-0000-000000000002");
        inactiveStatusId = UUID.fromString("00000000-0000-0000-0000-000000000003");
    }

    @Test
    void getBoms_WithFilters_ReturnsFilteredPageResponse() {
        Bom bom = Bom.builder()
                .id(UUID.randomUUID())
                .finishedProductId(finishedProductId)
                .version(1)
                .bomStatusId(activeStatusId)
                .createdBy(userId)
                .createdAt(Instant.now())
                .items(Collections.emptyList())
                .build();

        BomDto bomDto = BomDto.builder()
                .id(bom.getId())
                .finishedProductId(finishedProductId)
                .version(1)
                .bomStatusId(activeStatusId)
                .createdBy(userId)
                .createdAt(bom.getCreatedAt())
                .items(Collections.emptyList())
                .build();

        PaginationResult<Bom> paginationResult = new PaginationResult<>(1, List.of(bom));
        when(bomRepository.findAll(0, 10, finishedProductId, activeStatusId)).thenReturn(paginationResult);
        when(mapper.toDto(bom)).thenReturn(bomDto);

        PageResponse<BomDto> response = bomService.getBoms(0, 10, finishedProductId, activeStatusId);

        assertNotNull(response);
        assertEquals(1, response.getTotalElements());
        assertEquals(1, response.getItems().size());
        assertEquals(bom.getId(), response.getItems().get(0).getId());
    }

    @Test
    void createBom_HappyPath_Success() {
        CreateBomRequest request = new CreateBomRequest();
        request.setFinishedProductId(finishedProductId);
        request.setVersion(1);

        ProductDto productDto = ProductDto.builder().id(finishedProductId).build();
        when(productUseCase.getProductById(finishedProductId)).thenReturn(productDto);
        when(bomRepository.existsByFinishedProductIdAndVersion(finishedProductId, 1)).thenReturn(false);
        when(bomRepository.findStatusIdByName("DRAFT")).thenReturn(Optional.of(draftStatusId));
        when(currentUserPort.getCurrentUserId()).thenReturn(userId);

        Bom savedBom = Bom.builder()
                .id(UUID.randomUUID())
                .finishedProductId(finishedProductId)
                .version(1)
                .bomStatusId(draftStatusId)
                .createdBy(userId)
                .createdAt(Instant.now())
                .items(Collections.emptyList())
                .build();

        BomDto bomDto = BomDto.builder()
                .id(savedBom.getId())
                .finishedProductId(finishedProductId)
                .version(1)
                .bomStatusId(draftStatusId)
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
    void getBomById_HappyPath_ReturnsBomWithItems() {
        UUID bomId = UUID.randomUUID();
        Bom bom = Bom.builder()
                .id(bomId)
                .finishedProductId(finishedProductId)
                .version(1)
                .bomStatusId(activeStatusId)
                .createdBy(userId)
                .createdAt(Instant.now())
                .items(Collections.emptyList())
                .build();

        BomDto bomDto = BomDto.builder()
                .id(bomId)
                .finishedProductId(finishedProductId)
                .version(1)
                .bomStatusId(activeStatusId)
                .createdBy(userId)
                .createdAt(bom.getCreatedAt())
                .items(Collections.emptyList())
                .build();

        when(bomRepository.findById(bomId)).thenReturn(Optional.of(bom));
        when(mapper.toDto(bom)).thenReturn(bomDto);

        BomDto result = bomService.getBomById(bomId);

        assertNotNull(result);
        assertEquals(bomId, result.getId());
    }

    @Test
    void getBomById_NotFound_ThrowsBomNotFoundException() {
        UUID randomId = UUID.randomUUID();
        when(bomRepository.findById(randomId)).thenReturn(Optional.empty());

        assertThrows(BomNotFoundException.class, () -> bomService.getBomById(randomId));
    }

    @Test
    void activateBom_HappyPath_Success() {
        UUID bomId = UUID.randomUUID();
        Bom draftBom = Bom.builder()
                .id(bomId)
                .finishedProductId(finishedProductId)
                .version(1)
                .bomStatusId(draftStatusId)
                .createdBy(userId)
                .createdAt(Instant.now())
                .items(Collections.emptyList())
                .build();

        Bom activeBom = Bom.builder()
                .id(bomId)
                .finishedProductId(finishedProductId)
                .version(1)
                .bomStatusId(activeStatusId)
                .createdBy(userId)
                .createdAt(draftBom.getCreatedAt())
                .items(Collections.emptyList())
                .build();

        BomDto activeBomDto = BomDto.builder()
                .id(bomId)
                .finishedProductId(finishedProductId)
                .version(1)
                .bomStatusId(activeStatusId)
                .createdBy(userId)
                .createdAt(draftBom.getCreatedAt())
                .items(Collections.emptyList())
                .build();

        when(bomRepository.findById(bomId)).thenReturn(Optional.of(draftBom));
        when(bomRepository.findStatusIdByName("DRAFT")).thenReturn(Optional.of(draftStatusId));
        when(bomRepository.findStatusIdByName("ACTIVE")).thenReturn(Optional.of(activeStatusId));
        when(bomRepository.findStatusIdByName("INACTIVE")).thenReturn(Optional.of(inactiveStatusId));
        when(bomRepository.countItemsByBomId(bomId)).thenReturn(2);
        when(bomRepository.save(any(Bom.class))).thenReturn(activeBom);
        when(mapper.toDto(activeBom)).thenReturn(activeBomDto);

        BomDto result = bomService.activateBom(bomId);

        assertNotNull(result);
        assertEquals(activeStatusId, result.getBomStatusId());
        verify(bomRepository).deactivateActiveBomsForProduct(finishedProductId, activeStatusId, inactiveStatusId);
        verify(bomRepository).save(draftBom);
    }

    @Test
    void activateBom_EmptyBom_ThrowsEmptyBomException() {
        UUID bomId = UUID.randomUUID();
        Bom draftBom = Bom.builder()
                .id(bomId)
                .finishedProductId(finishedProductId)
                .version(1)
                .bomStatusId(draftStatusId)
                .createdBy(userId)
                .createdAt(Instant.now())
                .items(Collections.emptyList())
                .build();

        when(bomRepository.findById(bomId)).thenReturn(Optional.of(draftBom));
        when(bomRepository.findStatusIdByName("DRAFT")).thenReturn(Optional.of(draftStatusId));
        when(bomRepository.findStatusIdByName("ACTIVE")).thenReturn(Optional.of(activeStatusId));
        when(bomRepository.findStatusIdByName("INACTIVE")).thenReturn(Optional.of(inactiveStatusId));
        when(bomRepository.countItemsByBomId(bomId)).thenReturn(0);

        assertThrows(EmptyBomException.class, () -> bomService.activateBom(bomId));
        verify(bomRepository, never()).deactivateActiveBomsForProduct(any(), any(), any());
        verify(bomRepository, never()).save(any(Bom.class));
    }

    @Test
    void activateBom_NonDraftStatus_ThrowsInvalidBomStatusException() {
        UUID bomId = UUID.randomUUID();
        Bom activeBom = Bom.builder()
                .id(bomId)
                .finishedProductId(finishedProductId)
                .version(1)
                .bomStatusId(activeStatusId)
                .createdBy(userId)
                .createdAt(Instant.now())
                .items(Collections.emptyList())
                .build();

        when(bomRepository.findById(bomId)).thenReturn(Optional.of(activeBom));
        when(bomRepository.findStatusIdByName("DRAFT")).thenReturn(Optional.of(draftStatusId));
        when(bomRepository.findStatusIdByName("ACTIVE")).thenReturn(Optional.of(activeStatusId));
        when(bomRepository.findStatusIdByName("INACTIVE")).thenReturn(Optional.of(inactiveStatusId));

        assertThrows(InvalidBomStatusException.class, () -> bomService.activateBom(bomId));
        verify(bomRepository, never()).save(any(Bom.class));
    }

    @Test
    void createNewVersion_HappyPath_Success() {
        UUID sourceBomId = UUID.randomUUID();
        BomItem sourceItem = BomItem.builder()
                .id(UUID.randomUUID())
                .bomId(sourceBomId)
                .materialProductId(UUID.randomUUID())
                .quantityPerUnit(new BigDecimal("2.50"))
                .unit("PCS")
                .scrapRate(new BigDecimal("0.02"))
                .build();

        List<BomItem> sourceItems = new ArrayList<>();
        sourceItems.add(sourceItem);

        Bom sourceBom = Bom.builder()
                .id(sourceBomId)
                .finishedProductId(finishedProductId)
                .version(1)
                .bomStatusId(activeStatusId)
                .createdBy(userId)
                .createdAt(Instant.now())
                .items(sourceItems)
                .build();

        when(bomRepository.findById(sourceBomId)).thenReturn(Optional.of(sourceBom));
        when(bomRepository.findStatusIdByName("DRAFT")).thenReturn(Optional.of(draftStatusId));
        when(bomRepository.findMaxVersionByFinishedProductId(finishedProductId)).thenReturn(1);
        when(currentUserPort.getCurrentUserId()).thenReturn(userId);

        Bom clonedBom = Bom.builder()
                .id(UUID.randomUUID())
                .finishedProductId(finishedProductId)
                .version(2)
                .bomStatusId(draftStatusId)
                .createdBy(userId)
                .createdAt(Instant.now())
                .items(Collections.emptyList())
                .build();

        BomDto clonedBomDto = BomDto.builder()
                .id(clonedBom.getId())
                .finishedProductId(finishedProductId)
                .version(2)
                .bomStatusId(draftStatusId)
                .createdBy(userId)
                .createdAt(clonedBom.getCreatedAt())
                .items(Collections.emptyList())
                .build();

        when(bomRepository.save(any(Bom.class))).thenReturn(clonedBom);
        when(mapper.toDto(clonedBom)).thenReturn(clonedBomDto);

        BomDto result = bomService.createNewVersion(sourceBomId);

        assertNotNull(result);
        assertEquals(2, result.getVersion());
        assertEquals(draftStatusId, result.getBomStatusId());
        verify(bomRepository).save(any(Bom.class));
    }

    @Test
    void createNewVersion_SourceNotFound_ThrowsBomNotFoundException() {
        UUID randomId = UUID.randomUUID();
        when(bomRepository.findById(randomId)).thenReturn(Optional.empty());

        assertThrows(BomNotFoundException.class, () -> bomService.createNewVersion(randomId));
        verify(bomRepository, never()).save(any(Bom.class));
    }

    @Test
    void addBomItem_HappyPath_Success() {
        UUID bomId = UUID.randomUUID();
        UUID materialProductId = UUID.randomUUID();

        Bom draftBom = Bom.builder()
                .id(bomId)
                .finishedProductId(finishedProductId)
                .version(1)
                .bomStatusId(draftStatusId)
                .createdBy(userId)
                .createdAt(Instant.now())
                .items(Collections.emptyList())
                .build();

        CreateBomItemRequest request = new CreateBomItemRequest();
        request.setMaterialProductId(materialProductId);
        request.setQuantityPerUnit(new BigDecimal("2.50"));
        request.setUnit("PCS");
        request.setScrapRate(new BigDecimal("0.01"));

        BomItem savedItem = BomItem.builder()
                .id(UUID.randomUUID())
                .bomId(bomId)
                .materialProductId(materialProductId)
                .quantityPerUnit(new BigDecimal("2.50"))
                .unit("PCS")
                .scrapRate(new BigDecimal("0.01"))
                .build();

        fpt.qn.mes.bom.application.dto.response.BomItemDto itemDto = fpt.qn.mes.bom.application.dto.response.BomItemDto.builder()
                .id(savedItem.getId())
                .bomId(bomId)
                .materialProductId(materialProductId)
                .quantityPerUnit(new BigDecimal("2.50"))
                .unit("PCS")
                .scrapRate(new BigDecimal("0.01"))
                .build();

        when(bomRepository.findById(bomId)).thenReturn(Optional.of(draftBom));
        when(bomRepository.findStatusIdByName("DRAFT")).thenReturn(Optional.of(draftStatusId));
        when(bomRepository.saveItem(any(BomItem.class))).thenReturn(savedItem);
        when(mapper.toDto(savedItem)).thenReturn(itemDto);

        fpt.qn.mes.bom.application.dto.response.BomItemDto result = bomService.addBomItem(bomId, request);

        assertNotNull(result);
        assertEquals(materialProductId, result.getMaterialProductId());
        assertEquals(new BigDecimal("2.50"), result.getQuantityPerUnit());
        verify(bomRepository).saveItem(any(BomItem.class));
    }

    @Test
    void addBomItem_BomNotFound_ThrowsBomNotFoundException() {
        UUID randomId = UUID.randomUUID();
        CreateBomItemRequest request = new CreateBomItemRequest();
        when(bomRepository.findById(randomId)).thenReturn(Optional.empty());

        assertThrows(BomNotFoundException.class, () -> bomService.addBomItem(randomId, request));
        verify(bomRepository, never()).saveItem(any(BomItem.class));
    }

    @Test
    void addBomItem_NonDraftBom_ThrowsInvalidBomStatusException() {
        UUID bomId = UUID.randomUUID();
        Bom activeBom = Bom.builder()
                .id(bomId)
                .finishedProductId(finishedProductId)
                .version(1)
                .bomStatusId(activeStatusId)
                .createdBy(userId)
                .createdAt(Instant.now())
                .items(Collections.emptyList())
                .build();

        when(bomRepository.findById(bomId)).thenReturn(Optional.of(activeBom));
        when(bomRepository.findStatusIdByName("DRAFT")).thenReturn(Optional.of(draftStatusId));

        CreateBomItemRequest request = new CreateBomItemRequest();
        assertThrows(InvalidBomStatusException.class, () -> bomService.addBomItem(bomId, request));
        verify(bomRepository, never()).saveItem(any(BomItem.class));
    }

    @Test
    void deleteBomItem_HappyPath_Success() {
        UUID bomId = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();
        Bom draftBom = Bom.builder()
                .id(bomId)
                .finishedProductId(finishedProductId)
                .version(1)
                .bomStatusId(draftStatusId)
                .createdBy(userId)
                .createdAt(Instant.now())
                .items(Collections.emptyList())
                .build();

        when(bomRepository.findById(bomId)).thenReturn(Optional.of(draftBom));
        when(bomRepository.findStatusIdByName("DRAFT")).thenReturn(Optional.of(draftStatusId));

        bomService.deleteBomItem(bomId, itemId);

        verify(bomRepository).deleteItemById(itemId);
    }

    @Test
    void deleteBomItem_BomNotFound_ThrowsBomNotFoundException() {
        UUID randomId = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();
        when(bomRepository.findById(randomId)).thenReturn(Optional.empty());

        assertThrows(BomNotFoundException.class, () -> bomService.deleteBomItem(randomId, itemId));
        verify(bomRepository, never()).deleteItemById(itemId);
    }

    @Test
    void deleteBomItem_NonDraftBom_ThrowsInvalidBomStatusException() {
        UUID bomId = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();
        Bom activeBom = Bom.builder()
                .id(bomId)
                .finishedProductId(finishedProductId)
                .version(1)
                .bomStatusId(activeStatusId)
                .createdBy(userId)
                .createdAt(Instant.now())
                .items(Collections.emptyList())
                .build();

        when(bomRepository.findById(bomId)).thenReturn(Optional.of(activeBom));
        when(bomRepository.findStatusIdByName("DRAFT")).thenReturn(Optional.of(draftStatusId));

        assertThrows(InvalidBomStatusException.class, () -> bomService.deleteBomItem(bomId, itemId));
        verify(bomRepository, never()).deleteItemById(itemId);
    }
}
