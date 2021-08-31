package com.brian.api.common.exception;

import com.brian.api.common.util.ObjectUtil;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.List;

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class ExceptionHandleAdvice {


    /**
     * request body 필드 검증 에러 처리
     * (MethodValidationInterceptor 에서 발생)
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ProductApiException handleException(ConstraintViolationException e) {
        ConstraintViolation violation = e.getConstraintViolations().stream().findFirst().get();
        String message = makeConstraintMessage(violation);

        log.error("ERRORMSG : {}", HttpStatus.BAD_REQUEST, e);

        return new ProductApiException(message);
    }

    /**
     * request body 필드 검증 에러 처리
     * (SpringValidatorAdapter 에서 발생)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProductApiException handleException(MethodArgumentNotValidException e) {
        String message = e.getMessage();

        if (e.getBindingResult().getFieldError() != null) {
            FieldError fieldError = e.getBindingResult().getFieldError();
            ConstraintViolation violation = fieldError.unwrap(ConstraintViolation.class);
            message = makeConstraintMessage(violation);
        }

        log.error("ERRORMSG : {}", HttpStatus.BAD_REQUEST, e);

        return new ProductApiException(message);
    }

    /**
     * request param 검증 에러 처리
     */
    @ExceptionHandler(BindException.class)
    public ProductApiException handleException(BindException e) {
        String message = e.getMessage();
        List<FieldError> fieldErrors = e.getBindingResult().getFieldErrors();

        if (fieldErrors.size() > 0) {
            FieldError fieldError = fieldErrors.get(0);
            message = fieldError.getDefaultMessage();

            // 필드 제약 조건 에러인 경우 ConstraintViolation 을 통해 메시지 생성
            if (fieldError.contains(ConstraintViolation.class)) {
                ConstraintViolation violation = fieldError.unwrap(ConstraintViolation.class);
                message = makeConstraintMessage(violation);
                log.error("ERRORMSG : {}", HttpStatus.BAD_REQUEST, e);
                return new ProductApiException(message);
            }
        }

        log.error("ERRORMSG : {}", HttpStatus.INTERNAL_SERVER_ERROR, e);
        return new ProductApiException(message);
    }

    /**
     * ConstraintViolation 으로부터 메시지를 생성
     * 필드에 @Description 설정(필드명)되어 있으면 에러 메시지 앞에 설정값을 붙여준다.
     *
     * @param violation
     * @return
     */
    private String makeConstraintMessage(ConstraintViolation violation) {
        String message = violation.getMessage();
        Class cls = violation.getLeafBean().getClass();
        String[] fieldNameWithObject = violation.getPropertyPath().toString().split("[.]");
        String fieldName = fieldNameWithObject[fieldNameWithObject.length-1];
        String description = ObjectUtil.getDescription(cls, fieldName);
        return description == null ? message : description + message;
    }
}
