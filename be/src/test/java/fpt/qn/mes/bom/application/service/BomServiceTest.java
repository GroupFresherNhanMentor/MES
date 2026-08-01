package fpt.qn.mes.bom.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

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
import fpt.qn.mes.bom.domain.entities.Bom;
import fpt.qn.mes.bom.domain.entities.BomItem;
import fpt.qn.mes.bom.domain.entities.BomStatus;
import fpt.qn.mes.bom.domain.repository.BomItemRepository;
import fpt.qn.mes.bom.domain.repository.BomRepository;
import fpt.qn.mes.bom.domain.repository.BomStatusRepository;
import fpt.qn.mes.bom.domain.repository.criteria.BomSearchCriteria;
import fpt.qn.mes.common.domainQuery.PaginationResult;
import fpt.qn.mes.common.dto.response.PageResponse;
import fpt.qn.mes.common.port.out.JsonSerializerPort;
import fpt.qn.mes.master.product.application.dto.product.ProductResponse;
import fpt.qn.mes.master.product.application.exception.ProductNotFoundException;
import fpt.qn.mes.master.product.application.port.in.ProductUseCase;

@ExtendWith(MockitoExtension.class)
class BomServiceTest {

    @Mock BomRepository bomRepository;
    @Mock BomItemRepository bomItemRepository;
    @Mock BomStatusRepository bomStatusRepository;
    @Mock BomDtoMapper mapper;
    @Mock ProductUseCase productUseCase;
    @Mock CurrentUserPort currentUserPort;
    @Mock JsonSerializerPort jsonSerializer;
    @Mock ApplicationEventPublisher eventPublisher;

    @InjectMocks BomService bomService;

    UUID finishedProductId;
    UUID userId;
    BomStatus draftStatus;
    BomStatus activeStatus;
    BomStatus inactiveStatus;

    @BeforeEach
    void setUp() {
        finishedProductId = UUID.randomUUID();
        userId = UUID.randomUUID();
        draftStatus   = BomStatus.builder().id(UUID.fromString("00000000-0000-0000-0000-000000000001")).name("DRAFT").build();
        activeStatus  = BomStatus.builder().id(UUID.fromString("00000000-0000-0000-0000-000000000002")).name("ACTIVE").build();
        inactiveStatus = BomStatus.builder().id(UUID.fromString("00000000-0000-0000-0000-000000000003")).name("INACTIVE").build();
    }

    // ── getBoms ──────────────────────────────────────────────────────────────

    @Test
    void getBoms_returnsPagedResponse() {
        Bom bom = Bom.builder()
                .id(UUID.randomUUID())
                .finishedProductId(finishedProductId)
                .version(1)
                .bomStatus(activeStatus)
                .createdBy(Bom.UserRef.builder().id(userId).build())
                .createdAt(Instant.now())
                .items(Collections.emptyList())
                .build();

        BomResponse dto = BomResponse.builder()
                .id(bom.getId())
                .finishedProductId(finishedProductId)
                .version(1)
                .bomStatus(BomResponse.BomStatusInfo.builder().id(activeStatus.getId()).name("ACTIVE").build())
                .createdAt(bom.getCreatedAt())
                .build();

        PaginationResult<Bom> paginationResult = new PaginationResult<>(1, List.of(bom));
        when(bomRepository.search(any(BomSearchCriteria.class))).thenReturn(paginationResult);
        when(mapper.toDto(bom)).thenReturn(dto);

        BomSearchRequest request = new BomSearchRequest();
        request.setPage(0);
        request.setSize(10);
        PageResponse<BomResponse> response = bomService.getBoms(request);

        assertThat(response).isNotNull();
        assertThat(response.getTotalElements()).isEqualTo(1);
        assertThat(response.getItems()).hasSize(1);
        assertThat(response.getItems().get(0).getId()).isEqualTo(bom.getId());
    }

    // ── getBomById ────────────────────────────────────────────────────────────

    @Test
    void getBomById_returnsBomWhenFound() {
        UUID bomId = UUID.randomUUID();
        Bom bom = Bom.builder()
                .id(bomId)
                .finishedProductId(finishedProductId)
                .version(1)
                .bomStatus(activeStatus)
                .createdBy(Bom.UserRef.builder().id(userId).build())
                .createdAt(Instant.now())
                .items(Collections.emptyList())
                .build();

        BomResponse dto = BomResponse.builder().id(bomId).version(1).build();
        when(bomRepository.findById(bomId)).thenReturn(Optional.of(bom));
        when(mapper.toDto(bom)).thenReturn(dto);

        BomResponse result = bomService.getBomById(bomId);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(bomId);
    }

