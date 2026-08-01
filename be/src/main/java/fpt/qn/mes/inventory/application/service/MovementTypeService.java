package fpt.qn.mes.inventory.application.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fpt.qn.mes.common.dto.response.PageResponse;
import fpt.qn.mes.common.util.PaginationUtils;
import fpt.qn.mes.inventory.application.dto.movementtype.MovementTypeResponse;
import fpt.qn.mes.inventory.application.dto.movementtype.create.CreateMovementTypeRequest;
import fpt.qn.mes.inventory.application.dto.movementtype.search.MovementTypeSearchRequest;
import fpt.qn.mes.auth.application.port.out.CurrentUserPort;
import fpt.qn.mes.inventory.application.exception.MovementTypeConflictException;
import fpt.qn.mes.inventory.application.mapper.MovementTypeDtoMapper;
import fpt.qn.mes.inventory.application.port.in.MovementTypeUseCase;
import fpt.qn.mes.inventory.domain.entities.MovementType;
import fpt.qn.mes.inventory.domain.repository.MovementTypeRepository;
import fpt.qn.mes.inventory.domain.repository.criteria.MovementTypeSearchCriteria;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MovementTypeService implements MovementTypeUseCase {

    MovementTypeRepository movementTypeRepository;
    MovementTypeDtoMapper mapper;
    CurrentUserPort currentUserPort;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<MovementTypeResponse> getMovementTypes(MovementTypeSearchRequest request) {
        var criteria = MovementTypeSearchCriteria.builder()
                .query(request.getQuery())
                .name(request.getName())
                .page(request.getPage())
                .size(request.getSize())
                .sort(request.getSort())
                .build();
        var result = movementTypeRepository.search(criteria);
        var items = result.getItems().stream().map(mt -> mapper.toDto(mt)).toList();
        return PageResponse.<MovementTypeResponse>builder()
                .items(items).totalElements(result.getTotal())
                .pageNumber(request.getPage()).pageSize(request.getSize())
                .totalPages(PaginationUtils.calculateTotalPages(result.getTotal(), request.getSize()))
                .build();
    }

    @Override
    @Transactional
    public void createMovementType(CreateMovementTypeRequest request) {
        if (movementTypeRepository.existsByName(request.getName())) {
            throw new MovementTypeConflictException("MovementType with name already exists: " + request.getName());
        }
        movementTypeRepository.save(MovementType.create(request.getName(), request.getDescription(), currentUserPort.getCurrentUserId()));
    }

    @Override
    public UUID getMovementTypeIdByName(String name) {
        return movementTypeRepository.findIdByName(name).orElse(null);
    }
}
