package fpt.qn.mes.quality.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import fpt.qn.mes.auth.application.port.out.CurrentUserPort;
import fpt.qn.mes.quality.application.dto.request.CreateLookupRequest;
import fpt.qn.mes.quality.application.dto.request.FailQcRequest;
import fpt.qn.mes.quality.application.dto.request.PassQcRequest;
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
import fpt.qn.mes.quality.application.port.out.QcStockPort;
import fpt.qn.mes.quality.domain.entities.DefectType;
import fpt.qn.mes.quality.domain.entities.QcAction;
import fpt.qn.mes.quality.domain.entities.QcStatus;
import fpt.qn.mes.quality.domain.entities.QualityInspection;
import fpt.qn.mes.quality.domain.repository.DefectTypeRepository;
import fpt.qn.mes.quality.domain.repository.QcActionRepository;
import fpt.qn.mes.quality.domain.repository.QcStatusRepository;
import fpt.qn.mes.quality.domain.repository.QualityInspectionRepository;

@ExtendWith(MockitoExtension.class)
class QualityServiceTest {

    @Mock QualityInspectionRepository inspectionRepository;
    @Mock QcStatusRepository qcStatusRepository;
    @Mock QcActionRepository qcActionRepository;
    @Mock DefectTypeRepository defectTypeRepository;
    @Mock QcStockPort qcStockPort;
    @Mock QualityDtoMapper qualityMapper;
    @Mock QcStatusDtoMapper qcStatusMapper;
    @Mock QcActionDtoMapper qcActionMapper;
    @Mock DefectTypeDtoMapper defectTypeMapper;
    @Mock CurrentUserPort currentUserPort;

    @InjectMocks QualityService qualityService;

    UUID inspectionId;
    UUID userId;
    UUID pendingStatusId;
    UUID passedStatusId;
    UUID qcInspectionStatusId;
    UUID availableStatusId;
    UUID qcPassMovementTypeId;
    UUID scrapActionId;
    UUID scrapStatusId;
    UUID scrapMovementTypeId;
    UUID holdActionId;
    UUID holdStatusId;
    UUID qcHoldMovementTypeId;
    UUID reworkActionId;
    UUID reworkStatusId;
    UUID defectTypeId;

    @BeforeEach
    void setUp() {
        inspectionId = UUID.randomUUID();
        userId = UUID.randomUUID();
        pendingStatusId = UUID.randomUUID();
        passedStatusId = UUID.randomUUID();
        qcInspectionStatusId = UUID.randomUUID();
        availableStatusId = UUID.randomUUID();
        qcPassMovementTypeId = UUID.randomUUID();
        scrapActionId = UUID.randomUUID();
        scrapStatusId = UUID.randomUUID();
        scrapMovementTypeId = UUID.randomUUID();
        holdActionId = UUID.randomUUID();
        holdStatusId = UUID.randomUUID();
        qcHoldMovementTypeId = UUID.randomUUID();
        reworkActionId = UUID.randomUUID();
        reworkStatusId = UUID.randomUUID();
        defectTypeId = UUID.randomUUID();
    }

