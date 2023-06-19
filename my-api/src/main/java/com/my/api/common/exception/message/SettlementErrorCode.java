package com.my.api.common.exception.message;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * 정산 도메인 관련 에러코드 샘플
 */
@Getter
@AllArgsConstructor
public enum SettlementErrorCode implements ErrorCode {

    APPROVE("정산 승인 실패"),
    CANCEL("정산 취소 실패"),
    INVALID_REQUEST("잘못된 요청", HttpStatus.BAD_REQUEST)
    ;

    private static final String title = "정산 오류";

    String code;
    String message;
    HttpStatus httpStatus;

    SettlementErrorCode(String message) {
        this(title, message, HttpStatus.BAD_GATEWAY);
    }

    SettlementErrorCode(String message, HttpStatus status) {
        this(title, message, status);
    }
}
