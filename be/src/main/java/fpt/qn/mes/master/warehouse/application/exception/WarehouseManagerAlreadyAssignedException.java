package fpt.qn.mes.master.warehouse.application.exception;

import fpt.qn.mes.common.exception.AppException;
import fpt.qn.mes.common.exception.ErrorCode;

public class WarehouseManagerAlreadyAssignedException extends AppException {
    public WarehouseManagerAlreadyAssignedException(String message) {
        super(409, ErrorCode.CONFLICT, message);
    }
}
