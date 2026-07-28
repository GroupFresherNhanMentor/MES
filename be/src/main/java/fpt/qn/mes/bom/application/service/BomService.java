package fpt.qn.mes.bom.application.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fpt.qn.mes.auth.application.port.out.CurrentUserPort;
import fpt.qn.mes.bom.application.dto.request.CreateBomItemRequest;
import fpt.qn.mes.bom.application.dto.request.CreateBomRequest;
import fpt.qn.mes.bom.application.dto.response.BomDto;
import fpt.qn.mes.bom.application.dto.response.BomItemDto;
import fpt.qn.mes.bom.application.exception.BomAlreadyExistsException;
import fpt.qn.mes.bom.application.exception.BomNotFoundException;
import fpt.qn.mes.bom.application.mapper.BomDtoMapper;
import fpt.qn.mes.bom.application.port.in.BomUseCase;
import fpt.qn.mes.bom.domain.entities.Bom;
import fpt.qn.mes.bom.domain.repository.BomRepository;
import fpt.qn.mes.common.dto.response.PageResponse;
import fpt.qn.mes.common.dto.response.PaginationResult;
import fpt.qn.mes.master.product.application.dto.response.ProductDto;
import fpt.qn.mes.master.product.application.port.in.ProductUseCase;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BomService implements BomUseCase {

    BomRepository bomRepository;
    BomDtoMapper mapper;
    ProductUseCase productUseCase;
    CurrentUserPort currentUserPort;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<BomDto> getBoms(int page, int size) {
        PaginationResult<Bom> result = bomRepository.findAll(page, size);
        int totalPages = size > 0 ? (int) Math.ceil((double) result.getTotal() / size) : 0;
        return PageResponse.<BomDto>builder()
                .items(result.getItems().stream().map(mapper::toDto).toList())
                .totalElements(result.getTotal())
                .totalPages(totalPages)
                .pageNumber(page)
                .pageSize(size)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public BomDto getBomById(UUID id) {
        return bomRepository.findById(id)
                .map(mapper::toDto)
                .orElseThrow(() -> new BomNotFoundException("BOM not found: " + id));
    }

    @Override
    @Transactional
    public BomDto createBom(CreateBomRequest request) {
        UUID currentUserId = currentUserPort.getCurrentUserId();

        // 1. Verify product exists
        ProductDto product = productUseCase.getProductById(request.getFinishedProductId());

        // 2. Verify version does not already exist
        if (bomRepository.existsByFinishedProductIdAndVersion(request.getFinishedProductId(), request.getVersion())) {
            throw new BomAlreadyExistsException(
                    "BOM version " + request.getVersion() + " already exists for product " + request.getFinishedProductId()
            );
        }

        // 3. Resolve status ID (default to DRAFT if not provided)
        UUID statusId = request.getBomStatusId();
        if (statusId == null) {
            statusId = bomRepository.findStatusIdByName("DRAFT")
                    .orElse(UUID.fromString("00000000-0000-0000-0000-000000000001"));
        }

        // 4. Create and save entity
        Bom bom = Bom.create(
                request.getFinishedProductId(),
                request.getVersion(),
                statusId,
                currentUserId
        );

        Bom savedBom = bomRepository.save(bom);
        return mapper.toDto(savedBom);
    }

    @Override
    @Transactional
    public BomDto activateBom(UUID id) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    @Transactional
    public BomItemDto addBomItem(UUID bomId, CreateBomItemRequest request) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    @Transactional
    public void deleteBomItem(UUID bomId, UUID itemId) {
        throw new UnsupportedOperationException("Not implemented");
    }
}
