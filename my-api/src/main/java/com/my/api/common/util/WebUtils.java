package com.my.api.common.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

import javax.servlet.http.HttpServletRequest;

@Slf4j
public class WebUtils {

    public static final String HEADER_ORIGIN = "Origin";
    public static final String[] CLIENT_IP_HEADER_NAMES = new String[]{
            LogIp.HEADER_X_FORWARDED_FOR,
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_CLIENT_IP",
            "HTTP_X_FORWARDED_FOR"
    };

    /**
     * Form & Post 요청 여부
     */
    public static boolean isFormPost(HttpServletRequest request) {
        String contentType = request.getContentType();
        return (contentType != null && contentType.contains(MediaType.APPLICATION_FORM_URLENCODED_VALUE) &&
                HttpMethod.POST.matches(request.getMethod()));
    }

    public static String getOriginHeader(final HttpServletRequest request) {
        final String origin = request.getHeader(HEADER_ORIGIN);
        if (origin != null) {
            log.info("[Request origin] {}", origin);
        }
        return origin;
    }
}
