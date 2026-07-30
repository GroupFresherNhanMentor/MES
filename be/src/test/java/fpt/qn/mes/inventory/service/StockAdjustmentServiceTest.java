package fpt.qn.mes.inventory.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import fpt.qn.mes.common.exception.AppException;
import fpt.qn.mes.inventory.application.dto.request.StockAdjustmentRequest;
import fpt.qn.mes.inventory.application.dto.response.StockAdjustmentResponse;
import fpt.qn.mes.inventory.application.dto.response.StockMovementDto;
import fpt.qn.mes.inventory.application.exception.InventoryNotFoundException;
import fpt.qn.mes.inventory.application.mapper.InventoryDtoMapper;
import fpt.qn.mes.inventory.application.mapper.StockAdjustmentApprovalDtoMapper;
import fpt.qn.mes.inventory.application.service.InventoryService;
import fpt.qn.mes.inventory.domain.constants.MovementTypeConstants;
import fpt.qn.mes.inventory.domain.entities.StockAdjustmentApproval;
import fpt.qn.mes.inventory.domain.entities.StockBalance;
import fpt.qn.mes.inventory.domain.entities.StockMovement;
import fpt.qn.mes.inventory.domain.repository.MovementTypeRepository;
import fpt.qn.mes.inventory.domain.repository.StockAdjustmentApprovalRepository;
import fpt.qn.mes.inventory.domain.repository.StockBalanceRepository;
import fpt.qn.mes.inventory.domain.repository.StockMovementRepository;
import fpt.qn.mes.master.location.application.port.in.LocationUseCase;
import fpt.qn.mes.master.product.application.port.in.ProductUseCase;
import fpt.qn.mes.master.warehouse.application.port.in.WarehouseUseCase;
import fpt.qn.mes.user.application.mapper.UserDtoMapper;
import fpt.qn.mes.user.domain.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class StockAdjustmentServiceTest {

    @Mock
    StockBalanceRepository balanceRepository;
    @Mock
    StockMovementRepository movementRepository;
    @Mock
    MovementTypeRepository movementTypeRepository;
    @Mock
    StockAdjustmentApprovalRepository approvalRepository;
    @Mock
    InventoryDtoMapper mapper;
    @Mock
    StockAdjustmentApprovalDtoMapper approvalMapper;
    @Mock
    WarehouseUseCase warehouseUseCase;
    @Mock
    ProductUseCase productUseCase;
    @Mock
    LocationUseCase locationUseCase;
    @Mock
    UserRepository userRepository;
    @Mock
    UserDtoMapper userDtoMapper;

    @InjectMocks
    InventoryService inventoryService;

    private final UUID userId = UUID.randomUUID();
    private final UUID balanceId = UUID.randomUUID();
    private final UUID productId = UUID.randomUUID();
    private final UUID warehouseId = UUID.randomUUID();
    private final UUID locationId = UUID.randomUUID();
    private final UUID movementTypeId = UUID.randomUUID();

    @Test
    @DisplayName("adjustStock within threshold applies balance update and records movement")
    void adjustStock_withinThreshold_appliesBalanceAndUpdateMovement() {
        StockBalance balance = StockBalance.builder()
                .id(balanceId)
                .productId(productId)
                .warehouseId(warehouseId)
                .locationId(locationId)
                .quantity(new BigDecimal("50.00"))
                .version(1L)
                .createdAt(Instant.now())
                .build();

        StockAdjustmentRequest request = StockAdjustmentRequest.builder()
                .stockBalanceId(balanceId)
                .quantityAdjustment(new BigDecimal("10.00"))
                .reason("Inventory audit correction")
                .referenceNo("REF-001")
                .build();

        when(balanceRepository.findById(balanceId)).thenReturn(Optional.of(balance));
        when(movementTypeRepository.findIdByName(MovementTypeConstants.ADJUSTMENT)).thenReturn(Optional.of(movementTypeId));
        when(movementRepository.save(any(StockMovement.class))).thenAnswer(i -> i.getArgument(0));
        when(mapper.toDto(any(StockMovement.class))).thenReturn(StockMovementDto.builder().id(UUID.randomUUID()).build());

        StockAdjustmentResponse response = inventoryService.adjustStock(request, userId);

        assertThat(response.isRequiresApproval()).isFalse();
        assertThat(response.getMovement()).isNotNull();
        verify(balanceRepository).save(any(StockBalance.class));
        verify(movementRepository).save(any(StockMovement.class));
        verify(approvalRepository, never()).save(any());
    }

    @Test
    @DisplayName("adjustStock exceeding threshold creates pending approval record without modifying balance")
    void adjustStock_exceedingThreshold_savesPendingApproval() {
        StockBalance balance = StockBalance.builder()
                .id(balanceId)
                .productId(productId)
                .warehouseId(warehouseId)
                .locationId(locationId)
                .quantity(new BigDecimal("500.00"))
                .version(1L)
                .createdAt(Instant.now())
                .build();

        StockAdjustmentRequest request = StockAdjustmentRequest.builder()
                .stockBalanceId(balanceId)
                .quantityAdjustment(new BigDecimal("150.00"))
                .reason("Damaged batch replacement")
                .referenceNo("REF-002")
                .build();

        UUID approvalId = UUID.randomUUID();
        when(balanceRepository.findById(balanceId)).thenReturn(Optional.of(balance));
        when(approvalRepository.save(any(StockAdjustmentApproval.class)))
                .thenReturn(StockAdjustmentApproval.builder().id(approvalId).build());

        StockAdjustmentResponse response = inventoryService.adjustStock(request, userId);

        assertThat(response.isRequiresApproval()).isTrue();
        assertThat(response.getApprovalId()).isEqualTo(approvalId);
        verify(approvalRepository).save(any(StockAdjustmentApproval.class));
        verify(movementRepository, never()).save(any());
    }

    @Test
    @DisplayName("adjustStock missing reason throws AppException 400 Bad Request")
    void adjustStock_missingReason_throwsException() {
        StockAdjustmentRequest request = StockAdjustmentRequest.builder()
                .stockBalanceId(balanceId)
                .quantityAdjustment(new BigDecimal("10.00"))
                .reason("")
                .build();

        assertThatThrownBy(() -> inventoryService.adjustStock(request, userId))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("Reason is required");
    }

    @Test
    @DisplayName("adjustStock resulting in negative balance throws AppException 400 Bad Request")
    void adjustStock_resultingNegativeQuantity_throwsException() {
        StockBalance balance = StockBalance.builder()
                .id(balanceId)
                .quantity(new BigDecimal("20.00"))
                .build();

        StockAdjustmentRequest request = StockAdjustmentRequest.builder()
                .stockBalanceId(balanceId)
                .quantityAdjustment(new BigDecimal("-30.00"))
                .reason("Count reduction")
                .build();

        when(balanceRepository.findById(balanceId)).thenReturn(Optional.of(balance));

        assertThatThrownBy(() -> inventoryService.adjustStock(request, userId))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("Adjustment cannot result in negative stock balance");
    }

    @Test
    @DisplayName("approveAdjustment updates balance, records movement, and deletes pending record")
    void approveAdjustment_validId_updatesBalanceLogsMovementAndDeletesPendingRecord() {
        UUID approvalId = UUID.randomUUID();
        StockAdjustmentApproval approval = StockAdjustmentApproval.builder()
                .id(approvalId)
                .productId(productId)
                .warehouseId(warehouseId)
                .locationId(locationId)
                .stockBalanceId(balanceId)
                .quantityAdjustment(new BigDecimal("150.00"))
                .reason("Manager approved adjustment")
                .createdBy(userId)
                .build();

        StockBalance balance = StockBalance.builder()
                .id(balanceId)
                .productId(productId)
                .warehouseId(warehouseId)
                .locationId(locationId)
                .quantity(new BigDecimal("100.00"))
                .version(1L)
                .build();

        when(approvalRepository.findById(approvalId)).thenReturn(Optional.of(approval));
        when(balanceRepository.findById(balanceId)).thenReturn(Optional.of(balance));
        when(movementTypeRepository.findIdByName(MovementTypeConstants.ADJUSTMENT)).thenReturn(Optional.of(movementTypeId));
        when(movementRepository.save(any(StockMovement.class))).thenAnswer(i -> i.getArgument(0));
        when(mapper.toDto(any(StockMovement.class))).thenReturn(StockMovementDto.builder().id(UUID.randomUUID()).build());

        StockMovementDto result = inventoryService.approveAdjustment(approvalId, userId);

        assertThat(result).isNotNull();
        verify(balanceRepository).save(any(StockBalance.class));
        verify(movementRepository).save(any(StockMovement.class));
        verify(approvalRepository).deleteById(approvalId);
    }

    @Test
    @DisplayName("rejectAdjustment deletes pending record without updating balance or recording movement")
    void rejectAdjustment_validId_deletesPendingRecordWithoutBalanceChange() {
        UUID approvalId = UUID.randomUUID();
        StockAdjustmentApproval approval = StockAdjustmentApproval.builder()
                .id(approvalId)
                .stockBalanceId(balanceId)
                .build();

        when(approvalRepository.findById(approvalId)).thenReturn(Optional.of(approval));

        inventoryService.rejectAdjustment(approvalId, userId);

        verify(approvalRepository).deleteById(approvalId);
        verify(balanceRepository, never()).save(any());
        verify(movementRepository, never()).save(any());
    }
}
