package fpt.qn.mes.master.location.application.exception;

import org.springframework.http.HttpStatus;
import fpt.qn.mes.common.exception.AppException;
import fpt.qn.mes.common.exception.ErrorCode;

public class WarehouseLocationNotFoundException extends AppException {
    public WarehouseLocationNotFoundException(String message) {
        super(404, ErrorCode.NOT_FOUND, message);
    }
}
