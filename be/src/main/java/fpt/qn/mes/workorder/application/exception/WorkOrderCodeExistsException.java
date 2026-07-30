package fpt.qn.mes.workorder.application.exception;

import fpt.qn.mes.common.exception.AppException;
import fpt.qn.mes.common.exception.ErrorCode;

public class WorkOrderCodeExistsException extends AppException {
    public WorkOrderCodeExistsException(String message) {
        super(400, ErrorCode.WORK_ORDER_CODE_EXISTS, message);
    }
}
