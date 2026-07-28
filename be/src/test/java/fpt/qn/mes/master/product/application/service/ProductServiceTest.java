package fpt.qn.mes.master.product.application.service;

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

import fpt.qn.mes.master.product.application.dto.request.CreateProductRequest;
import fpt.qn.mes.master.product.application.dto.request.UpdateProductRequest;
import fpt.qn.mes.master.product.application.dto.response.ProductDto;
import fpt.qn.mes.master.product.application.exception.ProductConflictException;
import fpt.qn.mes.master.product.application.exception.ProductNotFoundException;
import fpt.qn.mes.master.product.application.mapper.ProductDtoMapper;
import fpt.qn.mes.master.product.domain.entities.Product;
import fpt.qn.mes.master.product.domain.repository.ProductRepository;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock ProductRepository productRepository;
    @Mock ProductDtoMapper mapper;
    @InjectMocks ProductService productService;

    UUID productId;
    UUID userId;
    UUID typeId;
    UUID statusId;
    UUID unitId;
    CreateProductRequest createReq;
    Product product;
    ProductDto dto;

    @BeforeEach
    void setUp() {
        productId = UUID.randomUUID();
        userId = UUID.randomUUID();
        typeId = UUID.randomUUID();
        statusId = UUID.randomUUID();
        unitId = UUID.randomUUID();

        createReq = new CreateProductRequest();
        createReq.setCode("TEST-001");
        createReq.setName("Test Product");
        createReq.setProductTypeId(typeId);
        createReq.setUnitId(unitId);
        createReq.setProductStatusId(statusId);

        product = Product.create("TEST-001", "Test Product", typeId, unitId, statusId, userId);
        dto = ProductDto.builder().id(productId).code("TEST-001").name("Test Product").build();
    }

    @Test
    void createProduct_Success() {
        when(productRepository.existsByCode("TEST-001")).thenReturn(false);
        when(productRepository.save(any())).thenReturn(product);
        when(mapper.toDto(any())).thenReturn(dto);

        var result = productService.createProduct(createReq, userId);
        assertNotNull(result);
        assertEquals("TEST-001", result.getCode());
        verify(productRepository).save(any());
    }

    @Test
    void createProduct_DuplicateCode_ThrowsConflict() {
        when(productRepository.existsByCode("TEST-001")).thenReturn(true);
        assertThrows(ProductConflictException.class, () ->
                productService.createProduct(createReq, userId));
        verify(productRepository, never()).save(any());
    }

    @Test
    void getProductById_NotFound_Throws404() {
        when(productRepository.findById(productId)).thenReturn(Optional.empty());
        assertThrows(ProductNotFoundException.class, () ->
                productService.getProductById(productId));
    }

    @Test
    void getProductById_Success() {
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(mapper.toDto(product)).thenReturn(dto);
        var result = productService.getProductById(productId);
        assertNotNull(result);
        assertEquals("TEST-001", result.getCode());
    }

    @Test
    void updateProduct_Success() {
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productRepository.update(any())).thenReturn(product);
        when(mapper.toDto(any())).thenReturn(dto);
        var req = new UpdateProductRequest();
        req.setName("Updated");
        var result = productService.updateProduct(productId, req, userId);
        assertNotNull(result);
    }
}
