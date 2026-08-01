package fpt.qn.mes.workorder.application.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import fpt.qn.mes.workorder.application.dto.workorder.complete.CompleteWorkOrderRequest;
import static fpt.qn.mes.workorder.application.exception.WorkOrderExceptions.InvalidInputException;

@ExtendWith(MockitoExtension.class)
class WorkOrderServiceCompleteTest {

    @InjectMocks
    WorkOrderService service;

    @Test
    void completeWorkOrder_rejectsReportedQuantitiesThatDoNotSumToActual() {
        CompleteWorkOrderRequest request = new CompleteWorkOrderRequest();
        request.setActualQuantity(BigDecimal.TEN);
        request.setGoodQuantity(BigDecimal.valueOf(7));
        request.setDefectQuantity(BigDecimal.ONE);
        request.setScrapQuantity(BigDecimal.ONE);
        request.setOutputWarehouseId(UUID.randomUUID());
        request.setOutputLocationId(UUID.randomUUID());

        assertThatThrownBy(() -> service.completeWorkOrder(UUID.randomUUID(), request))
                .isInstanceOf(InvalidInputException.class);
    }
}
