package com.brian.api.common.exception;

import com.brian.api.common.exception.message.BaseErrorMessage;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ProductApiException extends RuntimeException {

    private HttpStatus httpStatus;

    public ProductApiException(String message) {
        super(message);

        this.httpStatus = HttpStatus.BAD_REQUEST;
    }

    public ProductApiException(String message, Throwable cause) {
        super(message, cause);
        this.httpStatus = HttpStatus.BAD_REQUEST;
    }

    public ProductApiException(BaseErrorMessage errorMessage) {
        super(errorMessage.getMessage());
        this.httpStatus = errorMessage.getHttpStatus();
    }
}
