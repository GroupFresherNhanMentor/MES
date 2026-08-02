package fpt.qn.mes.quality.application.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
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
import fpt.qn.mes.common.port.out.JsonSerializerPort;
import fpt.qn.mes.quality.application.port.in.QualityUseCase;
import fpt.qn.mes.quality.domain.constants.QcActionConstants;
import fpt.qn.mes.quality.domain.constants.QcStatusConstants;
import fpt.qn.mes.quality.application.port.out.QcFailStockPort;
import fpt.qn.mes.quality.application.port.out.QcReleasePort;
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
    QcReleasePort qcReleasePort;
    QcFailStockPort qcFailStockPort;
    QualityDtoMapper qualityDtoMapper;
    CurrentUserPort currentUserPort;
    ApplicationEventPublisher eventPublisher;
    JsonSerializerPort jsonSerializer;

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

        BigDecimal remaining = inspection.getRemainingQuantity();

        if (remaining.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InspectionAlreadyClosedException("Inspection " + inspectionId + " is already fully processed");
        }

        BigDecimal passedQty = request.getPassedQuantity();
        if (passedQty.compareTo(remaining) > 0) {
            throw new InsufficientRemainingQuantityException(
                    "Passed quantity " + passedQty + " exceeds remaining " + remaining);
        }

        UUID currentUserId = currentUserPort.getCurrentUserId();

        QualityInspectionResult result = QualityInspectionResult.create(
                inspectionId, true, passedQty, null, null, null, currentUserId, request.getNote());
        inspectionRepository.saveResult(result);
        inspectionRepository.decrementRemainingQuantity(inspectionId, passedQty);

        qcReleasePort.releasePassedStock(
                inspection.getLot().getId(), inspection.getProduct().getId(), passedQty,
                inspection.getWorkOrder().getId(), currentUserId);

        eventPublisher.publishEvent(AuditEvent.create(currentUserId, AuditAction.QC_PASS, "QUALITY_INSPECTION", inspectionId,
                null, jsonSerializer.toJson(Map.of("passedQuantity", passedQty)), null));

        if (remaining.subtract(passedQty).compareTo(BigDecimal.ZERO) <= 0) {
            UUID passedStatusId = qcStatusRepository.findByName(QcStatusConstants.PASSED)
                    .orElseThrow(() -> new IllegalStateException("PASSED QC status not found"))
                    .getId();
            inspectionRepository.updateStatus(inspectionId, passedStatusId);
        }
    }

    @Override
    @Transactional
    public void failInspection(UUID inspectionId, FailQcRequest request) {
        QualityInspection inspection = inspectionRepository.findById(inspectionId)
                .orElseThrow(() -> new QualityInspectionNotFoundException("QC inspection not found: " + inspectionId));

        BigDecimal remaining = inspection.getRemainingQuantity();

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
                inspectionId, false, failedQty, defectType, request.getReason(), action, currentUserId,
                request.getNote());
        inspectionRepository.saveResult(result);
        inspectionRepository.decrementRemainingQuantity(inspectionId, failedQty);

        if (eventPublisher != null) {
            AuditAction auditAction = AuditAction.QC_FAIL;
            if (QcActionConstants.HOLD.equals(actionName)) {
                auditAction = AuditAction.QC_HOLD;
            } else if (QcActionConstants.SCRAP.equals(actionName)) {
                auditAction = AuditAction.SCRAP_STOCK;
            }
            eventPublisher.publishEvent(AuditEvent.create(currentUserId, auditAction, "QUALITY_INSPECTION", inspectionId,
                    null, jsonSerializer.toJson(Map.of(
                            "failedQuantity", failedQty,
                            "reason", request.getReason(),
                            "action", actionName)), null));
        }

        String qcStatusName;

        switch (actionName) {
            case QcActionConstants.SCRAP:
                qcStatusName = QcStatusConstants.FAILED;
                qcFailStockPort.scrapStock(
                        inspection.getLot().getId(), inspection.getProduct().getId(), failedQty,
                        inspection.getWorkOrder().getId(), currentUserId);
                break;
            case QcActionConstants.HOLD:
                qcStatusName = QcStatusConstants.ON_HOLD;
                qcFailStockPort.holdStock(
                        inspection.getLot().getId(), inspection.getProduct().getId(), failedQty,
                        inspection.getWorkOrder().getId(), currentUserId);
                break;
            case QcActionConstants.REWORK:
                qcStatusName = QcStatusConstants.REWORK_REQUIRED;
                break;
            default:
                throw new InvalidQcActionException("Unknown action: " + actionName);
        }

        BigDecimal newRemaining = remaining.subtract(failedQty);
        if (newRemaining.compareTo(BigDecimal.ZERO) <= 0) {
            UUID failStatusId = qcStatusRepository.findByName(qcStatusName)
                    .orElseThrow(() -> new IllegalStateException(qcStatusName + " QC status not found"))
                    .getId();
            inspectionRepository.updateStatus(inspection.getId(), failStatusId);
        }
    }
}
