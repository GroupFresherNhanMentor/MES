package fpt.qn.mes.master.machine.application.exception;

import fpt.qn.mes.common.exception.AppException;
import fpt.qn.mes.common.exception.ErrorCode;

public class MachineNotFoundException extends AppException {
    public MachineNotFoundException(String message) { super(404, ErrorCode.NOT_FOUND, message); }
}
