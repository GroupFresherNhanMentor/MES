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
import fpt.qn.mes.bom.application.exception.EmptyBomException;
import fpt.qn.mes.bom.application.exception.InvalidBomStatusException;
import fpt.qn.mes.bom.application.mapper.BomDtoMapper;
import fpt.qn.mes.bom.application.port.in.BomUseCase;
import fpt.qn.mes.bom.domain.entities.Bom;
import fpt.qn.mes.bom.domain.entities.BomItem;
import fpt.qn.mes.bom.domain.repository.BomRepository;
import fpt.qn.mes.common.dto.response.PageResponse;
import fpt.qn.mes.common.dto.response.PaginationResult;
import fpt.qn.mes.master.product.application.port.in.ProductUseCase;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import fpt.qn.mes.common.service.LookupEntry;
import fpt.qn.mes.common.service.LookupRepository;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BomService implements BomUseCase {

    BomRepository bomRepository;
    BomDtoMapper mapper;
    ProductUseCase productUseCase;
    CurrentUserPort currentUserPort;
    LookupRepository lookupRepository;

    @Override
    @Transactional(readOnly = true)
    public List<LookupEntry> getBomStatuses() {
        return lookupRepository.findAll("bom_statuses");
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<BomDto> getBoms(int page, int size, UUID finishedProductId, UUID bomStatusId) {
        PaginationResult<Bom> result = bomRepository.findAll(page, size, finishedProductId, bomStatusId);
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
        productUseCase.getProductById(request.getFinishedProductId());

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
        // 1. Fetch target BOM
        Bom bom = bomRepository.findById(id)
                .orElseThrow(() -> new BomNotFoundException("BOM not found: " + id));

        // 2. Resolve status IDs
        UUID draftStatusId = bomRepository.findStatusIdByName("DRAFT")
                .orElse(UUID.fromString("00000000-0000-0000-0000-000000000001"));
        UUID activeStatusId = bomRepository.findStatusIdByName("ACTIVE")
                .orElse(UUID.fromString("00000000-0000-0000-0000-000000000002"));
        UUID inactiveStatusId = bomRepository.findStatusIdByName("INACTIVE")
                .orElse(UUID.fromString("00000000-0000-0000-0000-000000000003"));

        // 3. Validate status is DRAFT
        if (!draftStatusId.equals(bom.getBomStatusId())) {
            throw new InvalidBomStatusException("Only DRAFT BOMs can be activated");
        }

        // 4. Validate BOM contains items (> 0)
        int itemCount = bomRepository.countItemsByBomId(id);
        if (itemCount == 0 && (bom.getItems() == null || bom.getItems().isEmpty())) {
            throw new EmptyBomException("Cannot activate an empty BOM (must contain at least 1 item)");
        }

        // 5. Deactivate any existing ACTIVE BOM for this product
        bomRepository.deactivateActiveBomsForProduct(
                bom.getFinishedProductId(),
                activeStatusId,
                inactiveStatusId
        );

        // 6. Transition target BOM to ACTIVE
        bom.updateStatus(activeStatusId);
        Bom savedBom = bomRepository.save(bom);

        return mapper.toDto(savedBom);
    }

    @Override
    @Transactional
    public BomDto createNewVersion(UUID id) {
        // 1. Fetch source BOM
        Bom sourceBom = bomRepository.findById(id)
                .orElseThrow(() -> new BomNotFoundException("Source BOM not found: " + id));

        // 2. Resolve DRAFT status ID
        UUID draftStatusId = bomRepository.findStatusIdByName("DRAFT")
                .orElse(UUID.fromString("00000000-0000-0000-0000-000000000001"));

        // 3. Find max version and calculate next version
        int maxVersion = bomRepository.findMaxVersionByFinishedProductId(sourceBom.getFinishedProductId());
        int newVersion = maxVersion + 1;

        // 4. Get current user ID
        UUID currentUserId = currentUserPort.getCurrentUserId();

        // 5. Create new draft BOM entity
        Bom newBom = Bom.create(
                sourceBom.getFinishedProductId(),
                newVersion,
                draftStatusId,
                currentUserId
        );

        // 6. Deep copy component items if present
        if (sourceBom.getItems() != null && !sourceBom.getItems().isEmpty()) {
            for (BomItem sourceItem : sourceBom.getItems()) {
                BomItem clonedItem = BomItem.create(
                        newBom.getId(),
                        sourceItem.getMaterialProductId(),
                        sourceItem.getQuantityPerUnit(),
                        sourceItem.getUnit(),
                        sourceItem.getScrapRate()
                );
                newBom.getItems().add(clonedItem);
            }
        }

        // 7. Save and return DTO
        Bom savedBom = bomRepository.save(newBom);
        return mapper.toDto(savedBom);
    }

    @Override
    @Transactional
    public BomItemDto addBomItem(UUID bomId, CreateBomItemRequest request) {
        Bom bom = bomRepository.findById(bomId)
                .orElseThrow(() -> new BomNotFoundException("BOM not found: " + bomId));

        UUID draftStatusId = bomRepository.findStatusIdByName("DRAFT")
                .orElse(UUID.fromString("00000000-0000-0000-0000-000000000001"));

        if (!draftStatusId.equals(bom.getBomStatusId())) {
            throw new InvalidBomStatusException("Only DRAFT BOMs can be modified; create a new version instead");
        }

        BomItem item = BomItem.create(
                bomId,
                request.getMaterialProductId(),
                request.getQuantityPerUnit(),
                request.getUnit(),
                request.getScrapRate()
        );

        BomItem savedItem = bomRepository.saveItem(item);
        return mapper.toDto(savedItem);
    }

    @Override
    @Transactional
    public void deleteBomItem(UUID bomId, UUID itemId) {
        Bom bom = bomRepository.findById(bomId)
                .orElseThrow(() -> new BomNotFoundException("BOM not found: " + bomId));

        UUID draftStatusId = bomRepository.findStatusIdByName("DRAFT")
                .orElse(UUID.fromString("00000000-0000-0000-0000-000000000001"));

        if (!draftStatusId.equals(bom.getBomStatusId())) {
            throw new InvalidBomStatusException("Only DRAFT BOMs can be modified; create a new version instead");
        }

        bomRepository.deleteItemById(itemId);
    }
}
