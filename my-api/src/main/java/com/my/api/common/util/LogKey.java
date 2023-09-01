package com.my.api.common.util;

import org.slf4j.MDC;
import org.springframework.util.StringUtils;

import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;

/**
 * 요청 추적용
 */
public final class LogKey {

    public static final String LOG_KEY_NAME = "logKey";

    private LogKey() {
    }

    public static String get() {
        return MDC.get(LOG_KEY_NAME);
    }

    public static void put(String keyValue) {
        MDC.put(LOG_KEY_NAME, keyValue);
    }

    public static void put(HttpServletRequest request) {
        put(getLogKey(request));
    }

    public static void remove() {
        MDC.remove(LOG_KEY_NAME);
    }

    public static String createLogKey() {
        return UUID.randomUUID().toString();
    }

    public static String getLogKey(HttpServletRequest request) {
        // 요청 header 에 이미 로그키가 있으면 그대로 사용
        String logKey = request.getHeader(LogKey.LOG_KEY_NAME);
        if (!StringUtils.hasLength(logKey)) {
            logKey = LogKey.createLogKey();
        }
        return logKey;
    }
}
