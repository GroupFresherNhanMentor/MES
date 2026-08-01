package fpt.qn.mes.master.line.application.exception;

import org.springframework.http.HttpStatus;
import fpt.qn.mes.common.exception.AppException;
import fpt.qn.mes.common.exception.ErrorCode;

public class LineConflictException extends AppException {
    public LineConflictException(String message) {
        super(409, ErrorCode.CONFLICT, message);
    }
}
