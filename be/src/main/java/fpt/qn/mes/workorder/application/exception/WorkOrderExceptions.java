package fpt.qn.mes.workorder.application.exception;

import java.util.List;

import fpt.qn.mes.common.exception.AppException;
import fpt.qn.mes.common.exception.ErrorCode;

public class WorkOrderExceptions {

    public static class BomNotActiveException extends AppException {
        public BomNotActiveException(String message) {
            super(400, ErrorCode.BOM_NOT_ACTIVE, message);
        }
    }

    public static class InsufficientMaterialException extends AppException {
        public InsufficientMaterialException(String message, List<ShortageDetail> details) {
            super(400, ErrorCode.INSUFFICIENT_STOCK, message, details);
        }
    }

    public static class InvalidInputException extends AppException {
        public InvalidInputException(String message) {
            super(400, ErrorCode.INVALID_INPUT, message);
        }
    }

    public static class InvalidWorkOrderReservationException extends AppException {
        public InvalidWorkOrderReservationException(String message) {
            super(400, ErrorCode.INVALID_INPUT, message);
        }
    }

    public static class InvalidWorkOrderStateException extends AppException {
        public InvalidWorkOrderStateException(String message) {
            super(400, ErrorCode.INVALID_STATUS_TRANSITION, message);
        }
    }

    public static class MachineNotAvailableException extends AppException {
        public MachineNotAvailableException(String message) {
            super(400, ErrorCode.BAD_REQUEST, message);
        }
    }

    public static class WorkOrderCodeExistsException extends AppException {
        public WorkOrderCodeExistsException(String message) {
            super(400, ErrorCode.WORK_ORDER_CODE_EXISTS, message);
        }
    }

    public static class WorkOrderNotFoundException extends AppException {
        public WorkOrderNotFoundException(String message) {
            super(404, ErrorCode.NOT_FOUND, message);
        }
    }
}
