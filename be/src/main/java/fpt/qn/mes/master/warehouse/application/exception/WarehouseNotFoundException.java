package fpt.qn.mes.master.warehouse.application.exception;

import fpt.qn.mes.common.exception.AppException;
import fpt.qn.mes.common.exception.ErrorCode;

public class WarehouseNotFoundException extends AppException {
    public WarehouseNotFoundException(String message) { super(404, ErrorCode.NOT_FOUND, message); }
}
