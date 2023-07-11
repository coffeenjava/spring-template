package com.my.api.common.exception.message;

import org.springframework.http.HttpStatus;

/**
 * Exception 핸들링을 위한 기본 에러 메시지
 */
public interface ErrorCode {

    String getCode();
    String getMessage();
    HttpStatus getHttpStatus();
}
