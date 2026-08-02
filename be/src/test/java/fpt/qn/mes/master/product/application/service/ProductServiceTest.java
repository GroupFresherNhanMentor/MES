package fpt.qn.mes.master.product.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.context.ApplicationEventPublisher;
import fpt.qn.mes.auth.application.port.out.CurrentUserPort;
import fpt.qn.mes.common.port.out.JsonSerializerPort;
import fpt.qn.mes.master.product.application.dto.product.ProductResponse;
import fpt.qn.mes.master.product.application.dto.product.create.CreateProductRequest;
import fpt.qn.mes.master.product.application.dto.product.update.UpdateProductRequest;
import fpt.qn.mes.master.product.application.exception.ProductConflictException;
import fpt.qn.mes.master.product.application.exception.ProductNotFoundException;
import fpt.qn.mes.master.product.application.exception.ProductStatusNotFoundException;
import fpt.qn.mes.master.product.application.exception.ProductTypeNotFoundException;
import fpt.qn.mes.master.product.application.exception.UnitOfMeasureNotFoundException;
import fpt.qn.mes.master.product.application.mapper.ProductDtoMapper;
import fpt.qn.mes.master.product.application.port.out.MovementStockCheckPort;
import fpt.qn.mes.master.product.domain.constants.ProductStatusConstants;
import fpt.qn.mes.master.product.domain.entities.Product;
import fpt.qn.mes.master.product.domain.entities.ProductStatus;
import fpt.qn.mes.master.product.domain.repository.ProductRepository;
import fpt.qn.mes.master.product.domain.repository.ProductStatusRepository;
import fpt.qn.mes.master.product.domain.repository.ProductTypeRepository;
import fpt.qn.mes.master.product.domain.repository.UnitOfMeasureRepository;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock ProductRepository productRepository;
    @Mock ProductStatusRepository productStatusRepository;
    @Mock ProductTypeRepository productTypeRepository;
    @Mock UnitOfMeasureRepository unitOfMeasureRepository;
    @Mock MovementStockCheckPort movementStockCheckPort;
    @Mock ProductDtoMapper mapper;
    @Mock CurrentUserPort currentUserPort;
    @Mock ApplicationEventPublisher eventPublisher;
    @Mock JsonSerializerPort jsonSerializer;
    @InjectMocks ProductService productService;

    UUID productId;
    UUID userId;
    UUID typeId;
    UUID statusId;
    UUID unitId;
    Product product;
    ProductResponse dto;
    CreateProductRequest createReq;

    @BeforeEach
    void setUp() {
        productId = UUID.randomUUID();
        userId = UUID.randomUUID();
        typeId = UUID.randomUUID();
        statusId = UUID.randomUUID();
        unitId = UUID.randomUUID();

        product = Product.create("TEST-001", "Test Product", "v1.0", typeId, unitId, statusId, userId);
        dto = ProductResponse.builder().id(product.getId()).code("TEST-001").name("Test Product").build();

        createReq = new CreateProductRequest();
        createReq.setCode("TEST-001");
        createReq.setName("Test Product");
        createReq.setVersion("v1.0");
        createReq.setProductTypeId(typeId);
        createReq.setUnitId(unitId);
    }

    // ── createProduct ─────────────────────────────────────────────────────────

    @Test
    void createProduct_savesProduct_whenAllRefsAreValid() {
        when(productRepository.existsByCode("TEST-001")).thenReturn(false);
        when(productRepository.existsByVersion("v1.0")).thenReturn(false);
        when(productTypeRepository.existsById(typeId)).thenReturn(true);
        when(unitOfMeasureRepository.existsById(unitId)).thenReturn(true);
        when(productStatusRepository.findByName(ProductStatusConstants.ACTIVE))
                .thenReturn(Optional.of(ProductStatus.builder().id(statusId).name("ACTIVE").build()));
        when(currentUserPort.getCurrentUserId()).thenReturn(userId);
        when(productRepository.save(any())).thenReturn(product);

        productService.createProduct(createReq);

        verify(productRepository).save(any());
    }

    @Test
    void createProduct_throwsConflict_whenCodeAlreadyExists() {
        when(productRepository.existsByCode("TEST-001")).thenReturn(true);

        assertThrows(ProductConflictException.class, () -> productService.createProduct(createReq));
        verify(productRepository, never()).save(any());
    }

    @Test
    void createProduct_throwsConflict_whenVersionAlreadyExists() {
        when(productRepository.existsByCode("TEST-001")).thenReturn(false);
        when(productRepository.existsByVersion("v1.0")).thenReturn(true);

        assertThrows(ProductConflictException.class, () -> productService.createProduct(createReq));
        verify(productRepository, never()).save(any());
    }

    @Test
    void createProduct_throwsNotFound_whenProductTypeDoesNotExist() {
        when(productRepository.existsByCode("TEST-001")).thenReturn(false);
        when(productRepository.existsByVersion("v1.0")).thenReturn(false);
        when(productTypeRepository.existsById(typeId)).thenReturn(false);

        assertThrows(ProductTypeNotFoundException.class, () -> productService.createProduct(createReq));
        verify(productRepository, never()).save(any());
    }

    @Test
    void createProduct_throwsNotFound_whenUnitOfMeasureDoesNotExist() {
        when(productRepository.existsByCode("TEST-001")).thenReturn(false);
        when(productRepository.existsByVersion("v1.0")).thenReturn(false);
        when(productTypeRepository.existsById(typeId)).thenReturn(true);
        when(unitOfMeasureRepository.existsById(unitId)).thenReturn(false);

        assertThrows(UnitOfMeasureNotFoundException.class, () -> productService.createProduct(createReq));
        verify(productRepository, never()).save(any());
    }

    @Test
    void createProduct_throwsNotFound_whenActiveStatusMissing() {
        when(productRepository.existsByCode("TEST-001")).thenReturn(false);
        when(productRepository.existsByVersion("v1.0")).thenReturn(false);
        when(productTypeRepository.existsById(typeId)).thenReturn(true);
        when(unitOfMeasureRepository.existsById(unitId)).thenReturn(true);
        when(productStatusRepository.findByName(ProductStatusConstants.ACTIVE)).thenReturn(Optional.empty());

        assertThrows(ProductStatusNotFoundException.class, () -> productService.createProduct(createReq));
        verify(productRepository, never()).save(any());
    }

    // ── getProductById ────────────────────────────────────────────────────────

    @Test
    void getProductById_returnsDto_whenProductExists() {
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(mapper.toDto(product)).thenReturn(dto);

        var result = productService.getProductById(productId);

        assertThat(result.getCode()).isEqualTo("TEST-001");
    }

    @Test
    void getProductById_throwsNotFound_whenProductDoesNotExist() {
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> productService.getProductById(productId));
    }

    // ── updateProduct ─────────────────────────────────────────────────────────

    @Test
    void updateProduct_updates_whenExists() {
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(movementStockCheckPort.hasStockMovements(productId)).thenReturn(false);
        when(currentUserPort.getCurrentUserId()).thenReturn(userId);
        when(productRepository.update(any())).thenReturn(product);

        var req = new UpdateProductRequest();
        req.setName("Updated Name");

        productService.updateProduct(productId, req);

        verify(productRepository).update(any());
    }

    @Test
    void updateProduct_throwsNotFound_whenProductDoesNotExist() {
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class,
            () -> productService.updateProduct(productId, new UpdateProductRequest()));
    }

    @Test
    void updateProduct_throwsConflict_whenChangingTypeWithExistingMovements() {
        UUID newTypeId = UUID.randomUUID();
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(movementStockCheckPort.hasStockMovements(productId)).thenReturn(true);

        var req = new UpdateProductRequest();
        req.setProductTypeId(newTypeId);

        assertThrows(ProductConflictException.class, () -> productService.updateProduct(productId, req));
        verify(productRepository, never()).update(any());
    }

    @Test
    void updateProduct_throwsConflict_whenChangingUnitWithExistingMovements() {
        UUID newUnitId = UUID.randomUUID();
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(movementStockCheckPort.hasStockMovements(productId)).thenReturn(true);

        var req = new UpdateProductRequest();
        req.setUnitId(newUnitId);

        assertThrows(ProductConflictException.class, () -> productService.updateProduct(productId, req));
        verify(productRepository, never()).update(any());
    }

    @Test
    void updateProduct_throwsNotFound_whenNewTypeDoesNotExist() {
        UUID newTypeId = UUID.randomUUID();
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(movementStockCheckPort.hasStockMovements(productId)).thenReturn(false);
        when(productTypeRepository.existsById(newTypeId)).thenReturn(false);

        var req = new UpdateProductRequest();
        req.setProductTypeId(newTypeId);

        assertThrows(ProductTypeNotFoundException.class, () -> productService.updateProduct(productId, req));
        verify(productRepository, never()).update(any());
    }

    @Test
    void updateProduct_allowsTypeChange_whenNoMovementsExist() {
        UUID newTypeId = UUID.randomUUID();
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(movementStockCheckPort.hasStockMovements(productId)).thenReturn(false);
        when(productTypeRepository.existsById(newTypeId)).thenReturn(true);
        when(currentUserPort.getCurrentUserId()).thenReturn(userId);
        when(productRepository.update(any())).thenReturn(product);

        var req = new UpdateProductRequest();
        req.setProductTypeId(newTypeId);

        productService.updateProduct(productId, req);

        verify(productRepository).update(any());
    }

    // ── activateProduct ───────────────────────────────────────────────────────

    @Test
    void activateProduct_changesStatus_whenProductExists() {
        ProductStatus activeStatus = ProductStatus.builder().id(UUID.randomUUID()).name("ACTIVE").build();
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productStatusRepository.findByName("ACTIVE")).thenReturn(Optional.of(activeStatus));
        when(currentUserPort.getCurrentUserId()).thenReturn(userId);

        productService.activateProduct(productId);

        verify(productRepository).update(any());
    }

    @Test
    void activateProduct_throwsNotFound_whenProductDoesNotExist() {
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> productService.activateProduct(productId));
        verify(productRepository, never()).update(any());
    }

    // ── deactivateProduct ─────────────────────────────────────────────────────

    @Test
    void deactivateProduct_changesStatus_whenProductExists() {
        ProductStatus inactiveStatus = ProductStatus.builder().id(UUID.randomUUID()).name("INACTIVE").build();
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productStatusRepository.findByName("INACTIVE")).thenReturn(Optional.of(inactiveStatus));
        when(currentUserPort.getCurrentUserId()).thenReturn(userId);

        productService.deactivateProduct(productId);

        verify(productRepository).update(any());
    }

    @Test
    void deactivateProduct_throwsNotFound_whenProductDoesNotExist() {
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> productService.deactivateProduct(productId));
        verify(productRepository, never()).update(any());
    }
}
