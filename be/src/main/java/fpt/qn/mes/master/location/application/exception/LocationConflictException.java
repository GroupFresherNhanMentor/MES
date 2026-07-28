package fpt.qn.mes.master.location.application.exception;

import fpt.qn.mes.common.exception.AppException;
import fpt.qn.mes.common.exception.ErrorCode;

public class LocationConflictException extends AppException {
    public LocationConflictException(String message) { super(409, ErrorCode.CONFLICT, message); }
}
