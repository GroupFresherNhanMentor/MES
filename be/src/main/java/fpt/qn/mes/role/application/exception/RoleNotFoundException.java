package fpt.qn.mes.role.application.exception;


import fpt.qn.mes.common.exception.AppException;
import fpt.qn.mes.common.exception.ErrorCode;

public class RoleNotFoundException extends AppException {
    public RoleNotFoundException(String message) {
        super(404, ErrorCode.NOT_FOUND, message);
    }
}
