package fpt.qn.mes.master.product.application.service;

import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Service;

import fpt.qn.mes.common.util.UuidV7;
import org.springframework.transaction.annotation.Transactional;

import fpt.qn.mes.auth.application.port.out.CurrentUserPort;
import fpt.qn.mes.common.dto.response.PageResponse;
import fpt.qn.mes.master.product.application.dto.unitofmeasure.UnitOfMeasureResponse;
import fpt.qn.mes.master.product.application.dto.unitofmeasure.create.CreateUnitOfMeasureRequest;
import fpt.qn.mes.master.product.application.dto.unitofmeasure.search.UnitOfMeasureSearchRequest;
import fpt.qn.mes.master.product.application.mapper.UnitOfMeasureDtoMapper;
import fpt.qn.mes.master.product.application.port.in.UnitOfMeasureUseCase;
import fpt.qn.mes.master.product.domain.entities.UnitOfMeasure;
import fpt.qn.mes.master.product.domain.repository.UnitOfMeasureRepository;
import fpt.qn.mes.master.product.domain.repository.criteria.UnitOfMeasureSearchCriteria;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UnitOfMeasureService implements UnitOfMeasureUseCase {

    UnitOfMeasureRepository unitOfMeasureRepository;
    UnitOfMeasureDtoMapper mapper;
    CurrentUserPort currentUserPort;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<UnitOfMeasureResponse> getUnitsOfMeasure(UnitOfMeasureSearchRequest request) {
        UnitOfMeasureSearchCriteria criteria = UnitOfMeasureSearchCriteria.builder()
            .page(request.getPage())
            .size(request.getSize())
            .sort(request.getSort())
            .name(request.getName())
            .build();
        var result = unitOfMeasureRepository.search(criteria);
        return PageResponse.of(
            result.getItems().stream().map(u -> mapper.toDto(u)).toList(),
            result.getTotal(), criteria.getPage(), criteria.getSize());
    }

    @Override
    @Transactional
    public void createUnitOfMeasure(CreateUnitOfMeasureRequest request) {
        UUID currentUserId = currentUserPort.getCurrentUserId();
        Instant now = Instant.now();
        UnitOfMeasure unit = UnitOfMeasure.builder()
            .id(UuidV7.generate())
            .name(request.getName())
            .description(request.getDescription())
            .createdBy(currentUserId)
            .updatedBy(currentUserId)
            .createdAt(now)
            .updatedAt(now)
            .build();
        unitOfMeasureRepository.save(unit);
    }
}
