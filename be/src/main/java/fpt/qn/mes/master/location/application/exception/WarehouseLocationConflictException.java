package fpt.qn.mes.master.location.application.exception;

import org.springframework.http.HttpStatus;
import fpt.qn.mes.common.exception.AppException;
import fpt.qn.mes.common.exception.ErrorCode;

public class WarehouseLocationConflictException extends AppException {
    public WarehouseLocationConflictException(String message) {
        super(409, ErrorCode.CONFLICT, message);
    }
}
