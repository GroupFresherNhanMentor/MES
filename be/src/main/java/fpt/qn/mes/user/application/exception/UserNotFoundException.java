package fpt.qn.mes.user.application.exception;

import org.springframework.http.HttpStatus;

import fpt.qn.mes.common.exception.AppException;
import fpt.qn.mes.common.exception.ErrorCode;

public class UserNotFoundException extends AppException {

    public UserNotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, ErrorCode.NOT_FOUND, message);
    }
}
