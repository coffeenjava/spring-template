package com.brian.api.common.exception.message;

import org.springframework.http.HttpStatus;

public interface BaseErrorMessage {
    HttpStatus getHttpStatus();
    String getMessage();
}
