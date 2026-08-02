package fpt.qn.mes.bom.application.service;

import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fpt.qn.mes.audit.domain.entities.AuditAction;
import fpt.qn.mes.audit.domain.events.AuditEvent;
import fpt.qn.mes.auth.application.port.out.CurrentUserPort;
import fpt.qn.mes.bom.application.dto.bom.BomResponse;
import fpt.qn.mes.bom.application.dto.bom.create.CreateBomRequest;
import fpt.qn.mes.bom.application.dto.bom.search.BomSearchRequest;
import fpt.qn.mes.bom.application.exception.BomNotFoundException;
import fpt.qn.mes.bom.application.exception.BomStatusNotFoundException;
import fpt.qn.mes.bom.application.exception.EmptyBomException;
import fpt.qn.mes.bom.application.exception.InvalidBomStatusException;
import fpt.qn.mes.bom.application.mapper.BomDtoMapper;
import fpt.qn.mes.bom.application.port.in.BomUseCase;
import fpt.qn.mes.bom.domain.constants.BomStatusConstants;
import fpt.qn.mes.bom.domain.entities.Bom;
import fpt.qn.mes.bom.domain.entities.BomItem;
import fpt.qn.mes.bom.domain.repository.BomItemRepository;
import fpt.qn.mes.bom.domain.repository.BomRepository;
import fpt.qn.mes.bom.domain.repository.BomStatusRepository;
import fpt.qn.mes.bom.domain.repository.criteria.BomSearchCriteria;
import fpt.qn.mes.common.dto.response.PageResponse;
import fpt.qn.mes.common.port.out.JsonSerializerPort;
import fpt.qn.mes.common.util.PaginationUtils;
import fpt.qn.mes.master.product.application.port.in.ProductUseCase;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BomService implements BomUseCase {

    BomRepository bomRepository;
    BomItemRepository bomItemRepository;
    BomStatusRepository bomStatusRepository;
    ProductUseCase productUseCase;
    CurrentUserPort currentUserPort;
    BomDtoMapper mapper;
    JsonSerializerPort jsonSerializer;
    ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<BomResponse> getBoms(BomSearchRequest request) {
        var criteria = BomSearchCriteria.builder()
                .finishedProductId(request.getFinishedProductId())
                .bomStatusId(request.getBomStatusId())
                .page(request.getPage())
                .size(request.getSize())
                .sort(request.getSort())
                .build();
        var result = bomRepository.search(criteria);
        return PageResponse.<BomResponse>builder()
                .items(result.getItems().stream().map(b -> mapper.toDto(b)).toList())
                .totalElements(result.getTotal())
                .pageNumber(request.getPage())
                .pageSize(request.getSize())
                .totalPages(PaginationUtils.calculateTotalPages(result.getTotal(), request.getSize()))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public BomResponse getBomById(UUID id) {
        return bomRepository.findById(id)
                .map(b -> mapper.toDto(b))
                .orElseThrow(() -> new BomNotFoundException("BOM not found: " + id));
    }

    @Override
    @Transactional
    public void createBom(CreateBomRequest request) {
        productUseCase.getProductById(request.getFinishedProductId());

        int maxVersion = bomRepository.findMaxVersionByFinishedProductId(request.getFinishedProductId());
        int version;
        if (request.getVersion() != null && request.getVersion() > 0) {
            version = bomRepository.existsByFinishedProductIdAndVersion(request.getFinishedProductId(), request.getVersion())
                    ? maxVersion + 1
                    : request.getVersion();
        } else {
            version = maxVersion + 1;
        }

        var draftStatus = bomStatusRepository.findByName(BomStatusConstants.DRAFT)
                .orElseThrow(() -> new BomStatusNotFoundException("DRAFT status not found in bom_statuses"));
        UUID currentUserId = currentUserPort.getCurrentUserId();
        Bom created = Bom.create(request.getFinishedProductId(), version, draftStatus.getId(), currentUserId);
        bomRepository.save(created);
        eventPublisher.publishEvent(AuditEvent.create(currentUserId, AuditAction.CREATE_BOM, "BOM", created.getId(),
                null, jsonSerializer.toJson(created), null));
    }

    @Override
    @Transactional
    public void activateBom(UUID id) {
        Bom bom = bomRepository.findById(id)
                .orElseThrow(() -> new BomNotFoundException("BOM not found: " + id));

        var draftStatus = bomStatusRepository.findByName(BomStatusConstants.DRAFT)
                .orElseThrow(() -> new BomStatusNotFoundException("DRAFT status not found in bom_statuses"));
        var activeStatus = bomStatusRepository.findByName(BomStatusConstants.ACTIVE)
                .orElseThrow(() -> new BomStatusNotFoundException("ACTIVE status not found in bom_statuses"));
        var inactiveStatus = bomStatusRepository.findByName(BomStatusConstants.INACTIVE)
                .orElseThrow(() -> new BomStatusNotFoundException("INACTIVE status not found in bom_statuses"));

        if (!draftStatus.getId().equals(bom.getBomStatus().getId())) {
            throw new InvalidBomStatusException("Only DRAFT BOMs can be activated");
        }
        if (bom.getItems() == null || bom.getItems().isEmpty()) {
            throw new EmptyBomException("Cannot activate an empty BOM (must contain at least 1 item)");
        }

        bomRepository.deactivateActiveBomsForProduct(bom.getFinishedProductId(), activeStatus.getId(), inactiveStatus.getId());

        Bom activated = Bom.changeStatus(bom, activeStatus.getId());
        bomRepository.update(activated);

        UUID currentUserId = currentUserPort.getCurrentUserId();
        eventPublisher.publishEvent(AuditEvent.create(currentUserId, AuditAction.ACTIVATE_BOM, "BOM", id,
                jsonSerializer.toJson(bom),
                jsonSerializer.toJson(activated),
                null));
    }

    @Override
    @Transactional
    public void deactivateBom(UUID id) {
        Bom bom = bomRepository.findById(id)
                .orElseThrow(() -> new BomNotFoundException("BOM not found: " + id));

        var activeStatus = bomStatusRepository.findByName(BomStatusConstants.ACTIVE)
                .orElseThrow(() -> new BomStatusNotFoundException("ACTIVE status not found in bom_statuses"));
        var inactiveStatus = bomStatusRepository.findByName(BomStatusConstants.INACTIVE)
                .orElseThrow(() -> new BomStatusNotFoundException("INACTIVE status not found in bom_statuses"));

        if (!activeStatus.getId().equals(bom.getBomStatus().getId())) {
            throw new InvalidBomStatusException("Only ACTIVE BOMs can be deactivated");
        }

        Bom deactivated = Bom.changeStatus(bom, inactiveStatus.getId());
        bomRepository.update(deactivated);

        UUID currentUserId = currentUserPort.getCurrentUserId();
        eventPublisher.publishEvent(AuditEvent.create(currentUserId, AuditAction.DEACTIVATE_BOM, "BOM", id,
                jsonSerializer.toJson(bom),
                jsonSerializer.toJson(deactivated),
                null));
    }

    @Override
    @Transactional
    public BomResponse createNewVersion(UUID id) {
        Bom sourceBom = bomRepository.findById(id)
                .orElseThrow(() -> new BomNotFoundException("Source BOM not found: " + id));

        var draftStatus = bomStatusRepository.findByName(BomStatusConstants.DRAFT)
                .orElseThrow(() -> new BomStatusNotFoundException("DRAFT status not found in bom_statuses"));

        int newVersion = bomRepository.findMaxVersionByFinishedProductId(sourceBom.getFinishedProductId()) + 1;
        UUID currentUserId = currentUserPort.getCurrentUserId();

        Bom newBom = Bom.create(sourceBom.getFinishedProductId(), newVersion, draftStatus.getId(), currentUserId);
        Bom savedBom = bomRepository.save(newBom);

        if (sourceBom.getItems() != null) {
            for (BomItem sourceItem : sourceBom.getItems()) {
                bomItemRepository.save(BomItem.create(
                        savedBom.getId(),
                        sourceItem.getMaterialProductId(),
                        sourceItem.getQuantityPerUnit(),
                        sourceItem.getScrapRate()));
            }
        }

        return bomRepository.findById(savedBom.getId())
                .map(b -> mapper.toDto(b))
                .orElseThrow(() -> new BomNotFoundException("BOM not found after save: " + savedBom.getId()));
    }
}
