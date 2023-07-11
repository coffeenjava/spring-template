package com.my.api.common.exception;

import com.my.api.common.exception.message.ErrorCode;
import lombok.Getter;

@Getter
public class ApiRuntimeException extends RuntimeException {

    protected ErrorCode errorCode;
    protected String detailMessage;
    protected Object additionalInfo;

    public ApiRuntimeException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public ApiRuntimeException(ErrorCode errorCode, String detailMessage) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.detailMessage = detailMessage;
    }

    public ApiRuntimeException(ErrorCode errorCode, String detailMessage, Object additionalInfo) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.detailMessage = detailMessage;
        this.additionalInfo = additionalInfo;
    }
}
