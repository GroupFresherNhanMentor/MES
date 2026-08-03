package fpt.qn.mes.quality.application.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import fpt.qn.mes.auth.application.port.out.CurrentUserPort;
import fpt.qn.mes.common.port.out.JsonSerializerPort;
import fpt.qn.mes.quality.application.dto.inspection.fail.FailQcRequest;
import fpt.qn.mes.quality.application.dto.inspection.pass.PassQcRequest;
import fpt.qn.mes.quality.application.exception.DefectTypeRequiredException;
import fpt.qn.mes.quality.application.exception.InspectionAlreadyClosedException;
import fpt.qn.mes.quality.application.exception.InsufficientRemainingQuantityException;
import fpt.qn.mes.quality.application.exception.InvalidQcActionException;
import fpt.qn.mes.quality.application.exception.QualityInspectionNotFoundException;
import fpt.qn.mes.quality.application.exception.ReasonRequiredException;
import fpt.qn.mes.quality.application.mapper.QualityDtoMapper;
import fpt.qn.mes.quality.application.port.out.QcFailStockPort;
import fpt.qn.mes.quality.application.port.out.QcReleasePort;
import fpt.qn.mes.quality.domain.entities.QcStatus;
import fpt.qn.mes.quality.domain.entities.QualityInspection;
import fpt.qn.mes.quality.domain.repository.QcActionRepository;
import fpt.qn.mes.quality.domain.repository.QcStatusRepository;
import fpt.qn.mes.quality.domain.repository.QualityInspectionRepository;

@ExtendWith(MockitoExtension.class)
class QualityServiceTest {

    @Mock QualityInspectionRepository inspectionRepository;
    @Mock QcStatusRepository qcStatusRepository;
    @Mock QcActionRepository qcActionRepository;
    @Mock QcReleasePort qcReleasePort;
    @Mock QcFailStockPort qcFailStockPort;
    @Mock QualityDtoMapper qualityDtoMapper;
    @Mock CurrentUserPort currentUserPort;
    @Mock ApplicationEventPublisher eventPublisher;
    @Mock JsonSerializerPort jsonSerializer;

    @InjectMocks QualityService qualityService;

    UUID inspectionId;
    UUID userId;
    UUID pendingStatusId;
    UUID scrapActionId;
    UUID reworkActionId;
    UUID defectTypeId;

    @BeforeEach
    void setUp() {
        inspectionId    = UUID.randomUUID();
        userId          = UUID.randomUUID();
        pendingStatusId = UUID.randomUUID();
        scrapActionId   = UUID.randomUUID();
        reworkActionId  = UUID.randomUUID();
        defectTypeId    = UUID.randomUUID();
    }

    // ── passInspection ────────────────────────────────────────────────────────

    @Test
    void passInspection_throwsNotFound_whenInspectionDoesNotExist() {
        when(inspectionRepository.findById(any())).thenReturn(Optional.empty());

        PassQcRequest request = new PassQcRequest();
        request.setPassedQuantity(BigDecimal.TEN);

        assertThatThrownBy(() -> qualityService.passInspection(inspectionId, request))
            .isInstanceOf(QualityInspectionNotFoundException.class);
    }

    // TODO: implement and uncomment once passInspection logic is finalized
    // @Test
    // void passInspection_throwsAlreadyClosed_whenFullyProcessed() { ... }

    // @Test
    // void passInspection_throwsInsufficient_whenPassExceedsRemaining() { ... }

    // @Test
    // void passInspection_succeeds_andUpdatesStatusToPassedWhenFullyProcessed() { ... }

    // @Test
    // void passInspection_partialPass_doesNotUpdateStatus() { ... }

    // ── failInspection ────────────────────────────────────────────────────────

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
    void failInspection_throwsAlreadyClosed_whenRemainingIsZero() {
        when(inspectionRepository.findById(inspectionId))
            .thenReturn(Optional.of(createInspection(BigDecimal.valueOf(100), BigDecimal.ZERO)));

        FailQcRequest request = new FailQcRequest();
        request.setFailedQuantity(BigDecimal.TEN);
        request.setActionId(scrapActionId);
        request.setDefectTypeId(defectTypeId);
        request.setReason("Scratch defect");

        assertThatThrownBy(() -> qualityService.failInspection(inspectionId, request))
            .isInstanceOf(InspectionAlreadyClosedException.class);
    }

    @Test
    void failInspection_throwsInsufficient_whenFailExceedsRemaining() {
        when(inspectionRepository.findById(inspectionId))
            .thenReturn(Optional.of(createInspection(BigDecimal.valueOf(100), BigDecimal.valueOf(100))));

        FailQcRequest request = new FailQcRequest();
        request.setFailedQuantity(BigDecimal.valueOf(200));
        request.setActionId(scrapActionId);
        request.setDefectTypeId(defectTypeId);
        request.setReason("Scratch defect");

        assertThatThrownBy(() -> qualityService.failInspection(inspectionId, request))
            .isInstanceOf(InsufficientRemainingQuantityException.class);
    }

    @Test
    void failInspection_throwsDefectTypeRequired_whenMissingDefectType() {
        when(inspectionRepository.findById(inspectionId))
            .thenReturn(Optional.of(createInspection(BigDecimal.valueOf(100), BigDecimal.valueOf(100))));

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
        when(inspectionRepository.findById(inspectionId))
            .thenReturn(Optional.of(createInspection(BigDecimal.valueOf(100), BigDecimal.valueOf(100))));

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
        when(inspectionRepository.findById(inspectionId))
            .thenReturn(Optional.of(createInspection(BigDecimal.valueOf(100), BigDecimal.valueOf(100))));
        when(qcActionRepository.findById(scrapActionId)).thenReturn(Optional.empty());

        FailQcRequest request = new FailQcRequest();
        request.setFailedQuantity(BigDecimal.TEN);
        request.setActionId(scrapActionId);
        request.setDefectTypeId(defectTypeId);
        request.setReason("Scratch defect");

        assertThatThrownBy(() -> qualityService.failInspection(inspectionId, request))
            .isInstanceOf(InvalidQcActionException.class);
    }

    // TODO: implement and uncomment once failInspection happy-path logic is finalized
    // @Test
    // void failInspection_withScrap_transfersStockAndUpdatesStatus() { ... }

    // @Test
    // void failInspection_withRework_doesNotTransferStock() { ... }

    // ── helpers ───────────────────────────────────────────────────────────────

    private QualityInspection createInspection(BigDecimal quantity, BigDecimal remainingQuantity) {
        return QualityInspection.builder()
            .id(inspectionId)
            .workOrder(QualityInspection.WorkOrderRef.builder().id(UUID.randomUUID()).build())
            .product(QualityInspection.ProductRef.builder().id(UUID.randomUUID()).build())
            .lot(QualityInspection.StockLotRef.builder().id(UUID.randomUUID()).build())
            .quantity(quantity)
            .remainingQuantity(remainingQuantity)
            .qcStatus(QcStatus.builder().id(pendingStatusId).name("PENDING_INSPECTION").build())
            .build();
    }
}