    @Test
    void getBomById_throwsNotFound_whenMissing() {
        UUID bomId = UUID.randomUUID();
        when(bomRepository.findById(bomId)).thenReturn(Optional.empty());

        assertThrows(BomNotFoundException.class, () -> bomService.getBomById(bomId));
    }

    // ── createBom ─────────────────────────────────────────────────────────────

    @Test
    void createBom_savesWithDraftStatus_whenVersionAvailable() {
        CreateBomRequest request = new CreateBomRequest();
        request.setFinishedProductId(finishedProductId);
        request.setVersion(1);

        when(productUseCase.getProductById(finishedProductId))
                .thenReturn(ProductResponse.builder().id(finishedProductId).build());
        when(bomRepository.existsByFinishedProductIdAndVersion(finishedProductId, 1)).thenReturn(false);
        when(bomRepository.findMaxVersionByFinishedProductId(finishedProductId)).thenReturn(0);
        when(bomStatusRepository.findByName("DRAFT")).thenReturn(Optional.of(draftStatus));
        when(currentUserPort.getCurrentUserId()).thenReturn(userId);
        when(bomRepository.save(any(Bom.class))).thenAnswer(inv -> inv.getArgument(0));

        bomService.createBom(request);

        verify(bomRepository).save(any(Bom.class));
    }

    @Test
    void createBom_autoIncrementsVersion_whenVersionConflicts() {
        CreateBomRequest request = new CreateBomRequest();
        request.setFinishedProductId(finishedProductId);
        request.setVersion(1);

        when(productUseCase.getProductById(finishedProductId))
                .thenReturn(ProductResponse.builder().id(finishedProductId).build());
        when(bomRepository.existsByFinishedProductIdAndVersion(finishedProductId, 1)).thenReturn(true);
        when(bomRepository.findMaxVersionByFinishedProductId(finishedProductId)).thenReturn(1);
        when(bomStatusRepository.findByName("DRAFT")).thenReturn(Optional.of(draftStatus));
        when(currentUserPort.getCurrentUserId()).thenReturn(userId);
        when(bomRepository.save(any(Bom.class))).thenAnswer(inv -> inv.getArgument(0));

        bomService.createBom(request);

        verify(bomRepository).save(any(Bom.class));
    }

    @Test
    void createBom_throwsProductNotFound_whenProductMissing() {
        CreateBomRequest request = new CreateBomRequest();
        request.setFinishedProductId(finishedProductId);
        when(productUseCase.getProductById(finishedProductId))
                .thenThrow(new ProductNotFoundException("Product not found: " + finishedProductId));

        assertThrows(ProductNotFoundException.class, () -> bomService.createBom(request));
        verify(bomRepository, never()).save(any(Bom.class));
    }

    @Test
    void createBom_throwsBomStatusNotFound_whenDraftStatusMissing() {
        CreateBomRequest request = new CreateBomRequest();
        request.setFinishedProductId(finishedProductId);
        request.setVersion(1);

        when(productUseCase.getProductById(finishedProductId))
                .thenReturn(ProductResponse.builder().id(finishedProductId).build());
        when(bomRepository.existsByFinishedProductIdAndVersion(finishedProductId, 1)).thenReturn(false);
        when(bomRepository.findMaxVersionByFinishedProductId(finishedProductId)).thenReturn(0);
        when(bomStatusRepository.findByName("DRAFT")).thenReturn(Optional.empty());

        assertThrows(BomStatusNotFoundException.class, () -> bomService.createBom(request));
        verify(bomRepository, never()).save(any(Bom.class));
    }

    // ── activateBom ───────────────────────────────────────────────────────────

