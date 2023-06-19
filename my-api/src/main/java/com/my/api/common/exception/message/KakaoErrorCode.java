package com.my.api.common.exception.message;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * 카카오 도메인 관련 에러코드 샘플
 */
@Getter
@AllArgsConstructor
public enum KakaoErrorCode implements ErrorCode {

    READY("결제 준비 실패"),
    APPROVE("결제 승인 실패"),
    CANCEL("결제 취소 실패"),
    INVALID_REQUEST("잘못된 요청", HttpStatus.BAD_REQUEST)
    ;

    private static final String title = "카카오페이 오류";

    String code;
    String message;
    HttpStatus httpStatus;

    KakaoErrorCode(String message) {
        this(title, message, HttpStatus.BAD_GATEWAY);
    }

    KakaoErrorCode(String message, HttpStatus status) {
        this(title, message, status);
    }
}
