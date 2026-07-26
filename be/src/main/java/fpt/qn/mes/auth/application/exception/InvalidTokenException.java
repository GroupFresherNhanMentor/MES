package fpt.qn.mes.auth.application.exception;

import org.springframework.http.HttpStatus;

import fpt.qn.mes.common.exception.AppException;
import fpt.qn.mes.common.exception.ErrorCode;

public class InvalidTokenException extends AppException {

    public InvalidTokenException(String message) {
        super(HttpStatus.UNAUTHORIZED, ErrorCode.UNAUTHORIZED, message);
    }
}
