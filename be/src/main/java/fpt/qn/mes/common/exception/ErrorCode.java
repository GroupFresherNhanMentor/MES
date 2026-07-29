package fpt.qn.mes.common.exception;

public enum ErrorCode {
    INVALID_INPUT("INVALID_INPUT"),
    NOT_FOUND("NOT_FOUND"),
    INTERNAL_SERVER_ERROR("INTERNAL_SERVER_ERROR"),
    UNAUTHORIZED("UNAUTHORIZED"),
    FORBIDDEN("FORBIDDEN"),
    CONFLICT("CONFLICT"),
    TOO_MANY_REQUESTS("TOO_MANY_REQUESTS");

    private final String code;

    ErrorCode(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
