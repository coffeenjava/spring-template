package com.my.api.common.util;

import org.slf4j.MDC;
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

    public static void remove() {
        MDC.remove(LOG_KEY_NAME);
    }

    public static String createLogKey() {
        return UUID.randomUUID().toString();
    }
}
