package fpt.qn.mes.quality.application.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fpt.qn.mes.common.dto.PageResponse;
import fpt.qn.mes.quality.application.dto.CreateInspectionResultRequest;
import fpt.qn.mes.quality.application.dto.CreateQualityInspectionRequest;
import fpt.qn.mes.quality.application.dto.QualityInspectionDto;
import fpt.qn.mes.quality.application.dto.QualityInspectionResultDto;
import fpt.qn.mes.quality.application.mapper.QualityDtoMapper;
import fpt.qn.mes.quality.application.port.in.QualityUseCase;
import fpt.qn.mes.quality.domain.repository.QualityInspectionRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class QualityService implements QualityUseCase {

    QualityInspectionRepository repository;
    QualityDtoMapper mapper;

    @Override @Transactional(readOnly = true)
    public PageResponse<QualityInspectionDto> getInspections(int page, int size) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override @Transactional(readOnly = true)
    public QualityInspectionDto getInspectionById(UUID id) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override @Transactional
    public QualityInspectionDto createInspection(CreateQualityInspectionRequest req) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override @Transactional
    public void deleteInspection(UUID id) {}

    @Override @Transactional(readOnly = true)
    public PageResponse<QualityInspectionResultDto> getResults(UUID inspectionId, int page, int size) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override @Transactional
    public QualityInspectionResultDto addResult(UUID inspectionId, CreateInspectionResultRequest req) {
        throw new UnsupportedOperationException("Not implemented");
    }
}
