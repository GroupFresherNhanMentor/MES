package fpt.qn.mes.master.warehouse.application.exception;

import org.springframework.http.HttpStatus;
import fpt.qn.mes.common.exception.AppException;
import fpt.qn.mes.common.exception.ErrorCode;

public class WarehouseNotFoundException extends AppException {
    public WarehouseNotFoundException(String message) { super(HttpStatus.NOT_FOUND, ErrorCode.NOT_FOUND, message); }
}
