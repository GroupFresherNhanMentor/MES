package fpt.qn.mes.quality.application.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fpt.qn.mes.auth.application.port.out.CurrentUserPort;
import fpt.qn.mes.common.dto.response.PageResponse;
import fpt.qn.mes.quality.application.dto.request.CreateDefectTypeRequest;
import fpt.qn.mes.quality.application.dto.request.CreateLookupRequest;
import fpt.qn.mes.quality.application.dto.request.CreateQualityInspectionRequest;
import fpt.qn.mes.quality.application.dto.request.FailQcRequest;
import fpt.qn.mes.quality.application.dto.request.PassQcRequest;
import fpt.qn.mes.quality.application.dto.response.DefectTypeDto;
import fpt.qn.mes.quality.application.dto.response.FailQcResponse;
import fpt.qn.mes.quality.application.dto.response.PassQcResponse;
import fpt.qn.mes.quality.application.dto.response.QcActionDto;
import fpt.qn.mes.quality.application.dto.response.QcStatusDto;
import fpt.qn.mes.quality.application.dto.response.QualityInspectionDto;
import fpt.qn.mes.quality.application.exception.DefectTypeRequiredException;
import fpt.qn.mes.quality.application.exception.InspectionAlreadyClosedException;
import fpt.qn.mes.quality.application.exception.InsufficientRemainingQuantityException;
import fpt.qn.mes.quality.application.exception.InvalidQcActionException;
import fpt.qn.mes.quality.application.exception.QualityInspectionNotFoundException;
import fpt.qn.mes.quality.application.exception.ReasonRequiredException;
import fpt.qn.mes.quality.application.mapper.DefectTypeDtoMapper;
import fpt.qn.mes.quality.application.mapper.QcActionDtoMapper;
import fpt.qn.mes.quality.application.mapper.QcStatusDtoMapper;
import fpt.qn.mes.quality.application.mapper.QualityDtoMapper;
import fpt.qn.mes.quality.application.port.in.QualityUseCase;
import fpt.qn.mes.quality.application.port.out.QcStockPort;
import fpt.qn.mes.quality.domain.entities.DefectType;
import fpt.qn.mes.quality.domain.entities.QcAction;
import fpt.qn.mes.quality.domain.entities.QcStatus;
import fpt.qn.mes.quality.domain.entities.QualityInspection;
import fpt.qn.mes.quality.domain.entities.QualityInspectionResult;
import fpt.qn.mes.quality.domain.repository.DefectTypeRepository;
import fpt.qn.mes.quality.domain.repository.QcActionRepository;
import fpt.qn.mes.quality.domain.repository.QcStatusRepository;
import fpt.qn.mes.quality.domain.repository.QualityInspectionRepository;
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
    DefectTypeRepository defectTypeRepository;
    QcStockPort qcStockPort;
    QualityDtoMapper qualityMapper;
    QcStatusDtoMapper qcStatusMapper;
    QcActionDtoMapper qcActionMapper;
    DefectTypeDtoMapper defectTypeMapper;
    CurrentUserPort currentUserPort;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<QualityInspectionDto> getInspections(int page, int size) {
        var result = inspectionRepository.findAll(page, size);
        int totalPages = size > 0 ? (int) Math.ceil((double) result.getTotal() / size) : 0;
        return PageResponse.<QualityInspectionDto>builder()
            .items(result.getItems().stream().map(this::toDtoWithStatusName).toList())
            .totalElements(result.getTotal())
            .totalPages(totalPages)
            .pageNumber(page)
            .pageSize(size)
            .build();
    }

    @Override
    @Transactional(readOnly = true)
    public QualityInspectionDto getInspectionById(UUID id) {
        return inspectionRepository.findById(id)
            .map(this::toDtoWithStatusName)
            .orElseThrow(() -> new QualityInspectionNotFoundException("QC inspection not found: " + id));
    }

    @Override
    @Transactional
    public QualityInspectionDto createInspection(CreateQualityInspectionRequest req) {
        UUID currentUserId = currentUserPort.getCurrentUserId();
        QualityInspection inspection = QualityInspection.create(
            req.getWorkOrderId(), req.getProductId(), req.getLotId(),
            req.getQuantity(), req.getQcStatusId());
        return toDtoWithStatusName(inspectionRepository.save(inspection));
    }

    @Override
    @Transactional
    public void deleteInspection(UUID id) {
        if (inspectionRepository.findById(id).isEmpty()) {
            throw new QualityInspectionNotFoundException("QC inspection not found: " + id);
        }
        inspectionRepository.deleteById(id);
    }

    @Override
    @Transactional
    public PassQcResponse passInspection(UUID inspectionId, PassQcRequest request) {
        QualityInspection inspection = inspectionRepository.findById(inspectionId)
            .orElseThrow(() -> new QualityInspectionNotFoundException("QC inspection not found: " + inspectionId));

        BigDecimal processed = inspectionRepository.sumResultQuantities(inspectionId);
        BigDecimal remaining = inspection.getQuantity().subtract(processed);

        if (remaining.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InspectionAlreadyClosedException("Inspection " + inspectionId + " is already fully processed");
        }

        BigDecimal passedQty = request.getPassedQuantity();
        if (passedQty.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InsufficientRemainingQuantityException("passedQuantity must be > 0");
        }

        if (passedQty.compareTo(remaining) > 0) {
            throw new InsufficientRemainingQuantityException(
                "Passed quantity " + passedQty + " exceeds remaining " + remaining);
        }

        UUID currentUserId = currentUserPort.getCurrentUserId();

        QualityInspectionResult result = QualityInspectionResult.create(
            inspectionId, true, passedQty, null, null, null, currentUserId, request.getNote());
        inspectionRepository.saveResult(result);

        UUID fromStockStatusId = qcStockPort.getQualityInspectionStatusId();
        UUID toStockStatusId = qcStockPort.getAvailableStatusId();
        UUID movementTypeId = qcStockPort.getQcReleaseMovementTypeId();

        UUID stockMovementId = qcStockPort.transferStock(
            inspection.getLotId(), inspection.getProductId(), passedQty,
            fromStockStatusId, toStockStatusId, movementTypeId,
            inspection.getWorkOrderId(), currentUserId);

        BigDecimal newRemaining = remaining.subtract(passedQty);
        String qcStatusName = "PASSED";
        if (newRemaining.compareTo(BigDecimal.ZERO) <= 0) {
            UUID passedStatusId = qcStatusRepository.findAll().stream()
                .filter(s -> "PASSED".equals(s.getName()))
                .findFirst()
                .map(QcStatus::getId)
                .orElseThrow(() -> new IllegalStateException("PASSED QC status not found"));
            inspectionRepository.updateStatus(inspection.getId(), passedStatusId);
        } else {
            qcStatusName = "PENDING_INSPECTION";
        }

        return PassQcResponse.builder()
            .resultId(result.getId())
            .qcStatusName(qcStatusName)
            .stockMovementId(stockMovementId)
            .message("Pass QC thành công")
            .build();
    }

    @Override
    @Transactional
    public FailQcResponse failInspection(UUID inspectionId, FailQcRequest request) {
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

        QualityInspectionResult result = QualityInspectionResult.create(
            inspectionId, false, failedQty,
            request.getDefectTypeId(), request.getReason(),
            request.getActionId(), currentUserId, request.getNote());
        inspectionRepository.saveResult(result);

        UUID fromStockStatusId = qcStockPort.getQualityInspectionStatusId();
        UUID stockMovementId = null;
        String qcStatusName;

        switch (actionName) {
            case "SCRAP":
                qcStatusName = "FAILED";
                stockMovementId = qcStockPort.transferStock(
                    inspection.getLotId(), inspection.getProductId(), failedQty,
                    fromStockStatusId, qcStockPort.getScrappedStatusId(),
                    qcStockPort.getScrapMovementTypeId(),
                    inspection.getWorkOrderId(), currentUserId);
                break;
            case "HOLD":
                qcStatusName = "ON_HOLD";
                stockMovementId = qcStockPort.transferStock(
                    inspection.getLotId(), inspection.getProductId(), failedQty,
                    fromStockStatusId, qcStockPort.getOnHoldStatusId(),
                    qcStockPort.getQcHoldMovementTypeId(),
                    inspection.getWorkOrderId(), currentUserId);
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
                .map(QcStatus::getId)
                .orElseThrow(() -> new IllegalStateException(qcStatusName + " QC status not found"));
            inspectionRepository.updateStatus(inspection.getId(), failStatusId);
        }

        return FailQcResponse.builder()
            .resultId(result.getId())
            .qcStatusName(newRemaining.compareTo(BigDecimal.ZERO) <= 0 ? qcStatusName : "PENDING_INSPECTION")
            .stockMovementId(stockMovementId)
            .message("Fail QC thành công")
            .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<QcStatusDto> getQcStatuses() {
        return qcStatusRepository.findAll().stream()
            .map(s -> qcStatusMapper.toDto(s))
            .toList();
    }

    @Override
    @Transactional
    public QcStatusDto createQcStatus(CreateLookupRequest request) {
        QcStatus status = QcStatus.builder()
            .id(UUID.randomUUID())
            .name(request.getName())
            .description(request.getDescription())
            .build();
        return qcStatusMapper.toDto(qcStatusRepository.save(status));
    }

    @Override
    @Transactional(readOnly = true)
    public List<QcActionDto> getQcActions() {
        return qcActionRepository.findAll().stream()
            .map(a -> qcActionMapper.toDto(a))
            .toList();
    }

    @Override
    @Transactional
    public QcActionDto createQcAction(CreateLookupRequest request) {
        QcAction action = QcAction.builder()
            .id(UUID.randomUUID())
            .name(request.getName())
            .description(request.getDescription())
            .build();
        return qcActionMapper.toDto(qcActionRepository.save(action));
    }

    @Override
    @Transactional(readOnly = true)
    public List<DefectTypeDto> getDefectTypes() {
        return defectTypeRepository.findAll().stream()
            .map(d -> defectTypeMapper.toDto(d))
            .toList();
    }

    @Override
    @Transactional
    public DefectTypeDto createDefectType(CreateDefectTypeRequest request) {
        DefectType defectType = DefectType.builder()
            .id(UUID.randomUUID())
            .name(request.getName())
            .description(request.getDescription())
            .build();
        return defectTypeMapper.toDto(defectTypeRepository.save(defectType));
    }

    private QualityInspectionDto toDtoWithStatusName(QualityInspection inspection) {
        QualityInspectionDto dto = qualityMapper.toDto(inspection);
        String statusName = qcStatusRepository.findById(inspection.getQcStatusId())
            .map(QcStatus::getName)
            .orElse(null);
        BigDecimal remaining = inspection.getId() != null
            ? inspection.getQuantity().subtract(inspectionRepository.sumResultQuantities(inspection.getId()))
            : inspection.getQuantity();

        return QualityInspectionDto.builder()
            .id(dto.getId())
            .workOrderId(dto.getWorkOrderId())
            .productId(dto.getProductId())
            .productCode(dto.getProductCode())
            .lotId(dto.getLotId())
            .lotNumber(dto.getLotNumber())
            .quantity(dto.getQuantity())
            .remainingQuantity(remaining)
            .qcStatusId(dto.getQcStatusId())
            .qcStatusName(statusName)
            .createdAt(dto.getCreatedAt())
            .results(dto.getResults())
            .build();
    }
}
