package fpt.qn.mes.quality.application.exception;

import fpt.qn.mes.common.exception.AppException;
import fpt.qn.mes.common.exception.ErrorCode;

public class QualityInspectionNotFoundException extends AppException {
    public QualityInspectionNotFoundException(String message) { super(404, ErrorCode.NOT_FOUND, message); }
}
