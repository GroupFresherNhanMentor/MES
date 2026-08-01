package fpt.qn.mes.audit.application.exception;

import org.springframework.http.HttpStatus;

import fpt.qn.mes.common.exception.AppException;
import fpt.qn.mes.common.exception.ErrorCode;

public class AuditLogNotFoundException extends AppException {

    public AuditLogNotFoundException(String message) {
        super(HttpStatus.NOT_FOUND.value(), ErrorCode.RESOURCE_NOT_FOUND, message);
    }
}
