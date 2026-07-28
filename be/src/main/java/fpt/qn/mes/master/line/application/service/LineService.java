package fpt.qn.mes.master.line.application.service;

import java.time.Instant;
import java.util.UUID;

import org.jooq.DSLContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fpt.qn.mes.common.dto.response.PageResponse;
import fpt.qn.mes.master.line.application.dto.request.CreateLineRequest;
import fpt.qn.mes.master.line.application.dto.request.UpdateLineRequest;
import fpt.qn.mes.master.line.application.dto.response.ProductionLineDto;
import fpt.qn.mes.master.line.application.exception.LineConflictException;
import fpt.qn.mes.master.line.application.exception.LineNotFoundException;
import fpt.qn.mes.master.line.application.mapper.LineDtoMapper;
import fpt.qn.mes.master.line.application.port.in.LineUseCase;
import fpt.qn.mes.master.line.domain.entities.ProductionLine;
import fpt.qn.mes.master.line.domain.repository.ProductionLineRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import static fpt.qn.mes.jooq.Tables.LINE_STATUSES;
import static fpt.qn.mes.jooq.Tables.MACHINES;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class LineService implements LineUseCase {

    ProductionLineRepository lineRepository;
    LineDtoMapper mapper;
    DSLContext ctx;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProductionLineDto> getLines(int page, int size) {
        UUID activeStatusId = getActiveStatusId();
        return getLinesByStatus(page, size, activeStatusId);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProductionLineDto> getLinesByStatus(int page, int size, UUID statusId) {
        var result = lineRepository.findAllByStatus(page, size, statusId);
        var items = result.getItems().stream().map(mapper::toDto).toList();
        return PageResponse.<ProductionLineDto>builder()
                .items(items).totalElements(result.getTotal())
                .pageNumber(page).pageSize(size)
                .totalPages((int) Math.ceil((double) result.getTotal() / size))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ProductionLineDto getLineById(UUID id) {
        return lineRepository.findById(id)
                .map(mapper::toDto)
                .orElseThrow(() -> new LineNotFoundException("Line not found: " + id));
    }

    @Override
    @Transactional
    public ProductionLineDto createLine(CreateLineRequest request, UUID currentUserId) {
        if (lineRepository.existsByCode(request.getCode())) {
            throw new LineConflictException("Line code already exists: " + request.getCode());
        }
        var line = ProductionLine.create(request.getCode(), request.getName(), request.getLineStatusId(), currentUserId);
        return mapper.toDto(lineRepository.save(line));
    }

    @Override
    @Transactional
    public ProductionLineDto updateLine(UUID id, UpdateLineRequest request, UUID currentUserId) {
        var existing = lineRepository.findById(id)
                .orElseThrow(() -> new LineNotFoundException("Line not found: " + id));
        var updated = ProductionLine.builder()
                .id(existing.getId()).code(existing.getCode())
                .name(request.getName() != null ? request.getName() : existing.getName())
                .lineStatusId(request.getLineStatusId() != null ? request.getLineStatusId() : existing.getLineStatusId())
                .createdAt(existing.getCreatedAt()).createdBy(existing.getCreatedBy())
                .updatedAt(Instant.now()).updatedBy(currentUserId)
                .build();
        return mapper.toDto(lineRepository.update(updated));
    }

    @Override
    @Transactional
    public void deleteLine(UUID id) {
        var existing = lineRepository.findById(id)
                .orElseThrow(() -> new LineNotFoundException("Line not found: " + id));

        boolean hasMachines = ctx.fetchExists(
                ctx.selectFrom(MACHINES).where(MACHINES.PRODUCTION_LINE_ID.eq(id)));
        if (hasMachines) {
            throw new LineConflictException("Cannot deactivate line — has active machines: " + id);
        }

        UUID inactiveStatusId = ctx.select(LINE_STATUSES.ID)
                .from(LINE_STATUSES).where(LINE_STATUSES.NAME.eq("INACTIVE"))
                .fetchOptionalInto(UUID.class)
                .orElseThrow(() -> new IllegalStateException("INACTIVE not found in line_statuses"));

        var deactivated = ProductionLine.builder()
                .id(id).code(existing.getCode()).name(existing.getName())
                .lineStatusId(inactiveStatusId).createdAt(existing.getCreatedAt()).createdBy(existing.getCreatedBy())
                .updatedAt(Instant.now()).updatedBy(existing.getUpdatedBy())
                .build();
        lineRepository.update(deactivated);
    }

    private UUID getActiveStatusId() {
        return ctx.select(LINE_STATUSES.ID).from(LINE_STATUSES)
                .where(LINE_STATUSES.NAME.eq("ACTIVE"))
                .fetchOptionalInto(UUID.class)
                .orElseThrow(() -> new IllegalStateException("ACTIVE not found in line_statuses"));
    }
}
