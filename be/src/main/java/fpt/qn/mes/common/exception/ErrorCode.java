package fpt.qn.mes.common.exception;

public enum ErrorCode {
    INVALID_INPUT("INVALID_INPUT"),
    NOT_FOUND("NOT_FOUND"),
    INTERNAL_SERVER_ERROR("INTERNAL_SERVER_ERROR"),
    UNAUTHORIZED("UNAUTHORIZED"),
    FORBIDDEN("FORBIDDEN"),
    CONFLICT("CONFLICT"),
    TOO_MANY_REQUESTS("TOO_MANY_REQUESTS"),
    BAD_REQUEST("BAD_REQUEST"),
    BOM_NOT_ACTIVE("BOM_NOT_ACTIVE"),
    WORK_ORDER_CODE_EXISTS("WORK_ORDER_CODE_EXISTS"),
    INVALID_STATUS_TRANSITION("INVALID_STATUS_TRANSITION");

    private final String code;

    ErrorCode(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
