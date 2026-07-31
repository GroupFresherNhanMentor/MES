package fpt.qn.mes.master.product.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import fpt.qn.mes.auth.application.port.out.CurrentUserPort;
import fpt.qn.mes.common.domainQuery.PaginationResult;
import fpt.qn.mes.common.dto.response.PageResponse;
import fpt.qn.mes.master.product.application.dto.producttype.ProductTypeResponse;
import fpt.qn.mes.master.product.application.dto.producttype.create.CreateProductTypeRequest;
import fpt.qn.mes.master.product.application.dto.producttype.search.ProductTypeSearchRequest;
import fpt.qn.mes.master.product.application.exception.ProductConflictException;
import fpt.qn.mes.master.product.application.mapper.ProductTypeDtoMapper;
import fpt.qn.mes.master.product.domain.entities.ProductType;
import fpt.qn.mes.master.product.domain.repository.ProductTypeRepository;

@ExtendWith(MockitoExtension.class)
class ProductTypeServiceTest {

    @Mock ProductTypeRepository productTypeRepository;
    @Mock ProductTypeDtoMapper mapper;
    @Mock CurrentUserPort currentUserPort;
    @InjectMocks ProductTypeService productTypeService;

    @Test
    void getProductTypes_returnsPage_whenDataExists() {
        UUID id = UUID.randomUUID();
        ProductType type = ProductType.builder().id(id).name("RAW_MATERIAL").description("Raw material").build();
        ProductTypeResponse response = ProductTypeResponse.builder().id(id).name("RAW_MATERIAL").build();
        PaginationResult<ProductType> result = PaginationResult.<ProductType>builder()
            .total(1L).items(List.of(type)).build();

        when(productTypeRepository.search(any())).thenReturn(result);
        when(mapper.toDto(type)).thenReturn(response);

        PageResponse<ProductTypeResponse> page = productTypeService.getProductTypes(new ProductTypeSearchRequest());

        assertThat(page.getTotalElements()).isEqualTo(1L);
        assertThat(page.getItems()).hasSize(1);
        assertThat(page.getItems().get(0).getName()).isEqualTo("RAW_MATERIAL");
    }

    @Test
    void getProductTypes_returnsEmptyPage_whenNoData() {
        PaginationResult<ProductType> result = PaginationResult.<ProductType>builder()
            .total(0L).items(List.of()).build();

        when(productTypeRepository.search(any())).thenReturn(result);

        PageResponse<ProductTypeResponse> page = productTypeService.getProductTypes(new ProductTypeSearchRequest());

        assertThat(page.getTotalElements()).isEqualTo(0L);
        assertThat(page.getItems()).isEmpty();
    }

    @Test
    void createProductType_savesEntity_whenValid() {
        UUID userId = UUID.randomUUID();
        when(currentUserPort.getCurrentUserId()).thenReturn(userId);
        when(productTypeRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CreateProductTypeRequest request = new CreateProductTypeRequest();
        request.setName("RAW_MATERIAL");
        request.setDescription("Raw material type");

        productTypeService.createProductType(request);

        verify(productTypeRepository).save(any(ProductType.class));
    }

    @Test
    void createProductType_throwsConflict_whenNameAlreadyExists() {
        when(productTypeRepository.existsByName("RAW_MATERIAL")).thenReturn(true);

        CreateProductTypeRequest request = new CreateProductTypeRequest();
        request.setName("RAW_MATERIAL");

        assertThrows(ProductConflictException.class, () -> productTypeService.createProductType(request));
        verify(productTypeRepository, never()).save(any());
    }
}
