package fpt.qn.mes.master.warehouse.application.exception;

import org.springframework.http.HttpStatus;
import fpt.qn.mes.common.exception.AppException;
import fpt.qn.mes.common.exception.ErrorCode;

public class WarehouseStatusConflictException extends AppException {
    public WarehouseStatusConflictException(String message) {
        super(409, ErrorCode.CONFLICT, message);
    }
}
