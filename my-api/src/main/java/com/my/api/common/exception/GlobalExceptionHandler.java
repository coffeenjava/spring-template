package com.my.api.common.exception;

import com.my.api.common.exception.message.CommonErrorCode;
import com.my.api.common.exception.message.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.RestClientException;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;


/**
 * 전역 에러처리 핸들러
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * API 표준 에러 처리
     *
     * @see ApiRuntimeException
     */
    @ExceptionHandler(ApiRuntimeException.class)
    public ResponseEntity<ErrorResponse> handleException(ApiRuntimeException e) {
        StringBuilder logMsg = new StringBuilder();

        logMsg.append("message: ");
        logMsg.append(e.getMessage());

        if (StringUtils.hasText(e.getDetailMessage())) {
            logMsg.append("\ndetailMessage: ");
            logMsg.append(e.getDetailMessage());
        }

        // stacktrace 포함 로깅
        log.error(logMsg.toString(), e);

        ErrorCode ec = e.getErrorCode();

        return ResponseEntity
                .status(ec.getHttpStatus())
                .body(new ErrorResponse(ec, e.getDetailMessage()));
    }

    /**
     * 핸들링 되지 않은 에러 발생 대비
     * 여기로 호출된 에러는 메서드 추가하여 핸들링하도록 해야 한다.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        return ErrorResponse.buildResponse(e, CommonErrorCode.UNHANDLED_ERROR, e.getMessage());
    }

    /**
     * request body 필드 검증 에러 처리
     * (MethodValidationInterceptor 에서 발생)
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleException(ConstraintViolationException e) {
        List<String> detail = e.getConstraintViolations().stream().map(cv -> {
            ConstraintViolation violation = cv.unwrap(ConstraintViolation.class);
            return makeConstraintMessage(violation);
        }).collect(Collectors.toList());

        return ErrorResponse.buildResponse(e, CommonErrorCode.INVALID_REQUEST_PARAM, detail);
    }

    /**
     * request body 필드 검증 에러 처리
     * (SpringValidatorAdapter 에서 발생)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleException(MethodArgumentNotValidException e) {
        List<String> detail;

        if (e.getBindingResult().getFieldErrors() == null) {
            detail = Arrays.asList(e.getMessage());
        } else {
            detail = e.getFieldErrors().stream().map(fe -> {
                ConstraintViolation violation = fe.unwrap(ConstraintViolation.class);
                return makeConstraintMessage(violation);
            }).collect(Collectors.toList());
        }

        return ErrorResponse.buildResponse(e, CommonErrorCode.INVALID_REQUEST_PARAM, detail);
    }

    /**
     * request param 검증 에러 처리
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ErrorResponse> handleException(BindException e) {
        List<FieldError> fieldErrors = e.getBindingResult().getFieldErrors();

        List<String> detail = fieldErrors.stream().map(fe -> {
            if (fe.contains(ConstraintViolation.class)) {
                ConstraintViolation violation = fe.unwrap(ConstraintViolation.class);
                return makeConstraintMessage(violation);
            } else {
                return fe.getDefaultMessage();
            }
        }).collect(Collectors.toList());

        if (detail.isEmpty()) {
            return ErrorResponse.buildResponse(e, CommonErrorCode.INVALID_REQUEST_PARAM, e.getMessage());
        }

        return ErrorResponse.buildResponse(e, CommonErrorCode.INVALID_REQUEST_PARAM, detail);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleException(HttpMessageNotReadableException e) {
        return ErrorResponse.buildResponse(e, CommonErrorCode.INVALID_REQUEST_PARAM, e.getMessage());
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleException(MissingServletRequestParameterException e) {
        String message = String.format("[%s] 필수값입니다.", e.getParameterName());
        return ErrorResponse.buildResponse(e, CommonErrorCode.INVALID_REQUEST_PARAM, message);
    }

    @ExceptionHandler(TypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleException(TypeMismatchException e) {
        return ErrorResponse.buildResponse(e, CommonErrorCode.INVALID_REQUEST_PARAM, e.getMessage());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleException(HttpRequestMethodNotSupportedException e) {
        String requestMethod = e.getMethod();
        String supportedMethod = Optional.ofNullable(e.getSupportedHttpMethods())
                .map(Objects::toString)
                .orElse("");
        String message = String.format("지원하지않는 요청 방식(%s). 지원가능%s", requestMethod, supportedMethod);

        return ErrorResponse.buildResponse(e, CommonErrorCode.INVALID_REQUEST, message);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleException(HttpMediaTypeNotSupportedException e) {
        final String supportMessage = "Support types are " + e.getSupportedMediaTypes();
        final List<String> detail = Arrays.asList(e.getMessage(), supportMessage);
        return ErrorResponse.buildResponse(e, CommonErrorCode.INVALID_REQUEST, detail);
    }

    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<ErrorResponse> handleException(NullPointerException e) {
        String message = e.getMessage();

        if (StringUtils.hasText(message) == false) {
            message = e.getClass().getName();
        }

        return ErrorResponse.buildResponse(e, CommonErrorCode.SERVER_ERROR, message);
    }

    @ExceptionHandler(RestClientException.class)
    public ResponseEntity<ErrorResponse> handleException(RestClientException e) {
        return ErrorResponse.buildResponse(e, CommonErrorCode.API_CALL_ERROR, e.getMessage());
    }

    /**
     * ConstraintViolation 메시지 생성
     */
    private String makeConstraintMessage(ConstraintViolation violation) {
        String message = violation.getMessage();
        String[] fieldNameWithObject = violation.getPropertyPath().toString().split("[.]");
        String fieldName = fieldNameWithObject[fieldNameWithObject.length - 1];
        return String.format("[%s] %s", fieldName, message);
    }
}
