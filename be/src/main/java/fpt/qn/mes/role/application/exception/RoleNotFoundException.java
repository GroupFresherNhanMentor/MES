package fpt.qn.mes.role.application.exception;

import org.springframework.http.HttpStatus;

import fpt.qn.mes.common.exception.AppException;
import fpt.qn.mes.common.exception.ErrorCode;

public class RoleNotFoundException extends AppException {
    public RoleNotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, ErrorCode.NOT_FOUND, message);
    }
}
