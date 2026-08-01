package fpt.qn.mes.inventory.application.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fpt.qn.mes.common.dto.response.PageResponse;
import fpt.qn.mes.common.util.PaginationUtils;
import fpt.qn.mes.inventory.application.dto.lottype.LotTypeResponse;
import fpt.qn.mes.inventory.application.dto.lottype.create.CreateLotTypeRequest;
import fpt.qn.mes.inventory.application.dto.lottype.search.LotTypeSearchRequest;
import fpt.qn.mes.auth.application.port.out.CurrentUserPort;
import fpt.qn.mes.inventory.application.exception.LotTypeConflictException;
import fpt.qn.mes.inventory.application.mapper.LotTypeDtoMapper;
import fpt.qn.mes.inventory.application.port.in.LotTypeUseCase;
import fpt.qn.mes.inventory.domain.entities.LotType;
import fpt.qn.mes.inventory.domain.repository.LotTypeRepository;
import fpt.qn.mes.inventory.domain.repository.criteria.LotTypeSearchCriteria;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class LotTypeService implements LotTypeUseCase {

    LotTypeRepository lotTypeRepository;
    LotTypeDtoMapper mapper;
    CurrentUserPort currentUserPort;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<LotTypeResponse> getLotTypes(LotTypeSearchRequest request) {
        var criteria = LotTypeSearchCriteria.builder()
                .query(request.getQuery())
                .name(request.getName())
                .page(request.getPage())
                .size(request.getSize())
                .sort(request.getSort())
                .build();
        var result = lotTypeRepository.search(criteria);
        var items = result.getItems().stream().map(lt -> mapper.toDto(lt)).toList();
        return PageResponse.<LotTypeResponse>builder()
                .items(items).totalElements(result.getTotal())
                .pageNumber(request.getPage()).pageSize(request.getSize())
                .totalPages(PaginationUtils.calculateTotalPages(result.getTotal(), request.getSize()))
                .build();
    }

    @Override
    @Transactional
    public void createLotType(CreateLotTypeRequest request) {
        if (lotTypeRepository.existsByName(request.getName())) {
            throw new LotTypeConflictException("LotType with name already exists: " + request.getName());
        }
        lotTypeRepository.save(LotType.create(request.getName(), request.getDescription(), currentUserPort.getCurrentUserId()));
    }

    @Override
    public UUID getLotTypeIdByName(String name) {
        return lotTypeRepository.findIdByName(name).orElse(null);
    }
}
