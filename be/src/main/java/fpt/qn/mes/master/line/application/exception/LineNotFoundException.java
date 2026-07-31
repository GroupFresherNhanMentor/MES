package fpt.qn.mes.master.line.application.exception;

import org.springframework.http.HttpStatus;
import fpt.qn.mes.common.exception.AppException;
import fpt.qn.mes.common.exception.ErrorCode;

public class LineNotFoundException extends AppException {
    public LineNotFoundException(String message) {
        super(404, ErrorCode.NOT_FOUND, message);
    }
}
