package fpt.qn.mes.master.line.application.exception;

import fpt.qn.mes.common.exception.AppException;
import fpt.qn.mes.common.exception.ErrorCode;

public class LineStatusNotFoundException extends AppException {
    public LineStatusNotFoundException(String message) {
        super(404, ErrorCode.NOT_FOUND, message);
    }
}
