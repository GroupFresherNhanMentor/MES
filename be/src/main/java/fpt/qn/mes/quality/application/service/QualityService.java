package fpt.qn.mes.quality.application.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import fpt.qn.mes.common.domainQuery.PaginationResult;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fpt.qn.mes.auth.application.port.out.CurrentUserPort;
import fpt.qn.mes.common.dto.response.PageResponse;
import fpt.qn.mes.quality.application.dto.inspection.QualityInspectionResponse;
import fpt.qn.mes.quality.application.dto.inspection.QualityInspectionResultResponse;
import fpt.qn.mes.quality.application.dto.inspection.create.CreateQualityInspectionRequest;
import fpt.qn.mes.quality.application.dto.inspection.fail.FailQcRequest;
import fpt.qn.mes.quality.application.dto.inspection.pass.PassQcRequest;
import fpt.qn.mes.quality.application.dto.inspection.result.search.QualityInspectionResultSearchRequest;
import fpt.qn.mes.quality.application.dto.inspection.search.QualityInspectionSearchRequest;
import fpt.qn.mes.quality.application.exception.DefectTypeRequiredException;
import fpt.qn.mes.quality.application.exception.InspectionAlreadyClosedException;
import fpt.qn.mes.quality.application.exception.InsufficientRemainingQuantityException;
import fpt.qn.mes.quality.application.exception.InvalidQcActionException;
import fpt.qn.mes.quality.application.exception.QcStatusNotFoundException;
import fpt.qn.mes.quality.application.exception.QualityInspectionNotFoundException;
import fpt.qn.mes.quality.application.exception.ReasonRequiredException;
import fpt.qn.mes.quality.application.mapper.QualityDtoMapper;
import fpt.qn.mes.quality.application.port.in.QualityUseCase;
import fpt.qn.mes.quality.application.port.out.QcStockPort;
import fpt.qn.mes.quality.domain.entities.DefectType;
import fpt.qn.mes.quality.domain.entities.QcAction;
import fpt.qn.mes.quality.domain.entities.QcStatus;
import fpt.qn.mes.quality.domain.entities.QualityInspection;
import fpt.qn.mes.quality.domain.entities.QualityInspectionResult;
import fpt.qn.mes.quality.domain.repository.QcActionRepository;
import fpt.qn.mes.quality.domain.repository.QcStatusRepository;
import fpt.qn.mes.quality.domain.repository.QualityInspectionRepository;
import fpt.qn.mes.quality.domain.repository.criteria.QualityInspectionResultSearchCriteria;
import fpt.qn.mes.quality.domain.repository.criteria.QualityInspectionSearchCriteria;
import fpt.qn.mes.audit.domain.entities.AuditAction;
import fpt.qn.mes.audit.domain.events.AuditEvent;
import org.springframework.context.ApplicationEventPublisher;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class QualityService implements QualityUseCase {

    QualityInspectionRepository inspectionRepository;
    QcStatusRepository qcStatusRepository;
    QcActionRepository qcActionRepository;
    QcStockPort qcStockPort;
    QualityDtoMapper qualityDtoMapper;
    CurrentUserPort currentUserPort;
    ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<QualityInspectionResponse> getInspections(QualityInspectionSearchRequest request) {
        QualityInspectionSearchCriteria criteria = QualityInspectionSearchCriteria.builder()
            .productCode(request != null ? request.getProductCode() : null)
            .productName(request != null ? request.getProductName() : null)
            .productTypeId(request != null ? request.getProductTypeId() : null)
            .workOrderCode(request != null ? request.getWorkOrderCode() : null)
            .lotNumber(request != null ? request.getLotNumber() : null)
            .lotType(request != null ? request.getLotType() : null)
            .qcStatusId(request != null ? request.getQcStatusId() : null)
            .page(request != null ? request.getPage() : 0)
            .size(request != null ? request.getSize() : 20)
            .sort(request != null ? request.getSort() : List.of())
            .build();

        PaginationResult<QualityInspection> result = inspectionRepository.search(criteria);
        List<QualityInspectionResponse> dtos = result.getItems().stream()
            .map(i -> qualityDtoMapper.toDto(i)).toList();

        return PageResponse.of(dtos, result.getTotal(),
            request != null ? request.getPage() : 0,
            request != null ? request.getSize() : 20);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<QualityInspectionResultResponse> getInspectionResults(
            UUID inspectionId, QualityInspectionResultSearchRequest request) {
        if (inspectionRepository.findById(inspectionId).isEmpty()) {
            throw new QualityInspectionNotFoundException("QC inspection not found: " + inspectionId);
        }
        QualityInspectionResultSearchCriteria criteria = QualityInspectionResultSearchCriteria.builder()
            .inspectionId(inspectionId)
            .isPass(request != null ? request.getIsPass() : null)
            .defectTypeId(request != null ? request.getDefectTypeId() : null)
            .inspectorId(request != null ? request.getInspectorId() : null)
            .actionId(request != null ? request.getActionId() : null)
            .page(request != null ? request.getPage() : 0)
            .size(request != null ? request.getSize() : 20)
            .sort(request != null ? request.getSort() : List.of())
            .build();

        PaginationResult<QualityInspectionResult> result = inspectionRepository.searchResults(criteria);
        List<QualityInspectionResultResponse> dtos = result.getItems().stream()
            .map(r -> qualityDtoMapper.toDto(r)).toList();
        return PageResponse.of(dtos, result.getTotal(),
            request != null ? request.getPage() : 0,
            request != null ? request.getSize() : 20);
    }

    @Override
    @Transactional
    public void createInspection(CreateQualityInspectionRequest req) {
        QcStatus qcStatus = qcStatusRepository.findById(req.getQcStatusId())
            .orElseThrow(() -> new QcStatusNotFoundException("QC status not found: " + req.getQcStatusId()));
        QualityInspection inspection = QualityInspection.create(
            req.getWorkOrderId(), req.getProductId(), req.getLotId(), req.getQuantity(), qcStatus);
        inspectionRepository.save(inspection);
    }

    @Override
    @Transactional
    public void passInspection(UUID inspectionId, PassQcRequest request) {
        QualityInspection inspection = inspectionRepository.findById(inspectionId)
            .orElseThrow(() -> new QualityInspectionNotFoundException("QC inspection not found: " + inspectionId));

        if (request.getPassedQuantity().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InsufficientRemainingQuantityException("passedQuantity must be > 0");
        }

        BigDecimal processed = inspectionRepository.sumResultQuantities(inspectionId);

        // if (remaining.compareTo(BigDecimal.ZERO) <= 0) {
        //     throw new InspectionAlreadyClosedException("Inspection " + inspectionId + " is already fully processed");
        // }

        // if (passedQty.compareTo(remaining) > 0) {
        //     throw new InsufficientRemainingQuantityException(
        //         "Passed quantity " + passedQty + " exceeds remaining " + remaining);
        // }

        UUID currentUserId = currentUserPort.getCurrentUserId();

        QualityInspectionResult result = QualityInspectionResult.create(
            inspectionId, true, request.getPassedQuantity(), null, null, null, currentUserId, request.getNote());
        inspectionRepository.saveResult(result);

        if (eventPublisher != null) {
            eventPublisher.publishEvent(AuditEvent.create(currentUserId, AuditAction.QC_PASS, "QUALITY_INSPECTION", inspectionId,
                    null, "{\"passedQuantity\":" + request.getPassedQuantity() + "}", null));
        }

        UUID fromStockStatusId = qcStockPort.getQualityInspectionStatusId();
        UUID toStockStatusId   = qcStockPort.getAvailableStatusId();
        UUID movementTypeId    = qcStockPort.getQcReleaseMovementTypeId();

        // qcStockPort.transferStock(
        //     inspection.getLot().getId(), inspection.getProduct().getId(), passedQty,
        //     fromStockStatusId, toStockStatusId, movementTypeId,
        //     inspection.getWorkOrder().getId(), currentUserId);

        // BigDecimal newRemaining = remaining.subtract(passedQty);
        // if (newRemaining.compareTo(BigDecimal.ZERO) <= 0) {
        //     UUID passedStatusId = qcStatusRepository.findAll().stream()
        //         .filter(s -> "PASSED".equals(s.getName()))
        //         .findFirst()
        //         .map(s -> s.getId())
        //         .orElseThrow(() -> new IllegalStateException("PASSED QC status not found"));
        //     inspectionRepository.updateStatus(inspection.getId(), passedStatusId);
        // }
    }

    @Override
    @Transactional
    public void failInspection(UUID inspectionId, FailQcRequest request) {
        QualityInspection inspection = inspectionRepository.findById(inspectionId)
            .orElseThrow(() -> new QualityInspectionNotFoundException("QC inspection not found: " + inspectionId));

        BigDecimal processed = inspectionRepository.sumResultQuantities(inspectionId);
        BigDecimal remaining = inspection.getQuantity().subtract(processed);

        if (remaining.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InspectionAlreadyClosedException("Inspection " + inspectionId + " is already fully processed");
        }

        BigDecimal failedQty = request.getFailedQuantity();
        if (failedQty.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InsufficientRemainingQuantityException("failedQuantity must be > 0");
        }
        if (failedQty.compareTo(remaining) > 0) {
            throw new InsufficientRemainingQuantityException(
                "Failed quantity " + failedQty + " exceeds remaining " + remaining);
        }
        if (request.getDefectTypeId() == null) {
            throw new DefectTypeRequiredException("defectTypeId is required when failing QC");
        }
        if (request.getReason() == null || request.getReason().isBlank()) {
            throw new ReasonRequiredException("reason is required when failing QC");
        }

        QcAction action = qcActionRepository.findById(request.getActionId())
            .orElseThrow(() -> new InvalidQcActionException("Invalid actionId: " + request.getActionId()));
        String actionName = action.getName();

        UUID currentUserId = currentUserPort.getCurrentUserId();

        DefectType defectType = DefectType.builder().id(request.getDefectTypeId()).build();

        QualityInspectionResult result = QualityInspectionResult.create(
            inspectionId, false, failedQty, defectType, request.getReason(), action, currentUserId, request.getNote());
        inspectionRepository.saveResult(result);

        if (eventPublisher != null) {
            AuditAction auditAction = AuditAction.QC_FAIL;
            if ("HOLD".equals(actionName)) {
                auditAction = AuditAction.QC_HOLD;
            } else if ("SCRAP".equals(actionName)) {
                auditAction = AuditAction.SCRAP_STOCK;
            }
            eventPublisher.publishEvent(AuditEvent.create(currentUserId, auditAction, "QUALITY_INSPECTION", inspectionId,
                    null, "{\"failedQuantity\":" + failedQty + ",\"reason\":\"" + request.getReason() + "\",\"action\":\"" + actionName + "\"}", null));
        }

        UUID fromStockStatusId = qcStockPort.getQualityInspectionStatusId();
        String qcStatusName;

        switch (actionName) {
            case "SCRAP":
                qcStatusName = "FAILED";
                qcStockPort.transferStock(
                    inspection.getLot().getId(), inspection.getProduct().getId(), failedQty,
                    fromStockStatusId, qcStockPort.getScrappedStatusId(),
                    qcStockPort.getScrapMovementTypeId(),
                    inspection.getWorkOrder().getId(), currentUserId);
                break;
            case "HOLD":
                qcStatusName = "ON_HOLD";
                qcStockPort.transferStock(
                    inspection.getLot().getId(), inspection.getProduct().getId(), failedQty,
                    fromStockStatusId, qcStockPort.getOnHoldStatusId(),
                    qcStockPort.getQcHoldMovementTypeId(),
                    inspection.getWorkOrder().getId(), currentUserId);
                break;
            case "REWORK":
                qcStatusName = "REWORK_REQUIRED";
                break;
            default:
                throw new InvalidQcActionException("Unknown action: " + actionName);
        }

        BigDecimal newRemaining = remaining.subtract(failedQty);
        if (newRemaining.compareTo(BigDecimal.ZERO) <= 0) {
            UUID failStatusId = qcStatusRepository.findAll().stream()
                .filter(s -> qcStatusName.equals(s.getName()))
                .findFirst()
                .map(s -> s.getId())
                .orElseThrow(() -> new IllegalStateException(qcStatusName + " QC status not found"));
            inspectionRepository.updateStatus(inspection.getId(), failStatusId);
        }
    }
}