    @Test
    void activateBom_transitionsToActive_andDeactivatesOthers() {
        UUID bomId = UUID.randomUUID();
        BomItem item = BomItem.builder().id(UUID.randomUUID()).bomId(bomId)
                .materialProductId(UUID.randomUUID()).build();
        Bom draftBom = Bom.builder()
                .id(bomId)
                .finishedProductId(finishedProductId)
                .version(1)
                .bomStatus(draftStatus)
                .createdBy(Bom.UserRef.builder().id(userId).build())
                .createdAt(Instant.now())
                .items(List.of(item))
                .build();

        when(bomRepository.findById(bomId)).thenReturn(Optional.of(draftBom));
        when(bomStatusRepository.findByName("DRAFT")).thenReturn(Optional.of(draftStatus));
        when(bomStatusRepository.findByName("ACTIVE")).thenReturn(Optional.of(activeStatus));
        when(bomStatusRepository.findByName("INACTIVE")).thenReturn(Optional.of(inactiveStatus));
        when(jsonSerializer.toJson(any())).thenReturn("{}");
        when(currentUserPort.getCurrentUserId()).thenReturn(userId);
        when(bomRepository.update(any(Bom.class))).thenAnswer(inv -> inv.getArgument(0));

        bomService.activateBom(bomId);

        verify(bomRepository).deactivateActiveBomsForProduct(finishedProductId, activeStatus.getId(), inactiveStatus.getId());
        verify(bomRepository).update(any(Bom.class));
        verify(eventPublisher).publishEvent(any(AuditEvent.class));
    }

    @Test
    void activateBom_throwsEmptyBom_whenNoItems() {
        UUID bomId = UUID.randomUUID();
        Bom emptyDraft = Bom.builder()
                .id(bomId)
                .finishedProductId(finishedProductId)
                .version(1)
                .bomStatus(draftStatus)
                .createdAt(Instant.now())
                .items(Collections.emptyList())
                .build();

        when(bomRepository.findById(bomId)).thenReturn(Optional.of(emptyDraft));
        when(bomStatusRepository.findByName("DRAFT")).thenReturn(Optional.of(draftStatus));
        when(bomStatusRepository.findByName("ACTIVE")).thenReturn(Optional.of(activeStatus));
        when(bomStatusRepository.findByName("INACTIVE")).thenReturn(Optional.of(inactiveStatus));

        assertThrows(EmptyBomException.class, () -> bomService.activateBom(bomId));
        verify(bomRepository, never()).update(any(Bom.class));
    }

    @Test
    void activateBom_throwsInvalidStatus_whenNotDraft() {
        UUID bomId = UUID.randomUUID();
        Bom activeBom = Bom.builder()
                .id(bomId)
                .finishedProductId(finishedProductId)
                .version(1)
                .bomStatus(activeStatus)
                .createdAt(Instant.now())
                .items(List.of(BomItem.builder().id(UUID.randomUUID()).build()))
                .build();

        when(bomRepository.findById(bomId)).thenReturn(Optional.of(activeBom));
        when(bomStatusRepository.findByName("DRAFT")).thenReturn(Optional.of(draftStatus));
        when(bomStatusRepository.findByName("ACTIVE")).thenReturn(Optional.of(activeStatus));
        when(bomStatusRepository.findByName("INACTIVE")).thenReturn(Optional.of(inactiveStatus));

        assertThrows(InvalidBomStatusException.class, () -> bomService.activateBom(bomId));
        verify(bomRepository, never()).update(any(Bom.class));
    }

    @Test
    void activateBom_throwsNotFound_whenBomMissing() {
        UUID bomId = UUID.randomUUID();
        when(bomRepository.findById(bomId)).thenReturn(Optional.empty());

        assertThrows(BomNotFoundException.class, () -> bomService.activateBom(bomId));
    }

    // ── deactivateBom ─────────────────────────────────────────────────────────

    @Test
    void deactivateBom_transitionsToInactive() {
        UUID bomId = UUID.randomUUID();
        Bom activeBom = Bom.builder()
                .id(bomId)
                .finishedProductId(finishedProductId)
                .version(1)
                .bomStatus(activeStatus)
                .createdAt(Instant.now())
                .items(Collections.emptyList())
                .build();

        when(bomRepository.findById(bomId)).thenReturn(Optional.of(activeBom));
        when(bomStatusRepository.findByName("ACTIVE")).thenReturn(Optional.of(activeStatus));
        when(bomStatusRepository.findByName("INACTIVE")).thenReturn(Optional.of(inactiveStatus));
        when(jsonSerializer.toJson(any())).thenReturn("{}");
        when(currentUserPort.getCurrentUserId()).thenReturn(userId);
        when(bomRepository.update(any(Bom.class))).thenAnswer(inv -> inv.getArgument(0));

        bomService.deactivateBom(bomId);

        verify(bomRepository).update(any(Bom.class));
        verify(eventPublisher).publishEvent(any(AuditEvent.class));
    }

