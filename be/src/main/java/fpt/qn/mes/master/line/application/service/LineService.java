package fpt.qn.mes.master.line.application.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fpt.qn.mes.auth.application.port.out.CurrentUserPort;
import fpt.qn.mes.common.dto.response.PageResponse;
import fpt.qn.mes.common.util.PaginationUtils;
import fpt.qn.mes.master.line.application.dto.line.LineResponse;
import fpt.qn.mes.master.line.application.dto.line.create.CreateLineRequest;
import fpt.qn.mes.master.line.application.dto.line.search.LineSearchRequest;
import fpt.qn.mes.master.line.application.dto.line.update.UpdateLineRequest;
import fpt.qn.mes.master.line.application.exception.LineConflictException;
import fpt.qn.mes.master.line.application.exception.LineNotFoundException;
import fpt.qn.mes.master.line.application.exception.LineStatusNotFoundException;
import fpt.qn.mes.master.line.application.mapper.LineDtoMapper;
import fpt.qn.mes.master.line.application.port.in.LineUseCase;
import fpt.qn.mes.master.line.application.port.out.LineMachinePort;
import fpt.qn.mes.master.line.domain.entities.Line;
import fpt.qn.mes.master.line.domain.repository.LineRepository;
import fpt.qn.mes.master.line.domain.repository.LineStatusRepository;
import fpt.qn.mes.master.line.domain.constants.LineStatusConstants;
import fpt.qn.mes.master.line.domain.repository.criteria.LineSearchCriteria;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class LineService implements LineUseCase {

    LineRepository lineRepository;
    LineStatusRepository lineStatusRepository;
    LineMachinePort lineMachinePort;
    LineDtoMapper mapper;
    CurrentUserPort currentUserPort;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<LineResponse> getLines(LineSearchRequest request) {
        LineSearchCriteria criteria = LineSearchCriteria.builder()
                .code(request.getCode())
                .name(request.getName())
                .lineStatusId(request.getLineStatusId())
                .page(request.getPage())
                .size(request.getSize())
                .sort(request.getSort())
                .build();

        var result = lineRepository.search(criteria);
        var items = result.getItems().stream().map(line -> mapper.toDto(line)).toList();
        return PageResponse.<LineResponse>builder()
                .items(items).totalElements(result.getTotal())
                .pageNumber(request.getPage()).pageSize(request.getSize())
                .totalPages(PaginationUtils.calculateTotalPages(result.getTotal(), request.getSize()))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public LineResponse getLineById(UUID id) {
        return lineRepository.findById(id)
                .map(line -> mapper.toDto(line))
                .orElseThrow(() -> new LineNotFoundException("Line not found: " + id));
    }

    @Override
    @Transactional
    public void createLine(CreateLineRequest request) {
        if (lineRepository.existsByCode(request.getCode())) {
            throw new LineConflictException("Line code already exists: " + request.getCode());
        }
        if (!lineStatusRepository.existsById(request.getLineStatusId())) {
            throw new LineStatusNotFoundException("Line status not found: " + request.getLineStatusId());
        }
        UUID currentUserId = currentUserPort.getCurrentUserId();
        lineRepository.save(Line.create(request.getCode(), request.getName(), request.getLineStatusId(), currentUserId));
    }

    @Override
    @Transactional
    public void updateLine(UUID id, UpdateLineRequest request) {
        var existing = lineRepository.findById(id)
                .orElseThrow(() -> new LineNotFoundException("Line not found: " + id));

        if (request.getLineStatusId() != null && !lineStatusRepository.existsById(request.getLineStatusId())) {
            throw new LineStatusNotFoundException("Line status not found: " + request.getLineStatusId());
        }
        UUID currentUserId = currentUserPort.getCurrentUserId();
        lineRepository.update(Line.update(existing, request.getName(), request.getLineStatusId(), currentUserId));
    }

    @Override
    @Transactional
    public void activateLine(UUID id) {
        var existing = lineRepository.findById(id)
                .orElseThrow(() -> new LineNotFoundException("Line not found: " + id));
        var activeStatus = lineStatusRepository.findByName(LineStatusConstants.ACTIVE)
                .orElseThrow(() -> new LineStatusNotFoundException("ACTIVE status not found in line_statuses"));
        UUID currentUserId = currentUserPort.getCurrentUserId();
        lineRepository.update(Line.activate(existing, activeStatus.getId(), currentUserId));
    }

    @Override
    @Transactional
    public void deactivateLine(UUID id) {
        var existing = lineRepository.findById(id)
                .orElseThrow(() -> new LineNotFoundException("Line not found: " + id));

        if (lineMachinePort.hasRunningMachines(id)) {
            throw new LineConflictException("Cannot deactivate line with running machines: " + id);
        }

        var inactiveStatus = lineStatusRepository.findByName(LineStatusConstants.INACTIVE)
                .orElseThrow(() -> new LineStatusNotFoundException("INACTIVE status not found in line_statuses"));

        UUID currentUserId = currentUserPort.getCurrentUserId();
        lineRepository.update(Line.deactivate(existing, inactiveStatus.getId(), currentUserId));
    }
}
