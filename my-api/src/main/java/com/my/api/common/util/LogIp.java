package com.my.api.common.util;

import org.slf4j.MDC;
import java.util.Optional;

public final class LogIp {

    public static final String HEADER_X_FORWARDED_FOR = "X-Forwarded-For";

    private LogIp() {
    }

    public static String getLogIpName() {
        return HEADER_X_FORWARDED_FOR;
    }

    public static String get() {
        return MDC.get(HEADER_X_FORWARDED_FOR);
    }

    public static void put(String keyValue) {
        MDC.put(HEADER_X_FORWARDED_FOR, keyValue);
    }

    public static void remove() {
        MDC.remove(HEADER_X_FORWARDED_FOR);
    }

    public static String getFirstIp() {
        String logIp = Optional.ofNullable(get()).orElse("");
        logIp = logIp.replace("/", ",").replace(" ", "");
        return logIp.split(",")[0];
    }

    public static String getRemoteIp() {
        return getFirstIp();
    }
}