    @Test
    void deactivateBom_throwsInvalidStatus_whenNotActive() {
        UUID bomId = UUID.randomUUID();
        Bom draftBom = Bom.builder()
                .id(bomId)
                .finishedProductId(finishedProductId)
                .version(1)
                .bomStatus(draftStatus)
                .createdAt(Instant.now())
                .items(Collections.emptyList())
                .build();

        when(bomRepository.findById(bomId)).thenReturn(Optional.of(draftBom));
        when(bomStatusRepository.findByName("ACTIVE")).thenReturn(Optional.of(activeStatus));
        when(bomStatusRepository.findByName("INACTIVE")).thenReturn(Optional.of(inactiveStatus));

        assertThrows(InvalidBomStatusException.class, () -> bomService.deactivateBom(bomId));
        verify(bomRepository, never()).update(any(Bom.class));
    }

    @Test
    void deactivateBom_throwsNotFound_whenBomMissing() {
        UUID bomId = UUID.randomUUID();
        when(bomRepository.findById(bomId)).thenReturn(Optional.empty());

        assertThrows(BomNotFoundException.class, () -> bomService.deactivateBom(bomId));
    }

    // ── createNewVersion ──────────────────────────────────────────────────────

    @Test
    void createNewVersion_copiesItemsToNewDraft() {
        UUID sourceBomId = UUID.randomUUID();
        UUID materialId = UUID.randomUUID();

        BomItem sourceItem = BomItem.builder()
                .id(UUID.randomUUID())
                .bomId(sourceBomId)
                .materialProductId(materialId)
                .build();

        Bom sourceBom = Bom.builder()
                .id(sourceBomId)
                .finishedProductId(finishedProductId)
                .version(1)
                .bomStatus(activeStatus)
                .createdBy(Bom.UserRef.builder().id(userId).build())
                .createdAt(Instant.now())
                .items(new ArrayList<>(List.of(sourceItem)))
                .build();

        UUID newBomId = UUID.randomUUID();
        Bom savedBom = Bom.builder()
                .id(newBomId)
                .finishedProductId(finishedProductId)
                .version(2)
                .bomStatus(draftStatus)
                .createdBy(Bom.UserRef.builder().id(userId).build())
                .createdAt(Instant.now())
                .items(Collections.emptyList())
                .build();

        BomResponse dto = BomResponse.builder()
                .id(newBomId)
                .finishedProductId(finishedProductId)
                .version(2)
                .bomStatus(BomResponse.BomStatusInfo.builder().id(draftStatus.getId()).name("DRAFT").build())
                .build();

        when(bomRepository.findById(sourceBomId)).thenReturn(Optional.of(sourceBom));
        when(bomStatusRepository.findByName("DRAFT")).thenReturn(Optional.of(draftStatus));
        when(bomRepository.findMaxVersionByFinishedProductId(finishedProductId)).thenReturn(1);
        when(currentUserPort.getCurrentUserId()).thenReturn(userId);
        when(bomRepository.save(any(Bom.class))).thenReturn(savedBom);
        when(bomItemRepository.save(any(BomItem.class))).thenAnswer(inv -> inv.getArgument(0));
        when(bomRepository.findById(newBomId)).thenReturn(Optional.of(savedBom));
        when(mapper.toDto(savedBom)).thenReturn(dto);

        BomResponse result = bomService.createNewVersion(sourceBomId);

        assertThat(result).isNotNull();
        assertThat(result.getVersion()).isEqualTo(2);
        verify(bomRepository).save(any(Bom.class));
        verify(bomItemRepository).save(any(BomItem.class));
    }

    @Test
    void createNewVersion_throwsNotFound_whenSourceMissing() {
        UUID randomId = UUID.randomUUID();
        when(bomRepository.findById(randomId)).thenReturn(Optional.empty());

        assertThrows(BomNotFoundException.class, () -> bomService.createNewVersion(randomId));
        verify(bomRepository, never()).save(any(Bom.class));
    }
}
