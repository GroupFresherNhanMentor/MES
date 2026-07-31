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
import fpt.qn.mes.master.product.application.dto.productstatus.ProductStatusResponse;
import fpt.qn.mes.master.product.application.dto.productstatus.create.CreateProductStatusRequest;
import fpt.qn.mes.master.product.application.dto.productstatus.search.ProductStatusSearchRequest;
import fpt.qn.mes.master.product.application.exception.ProductConflictException;
import fpt.qn.mes.master.product.application.mapper.ProductStatusDtoMapper;
import fpt.qn.mes.master.product.domain.entities.ProductStatus;
import fpt.qn.mes.master.product.domain.repository.ProductStatusRepository;

@ExtendWith(MockitoExtension.class)
class ProductStatusServiceTest {

    @Mock ProductStatusRepository productStatusRepository;
    @Mock ProductStatusDtoMapper mapper;
    @Mock CurrentUserPort currentUserPort;
    @InjectMocks ProductStatusService productStatusService;

    @Test
    void getProductStatuses_returnsPage_whenDataExists() {
        UUID id = UUID.randomUUID();
        ProductStatus status = ProductStatus.builder().id(id).name("ACTIVE").description("Active").build();
        ProductStatusResponse response = ProductStatusResponse.builder().id(id).name("ACTIVE").build();
        PaginationResult<ProductStatus> result = PaginationResult.<ProductStatus>builder()
            .total(1L).items(List.of(status)).build();

        when(productStatusRepository.search(any())).thenReturn(result);
        when(mapper.toDto(status)).thenReturn(response);

        PageResponse<ProductStatusResponse> page = productStatusService.getProductStatuses(new ProductStatusSearchRequest());

        assertThat(page.getTotalElements()).isEqualTo(1L);
        assertThat(page.getItems()).hasSize(1);
        assertThat(page.getItems().get(0).getName()).isEqualTo("ACTIVE");
    }

    @Test
    void getProductStatuses_returnsEmptyPage_whenNoData() {
        PaginationResult<ProductStatus> result = PaginationResult.<ProductStatus>builder()
            .total(0L).items(List.of()).build();

        when(productStatusRepository.search(any())).thenReturn(result);

        PageResponse<ProductStatusResponse> page = productStatusService.getProductStatuses(new ProductStatusSearchRequest());

        assertThat(page.getTotalElements()).isEqualTo(0L);
        assertThat(page.getItems()).isEmpty();
    }

    @Test
    void createProductStatus_savesEntity_whenValid() {
        UUID userId = UUID.randomUUID();
        when(currentUserPort.getCurrentUserId()).thenReturn(userId);
        when(productStatusRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CreateProductStatusRequest request = new CreateProductStatusRequest();
        request.setName("ACTIVE");
        request.setDescription("Active status");

        productStatusService.createProductStatus(request);

        verify(productStatusRepository).save(any(ProductStatus.class));
    }

    @Test
    void createProductStatus_throwsConflict_whenNameAlreadyExists() {
        when(productStatusRepository.existsByName("ACTIVE")).thenReturn(true);

        CreateProductStatusRequest request = new CreateProductStatusRequest();
        request.setName("ACTIVE");

        assertThrows(ProductConflictException.class, () -> productStatusService.createProductStatus(request));
        verify(productStatusRepository, never()).save(any());
    }
}
