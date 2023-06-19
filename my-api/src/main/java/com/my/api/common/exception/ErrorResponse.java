package com.my.api.common.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.my.api.common.annotation.Description;
import com.my.api.common.exception.message.CommonErrorCode;
import com.my.api.common.exception.message.ErrorCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;

/**
 * 공통 에러 형식으로 내려주기 위한 응답 객체
 *
 * <p><p>ex) {@link CommonErrorCode#API_CALL_ERROR}
 * <pre>
 * {@code
 * {
 *     code: "api-error",
 *     message: "api 호출 오류",
 *     detail: [ // detail 은 설정했을때만 존재
 *         "응답시간 초과"
 *     ]
 * }
 * }
 * </pre>
 */
@Slf4j
@Getter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    @Description("에러 코드")
    protected String code;

    @Description("에러 메시지")
    protected String message;

    @Description("에러 상세 메시지")
    protected String detailMessage;

    @Description("추가적인 에러 정보 목록")
    private List<Object> additionalInfo;

    public ErrorResponse(ErrorCode ec) {
        this(ec, null);
    }

    public ErrorResponse(ErrorCode ec, String detailMessage) {
        this(ec.getCode(), ec.getMessage(), detailMessage);
    }

    public ErrorResponse(String code, String message, String detailMessage) {
        this(code, message, detailMessage, null);
    }

    public ErrorResponse(String code, String message, String detailMessage, Object additionalInfo) {
        this.code = code;
        this.message = message;
        this.detailMessage = detailMessage;

        if (additionalInfo != null) {
            if (additionalInfo instanceof List) {
                this.additionalInfo = (List) additionalInfo;
            } else {
                this.additionalInfo = Arrays.asList(additionalInfo);
            }
        }
    }

    public static ResponseEntity<ErrorResponse> buildResponse(Throwable e, ErrorCode ec) {
        return buildResponse(e, ec, null);
    }

    public static ResponseEntity<ErrorResponse> buildResponse(Throwable e, ErrorCode ec, Object additionalInfo) {
        HttpStatus httpStatus = ec.getHttpStatus();

        if (httpStatus.is5xxServerError()) { // 5xx 일때만 error 로 남긴다
            log.error(httpStatus.toString(), e);
        } else {
            log.info(httpStatus.toString(), e);
        }

        ErrorResponse errorResponse = new ErrorResponse(ec.getCode(), ec.getMessage(), null, additionalInfo);

        return ResponseEntity
                .status(ec.getHttpStatus())
                .body(errorResponse);
    }
}
