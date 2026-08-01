package fpt.qn.mes.master.line.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fpt.qn.mes.common.dto.response.PageResponse;
import fpt.qn.mes.common.util.PaginationUtils;
import fpt.qn.mes.master.line.application.dto.linestatus.LineStatusResponse;
import fpt.qn.mes.master.line.application.dto.linestatus.create.CreateLineStatusRequest;
import fpt.qn.mes.master.line.application.dto.linestatus.search.LineStatusSearchRequest;
import fpt.qn.mes.master.line.application.exception.LineStatusConflictException;
import fpt.qn.mes.master.line.application.mapper.LineStatusDtoMapper;
import fpt.qn.mes.master.line.application.port.in.LineStatusUseCase;
import fpt.qn.mes.master.line.domain.entities.LineStatus;
import fpt.qn.mes.master.line.domain.repository.LineStatusRepository;
import fpt.qn.mes.master.line.domain.repository.criteria.LineStatusSearchCriteria;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class LineStatusService implements LineStatusUseCase {

    LineStatusRepository linestatusRepository;
    LineStatusDtoMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<LineStatusResponse> getLineStatuses(LineStatusSearchRequest request) {
        var criteria = LineStatusSearchCriteria.builder()
                .name(request.getName())
                .page(request.getPage())
                .size(request.getSize())
                .sort(request.getSort())
                .build();
        var result = linestatusRepository.search(criteria);
        var items = result.getItems().stream().map(s -> mapper.toDto(s)).toList();
        return PageResponse.<LineStatusResponse>builder()
                .items(items).totalElements(result.getTotal())
                .pageNumber(request.getPage()).pageSize(request.getSize())
                .totalPages(PaginationUtils.calculateTotalPages(result.getTotal(), request.getSize()))
                .build();
    }

    @Override
    @Transactional
    public void createLineStatus(CreateLineStatusRequest request) {
        if (linestatusRepository.existsByName(request.getName())) {
            throw new LineStatusConflictException("Name already exists: " + request.getName());
        }
        var status = LineStatus.create(request.getName(), request.getDescription());
        linestatusRepository.save(status);
    }
}
