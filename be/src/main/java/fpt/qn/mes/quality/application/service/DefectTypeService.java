package fpt.qn.mes.quality.application.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fpt.qn.mes.auth.application.port.out.CurrentUserPort;
import fpt.qn.mes.common.dto.response.PageResponse;
import fpt.qn.mes.quality.application.dto.defecttype.DefectTypeResponse;
import fpt.qn.mes.quality.application.dto.defecttype.create.CreateDefectTypeRequest;
import fpt.qn.mes.quality.application.dto.defecttype.search.DefectTypeSearchRequest;
import fpt.qn.mes.quality.application.mapper.DefectTypeDtoMapper;
import fpt.qn.mes.quality.application.port.in.DefectTypeUseCase;
import fpt.qn.mes.quality.domain.entities.DefectType;
import fpt.qn.mes.quality.domain.repository.DefectTypeRepository;
import fpt.qn.mes.quality.domain.repository.criteria.DefectTypeSearchCriteria;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DefectTypeService implements DefectTypeUseCase {

    DefectTypeRepository defectTypeRepository;
    DefectTypeDtoMapper defectTypeMapper;
    CurrentUserPort currentUserPort;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<DefectTypeResponse> getDefectTypes(DefectTypeSearchRequest request) {
        DefectTypeSearchCriteria criteria = DefectTypeSearchCriteria.builder()
            .page(request.getPage())
            .size(request.getSize())
            .sort(request.getSort())
            .name(request.getName())
            .build();

        var result = defectTypeRepository.search(criteria);
        return PageResponse.of(
            result.getItems().stream().map(d -> defectTypeMapper.toDto(d)).toList(),
            result.getTotal(), criteria.getPage(), criteria.getSize());
    }

    @Override
    @Transactional
    public void createDefectType(CreateDefectTypeRequest request) {
        UUID currentUserId = currentUserPort.getCurrentUserId();
        DefectType defectType = DefectType.builder()
            .id(UUID.randomUUID())
            .name(request.getName())
            .description(request.getDescription())
            .createdBy(currentUserId)
            .updatedBy(currentUserId)
            .build();
        defectTypeRepository.save(defectType);
    }
}
