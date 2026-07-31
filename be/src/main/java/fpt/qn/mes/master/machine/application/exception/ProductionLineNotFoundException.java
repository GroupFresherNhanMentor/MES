package fpt.qn.mes.master.machine.application.exception;

import fpt.qn.mes.common.exception.AppException;
import fpt.qn.mes.common.exception.ErrorCode;

public class ProductionLineNotFoundException extends AppException {
    public ProductionLineNotFoundException(String message) {
        super(404, ErrorCode.NOT_FOUND, message);
    }
}
