package fpt.qn.mes.quality.application.service;

import java.util.UUID;

import fpt.qn.mes.common.util.UuidV7;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fpt.qn.mes.auth.application.port.out.CurrentUserPort;
import fpt.qn.mes.common.dto.response.PageResponse;
import fpt.qn.mes.quality.application.dto.qcaction.QcActionResponse;
import fpt.qn.mes.quality.application.dto.qcaction.create.CreateQcActionRequest;
import fpt.qn.mes.quality.application.dto.qcaction.search.QcActionSearchRequest;
import fpt.qn.mes.quality.application.mapper.QcActionDtoMapper;
import fpt.qn.mes.quality.application.port.in.QcActionUseCase;
import fpt.qn.mes.quality.domain.entities.QcAction;
import fpt.qn.mes.quality.domain.repository.QcActionRepository;
import fpt.qn.mes.quality.domain.repository.criteria.QcActionSearchCriteria;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class QcActionService implements QcActionUseCase {

    QcActionRepository qcActionRepository;
    QcActionDtoMapper qcActionMapper;
    CurrentUserPort currentUserPort;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<QcActionResponse> getQcActions(QcActionSearchRequest request) {
        QcActionSearchCriteria criteria = QcActionSearchCriteria.builder()
            .page(request.getPage())
            .size(request.getSize())
            .sort(request.getSort())
            .name(request.getName())
            .build();

        var result = qcActionRepository.search(criteria);
        return PageResponse.of(
            result.getItems().stream().map(a -> qcActionMapper.toDto(a)).toList(),
            result.getTotal(), criteria.getPage(), criteria.getSize());
    }

    @Override
    @Transactional
    public void createQcAction(CreateQcActionRequest request) {
        UUID currentUserId = currentUserPort.getCurrentUserId();
        QcAction action = QcAction.builder()
            .id(UuidV7.generate())
            .name(request.getName())
            .description(request.getDescription())
            .createdBy(currentUserId)
            .updatedBy(currentUserId)
            .build();
        qcActionRepository.save(action);
    }
}
