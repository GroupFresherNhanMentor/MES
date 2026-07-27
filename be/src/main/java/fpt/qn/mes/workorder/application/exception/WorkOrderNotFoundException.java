package fpt.qn.mes.workorder.application.exception;

import fpt.qn.mes.common.exception.AppException;
import fpt.qn.mes.common.exception.ErrorCode;

public class WorkOrderNotFoundException extends AppException {
    public WorkOrderNotFoundException(String message) { super(404, ErrorCode.NOT_FOUND, message); }
}
