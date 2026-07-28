package fpt.qn.mes.quality.application.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fpt.qn.mes.auth.application.port.out.CurrentUserPort;
import fpt.qn.mes.common.dto.response.PageResponse;
import fpt.qn.mes.quality.application.dto.qcstatus.QcStatusResponse;
import fpt.qn.mes.quality.application.dto.qcstatus.create.CreateQcStatusRequest;
import fpt.qn.mes.quality.application.dto.qcstatus.search.QcStatusSearchRequest;
import fpt.qn.mes.quality.application.mapper.QcStatusDtoMapper;
import fpt.qn.mes.quality.application.port.in.QcStatusUseCase;
import fpt.qn.mes.quality.domain.entities.QcStatus;
import fpt.qn.mes.quality.domain.repository.QcStatusRepository;
import fpt.qn.mes.quality.domain.repository.criteria.QcStatusSearchCriteria;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class QcStatusService implements QcStatusUseCase {

    QcStatusRepository qcStatusRepository;
    QcStatusDtoMapper qcStatusMapper;
    CurrentUserPort currentUserPort;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<QcStatusResponse> getQcStatuses(QcStatusSearchRequest request) {
        QcStatusSearchCriteria criteria = QcStatusSearchCriteria.builder()
            .page(request.getPage())
            .size(request.getSize())
            .sort(request.getSort())
            .name(request.getName())
            .build();

        var result = qcStatusRepository.search(criteria);
        return PageResponse.of(
            result.getItems().stream().map(s -> qcStatusMapper.toDto(s)).toList(),
            result.getTotal(), criteria.getPage(), criteria.getSize());
    }

    @Override
    @Transactional
    public void createQcStatus(CreateQcStatusRequest request) {
        UUID currentUserId = currentUserPort.getCurrentUserId();
        QcStatus status = QcStatus.builder()
            .id(UUID.randomUUID())
            .name(request.getName())
            .description(request.getDescription())
            .createdBy(currentUserId)
            .updatedBy(currentUserId)
            .build();
        qcStatusRepository.save(status);
    }
}
