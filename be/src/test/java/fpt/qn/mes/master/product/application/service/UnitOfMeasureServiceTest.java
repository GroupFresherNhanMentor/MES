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
import fpt.qn.mes.master.product.application.dto.unitofmeasure.UnitOfMeasureResponse;
import fpt.qn.mes.master.product.application.dto.unitofmeasure.create.CreateUnitOfMeasureRequest;
import fpt.qn.mes.master.product.application.dto.unitofmeasure.search.UnitOfMeasureSearchRequest;
import fpt.qn.mes.master.product.application.exception.ProductConflictException;
import fpt.qn.mes.master.product.application.mapper.UnitOfMeasureDtoMapper;
import fpt.qn.mes.master.product.domain.entities.UnitOfMeasure;
import fpt.qn.mes.master.product.domain.repository.UnitOfMeasureRepository;

@ExtendWith(MockitoExtension.class)
class UnitOfMeasureServiceTest {

    @Mock UnitOfMeasureRepository unitOfMeasureRepository;
    @Mock UnitOfMeasureDtoMapper mapper;
    @Mock CurrentUserPort currentUserPort;
    @InjectMocks UnitOfMeasureService unitOfMeasureService;

    @Test
    void getUnitsOfMeasure_returnsPage_whenDataExists() {
        UUID id = UUID.randomUUID();
        UnitOfMeasure unit = UnitOfMeasure.builder().id(id).name("KG").description("Kilogram").build();
        UnitOfMeasureResponse response = UnitOfMeasureResponse.builder().id(id).name("KG").build();
        PaginationResult<UnitOfMeasure> result = PaginationResult.<UnitOfMeasure>builder()
            .total(1L).items(List.of(unit)).build();

        when(unitOfMeasureRepository.search(any())).thenReturn(result);
        when(mapper.toDto(unit)).thenReturn(response);

        PageResponse<UnitOfMeasureResponse> page = unitOfMeasureService.getUnitsOfMeasure(new UnitOfMeasureSearchRequest());

        assertThat(page.getTotalElements()).isEqualTo(1L);
        assertThat(page.getItems()).hasSize(1);
        assertThat(page.getItems().get(0).getName()).isEqualTo("KG");
    }

    @Test
    void getUnitsOfMeasure_returnsEmptyPage_whenNoData() {
        PaginationResult<UnitOfMeasure> result = PaginationResult.<UnitOfMeasure>builder()
            .total(0L).items(List.of()).build();

        when(unitOfMeasureRepository.search(any())).thenReturn(result);

        PageResponse<UnitOfMeasureResponse> page = unitOfMeasureService.getUnitsOfMeasure(new UnitOfMeasureSearchRequest());

        assertThat(page.getTotalElements()).isEqualTo(0L);
        assertThat(page.getItems()).isEmpty();
    }

    @Test
    void createUnitOfMeasure_savesEntity_whenValid() {
        UUID userId = UUID.randomUUID();
        when(currentUserPort.getCurrentUserId()).thenReturn(userId);
        when(unitOfMeasureRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CreateUnitOfMeasureRequest request = new CreateUnitOfMeasureRequest();
        request.setName("KG");
        request.setDescription("Kilogram");

        unitOfMeasureService.createUnitOfMeasure(request);

        verify(unitOfMeasureRepository).save(any(UnitOfMeasure.class));
    }

    @Test
    void createUnitOfMeasure_throwsConflict_whenNameAlreadyExists() {
        when(unitOfMeasureRepository.existsByName("KG")).thenReturn(true);

        CreateUnitOfMeasureRequest request = new CreateUnitOfMeasureRequest();
        request.setName("KG");

        assertThrows(ProductConflictException.class, () -> unitOfMeasureService.createUnitOfMeasure(request));
        verify(unitOfMeasureRepository, never()).save(any());
    }
}