    @Test
    void getInspectionById_throwsNotFound_whenInspectionDoesNotExist() {
        when(inspectionRepository.findById(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> qualityService.getInspectionById(inspectionId))
            .isInstanceOf(QualityInspectionNotFoundException.class);
    }

    @Test
    void passInspection_throwsNotFound_whenInspectionDoesNotExist() {
        when(inspectionRepository.findById(any())).thenReturn(Optional.empty());

        PassQcRequest request = new PassQcRequest();
        request.setPassedQuantity(BigDecimal.TEN);

        assertThatThrownBy(() -> qualityService.passInspection(inspectionId, request))
            .isInstanceOf(QualityInspectionNotFoundException.class);
    }

    @Test
    void passInspection_throwsInsufficient_whenPassExceedsRemaining() {
        QualityInspection inspection = createInspection(BigDecimal.valueOf(100));
        when(inspectionRepository.findById(inspectionId)).thenReturn(Optional.of(inspection));
        when(inspectionRepository.sumResultQuantities(inspectionId)).thenReturn(BigDecimal.ZERO);

        PassQcRequest request = new PassQcRequest();
        request.setPassedQuantity(BigDecimal.valueOf(200));

        assertThatThrownBy(() -> qualityService.passInspection(inspectionId, request))
            .isInstanceOf(InsufficientRemainingQuantityException.class);
    }

    @Test
    void passInspection_throwsAlreadyClosed_whenFullyProcessed() {
        QualityInspection inspection = createInspection(BigDecimal.ZERO);
        when(inspectionRepository.findById(inspectionId)).thenReturn(Optional.of(inspection));
        when(inspectionRepository.sumResultQuantities(inspectionId)).thenReturn(BigDecimal.ZERO);

        PassQcRequest request = new PassQcRequest();
        request.setPassedQuantity(BigDecimal.TEN);

        assertThatThrownBy(() -> qualityService.passInspection(inspectionId, request))
            .isInstanceOf(InspectionAlreadyClosedException.class);
    }

    @Test
    void passInspection_succeeds_whenValid() {
        QualityInspection inspection = createInspection(BigDecimal.valueOf(100));
        when(inspectionRepository.findById(inspectionId)).thenReturn(Optional.of(inspection));
        when(inspectionRepository.sumResultQuantities(inspectionId)).thenReturn(BigDecimal.ZERO);
        when(currentUserPort.getCurrentUserId()).thenReturn(userId);
        when(inspectionRepository.saveResult(any())).thenReturn(null);
        when(qcStockPort.getQualityInspectionStatusId()).thenReturn(qcInspectionStatusId);
        when(qcStockPort.getAvailableStatusId()).thenReturn(availableStatusId);
        when(qcStockPort.getQcReleaseMovementTypeId()).thenReturn(qcPassMovementTypeId);
        when(qcStockPort.transferStock(any(), any(), any(), any(), any(), any(), any(), any()))
            .thenReturn(UUID.randomUUID());
        when(qcStatusRepository.findAll()).thenReturn(List.of(
            QcStatus.builder().id(passedStatusId).name("PASSED").build()
        ));

        PassQcRequest request = new PassQcRequest();
        request.setPassedQuantity(BigDecimal.valueOf(100));

        var response = qualityService.passInspection(inspectionId, request);

        assertThat(response.getQcStatusName()).isEqualTo("PASSED");
        assertThat(response.getResultId()).isNotNull();
        assertThat(response.getStockMovementId()).isNotNull();
        verify(inspectionRepository).saveResult(any());
        verify(inspectionRepository).updateStatus(inspectionId, passedStatusId);
    }

    @Test
    void failInspection_throwsNotFound_whenInspectionDoesNotExist() {
        when(inspectionRepository.findById(any())).thenReturn(Optional.empty());

        FailQcRequest request = new FailQcRequest();
        request.setFailedQuantity(BigDecimal.TEN);
        request.setActionId(scrapActionId);
        request.setDefectTypeId(defectTypeId);
        request.setReason("Scratch defect");

        assertThatThrownBy(() -> qualityService.failInspection(inspectionId, request))
            .isInstanceOf(QualityInspectionNotFoundException.class);
    }

    @Test
    void failInspection_throwsDefectTypeRequired_whenMissingDefectType() {
        QualityInspection inspection = createInspection(BigDecimal.valueOf(100));
        when(inspectionRepository.findById(inspectionId)).thenReturn(Optional.of(inspection));
        when(inspectionRepository.sumResultQuantities(inspectionId)).thenReturn(BigDecimal.ZERO);

        FailQcRequest request = new FailQcRequest();
        request.setFailedQuantity(BigDecimal.TEN);
        request.setActionId(scrapActionId);
        request.setDefectTypeId(null);
        request.setReason("Scratch");

        assertThatThrownBy(() -> qualityService.failInspection(inspectionId, request))
            .isInstanceOf(DefectTypeRequiredException.class);
    }

    @Test
    void failInspection_throwsReasonRequired_whenMissingReason() {
        QualityInspection inspection = createInspection(BigDecimal.valueOf(100));
        when(inspectionRepository.findById(inspectionId)).thenReturn(Optional.of(inspection));
        when(inspectionRepository.sumResultQuantities(inspectionId)).thenReturn(BigDecimal.ZERO);

        FailQcRequest request = new FailQcRequest();
        request.setFailedQuantity(BigDecimal.TEN);
        request.setActionId(scrapActionId);
        request.setDefectTypeId(defectTypeId);
        request.setReason(null);

        assertThatThrownBy(() -> qualityService.failInspection(inspectionId, request))
            .isInstanceOf(ReasonRequiredException.class);
    }

    @Test
    void failInspection_throwsInvalidAction_whenActionNotFound() {
        QualityInspection inspection = createInspection(BigDecimal.valueOf(100));
        when(inspectionRepository.findById(inspectionId)).thenReturn(Optional.of(inspection));
        when(inspectionRepository.sumResultQuantities(inspectionId)).thenReturn(BigDecimal.ZERO);
        when(qcActionRepository.findById(scrapActionId)).thenReturn(Optional.empty());

        FailQcRequest request = new FailQcRequest();
        request.setFailedQuantity(BigDecimal.TEN);
        request.setActionId(scrapActionId);
        request.setDefectTypeId(defectTypeId);
        request.setReason("Scratch defect");

        assertThatThrownBy(() -> qualityService.failInspection(inspectionId, request))
            .isInstanceOf(InvalidQcActionException.class);
    }

    @Test
    void failInspection_withScrap_succeeds() {
        QualityInspection inspection = createInspection(BigDecimal.valueOf(100));
        when(inspectionRepository.findById(inspectionId)).thenReturn(Optional.of(inspection));
        when(inspectionRepository.sumResultQuantities(inspectionId)).thenReturn(BigDecimal.ZERO);
        when(currentUserPort.getCurrentUserId()).thenReturn(userId);
        when(qcActionRepository.findById(scrapActionId))
            .thenReturn(Optional.of(QcAction.builder().id(scrapActionId).name("SCRAP").build()));
        when(inspectionRepository.saveResult(any())).thenReturn(null);
        when(qcStockPort.getQualityInspectionStatusId()).thenReturn(qcInspectionStatusId);
        when(qcStockPort.getScrappedStatusId()).thenReturn(scrapStatusId);
        when(qcStockPort.getScrapMovementTypeId()).thenReturn(scrapMovementTypeId);
        when(qcStockPort.transferStock(any(), any(), any(), any(), any(), any(), any(), any()))
            .thenReturn(UUID.randomUUID());
        when(qcStockPort.getQualityInspectionStatusId()).thenReturn(qcInspectionStatusId);
        when(qcStockPort.getScrappedStatusId()).thenReturn(scrapStatusId);
        when(qcStockPort.getScrapMovementTypeId()).thenReturn(scrapMovementTypeId);
        when(qcStockPort.transferStock(any(), any(), any(), any(), any(), any(), any(), any()))
            .thenReturn(UUID.randomUUID());

        FailQcRequest request = new FailQcRequest();
        request.setFailedQuantity(BigDecimal.valueOf(100));
        request.setActionId(scrapActionId);
        request.setDefectTypeId(defectTypeId);
        request.setReason("Scratch defect");

        when(qcStatusRepository.findAll()).thenReturn(List.of(
            QcStatus.builder().id(passedStatusId).name("FAILED").build()
        ));

        var response = qualityService.failInspection(inspectionId, request);

        assertThat(response.getQcStatusName()).isEqualTo("FAILED");
        assertThat(response.getResultId()).isNotNull();
        assertThat(response.getStockMovementId()).isNotNull();
        verify(inspectionRepository).saveResult(any());
        verify(inspectionRepository).updateStatus(inspectionId, passedStatusId);
    }

    @Test
    void failInspection_withRework_succeeds_noStockMovement() {
        QualityInspection inspection = createInspection(BigDecimal.valueOf(100));
        when(inspectionRepository.findById(inspectionId)).thenReturn(Optional.of(inspection));
        when(inspectionRepository.sumResultQuantities(inspectionId)).thenReturn(BigDecimal.ZERO);
        when(currentUserPort.getCurrentUserId()).thenReturn(userId);
        when(qcActionRepository.findById(reworkActionId))
            .thenReturn(Optional.of(QcAction.builder().id(reworkActionId).name("REWORK").build()));
        when(inspectionRepository.saveResult(any())).thenReturn(null);
        when(qcStatusRepository.findAll()).thenReturn(List.of(
            QcStatus.builder().id(passedStatusId).name("REWORK_REQUIRED").build()
        ));

        FailQcRequest request = new FailQcRequest();
        request.setFailedQuantity(BigDecimal.valueOf(100));
        request.setActionId(reworkActionId);
        request.setDefectTypeId(defectTypeId);
        request.setReason("Assembly issue");

        var response = qualityService.failInspection(inspectionId, request);

        assertThat(response.getQcStatusName()).isEqualTo("REWORK_REQUIRED");
        assertThat(response.getStockMovementId()).isNull();
        verify(inspectionRepository).saveResult(any());
        verify(inspectionRepository).updateStatus(inspectionId, passedStatusId);
    }

    @Test
    void partialPass_keepsPendingInspection() {
        QualityInspection inspection = createInspection(BigDecimal.valueOf(100));
        when(inspectionRepository.findById(inspectionId)).thenReturn(Optional.of(inspection));
        when(inspectionRepository.sumResultQuantities(inspectionId)).thenReturn(BigDecimal.ZERO);
        when(currentUserPort.getCurrentUserId()).thenReturn(userId);
        when(inspectionRepository.saveResult(any())).thenReturn(null);
        when(qcStockPort.getQualityInspectionStatusId()).thenReturn(qcInspectionStatusId);
        when(qcStockPort.getAvailableStatusId()).thenReturn(availableStatusId);
        when(qcStockPort.getQcReleaseMovementTypeId()).thenReturn(qcPassMovementTypeId);
        when(qcStockPort.transferStock(any(), any(), any(), any(), any(), any(), any(), any()))
            .thenReturn(UUID.randomUUID());

        PassQcRequest request = new PassQcRequest();
        request.setPassedQuantity(BigDecimal.valueOf(30));

        var response = qualityService.passInspection(inspectionId, request);

        assertThat(response.getQcStatusName()).isEqualTo("PENDING_INSPECTION");
        verify(inspectionRepository, never()).updateStatus(any(), any());
    }

    @Test
    void getQcStatuses_returnsList() {
        when(qcStatusRepository.findAll()).thenReturn(
            List.of(QcStatus.builder().id(pendingStatusId).name("PENDING_INSPECTION").build())
        );
        when(qcStatusMapper.toDto(any())).thenReturn(null);

        var result = qualityService.getQcStatuses();

        assertThat(result).hasSize(1);
    }

    @Test
    void getDefectTypes_returnsList() {
        when(defectTypeRepository.findAll()).thenReturn(
            List.of(DefectType.builder().id(defectTypeId).name("Scratch").build())
        );
        when(defectTypeMapper.toDto(any())).thenReturn(null);

        var result = qualityService.getDefectTypes();

        assertThat(result).hasSize(1);
    }

    @Test
    void getQcActions_returnsList() {
        when(qcActionRepository.findAll()).thenReturn(
            List.of(QcAction.builder().id(scrapActionId).name("SCRAP").build())
        );
        when(qcActionMapper.toDto(any())).thenReturn(null);

        var result = qualityService.getQcActions();

        assertThat(result).hasSize(1);
    }

    private QualityInspection createInspection(BigDecimal quantity) {
        return QualityInspection.builder()
            .id(inspectionId)
            .workOrderId(UUID.randomUUID())
            .productId(UUID.randomUUID())
            .lotId(UUID.randomUUID())
            .quantity(quantity)
            .qcStatusId(pendingStatusId)
            .results(List.of())
            .build();
    }
}