package com.my.api.common.filter;

import org.springframework.boot.autoconfigure.security.SecurityProperties;

/**
 * 필터 실행 순서 모음
 *
 * <p>실행 순서 보장이 필요한 필터는 여기에 순서를 지정하여 사용한다.
 */
public interface FilterOrder {

    int INIT = SecurityProperties.DEFAULT_FILTER_ORDER - 2;
    int REQUEST_RESPONSE_LOGGING = SecurityProperties.DEFAULT_FILTER_ORDER - 1;
}
