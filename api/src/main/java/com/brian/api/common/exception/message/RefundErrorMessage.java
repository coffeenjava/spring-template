package com.brian.api.common.exception.message;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum RefundErrorMessage implements BaseErrorMessage {

    END_GRATER_THAN_START(HttpStatus.BAD_REQUEST, "시작일이 종료일보다 크게 설정 될 수 없습니다."),
    SEARCH_RANGE_LIMIT(HttpStatus.BAD_REQUEST, "최대 조회 가능 기간은 30일입니다.")
    ;

    HttpStatus httpStatus;
    String message;

    RefundErrorMessage(HttpStatus httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }
}
