package com.my.api.common.filter;

import com.my.api.common.util.LogIp;
import com.my.api.common.util.LogKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * api 공통 초기화 필터
 *
 * <p>로그키 / 요청 uri / cors 응답 헤더 등 설정
 */
@Slf4j
@Order(FilterOrder.INIT)
//@Component
@Deprecated // TomcatCustomizer 로 기능 이동. 샘플로 남겨둔다. 톰캣 외의 서버를 사용하게 될 경우 필터 활성화 필요
public class InitFilter extends OncePerRequestFilter {
    public static final String HEADER_ORIGIN = "Origin";
    public static final String[] CLIENT_IP_HEADER_NAMES = new String[]{
            LogIp.HEADER_X_FORWARDED_FOR,
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_CLIENT_IP",
            "HTTP_X_FORWARDED_FOR"
    };

    @Override
    protected void doFilterInternal(final HttpServletRequest request, final HttpServletResponse response, final FilterChain filterChain) throws ServletException, IOException {

        try {
            addLogKey(request); // 로그키 설정
            addXForwardedFor(request);

            final String originHeader = getOriginHeader(request);
            addCorsHeader(originHeader, response);

            filterChain.doFilter(request, response);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            removeXForwardedFor();
            removeLogKey();
        }
    }

    private void addXForwardedFor(final HttpServletRequest request) {
        String ip = null;
        for (final String headerName : CLIENT_IP_HEADER_NAMES) {
            if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader(headerName);
            }
        }
        // 그래도 없다면 최후의 수단으로
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        LogIp.put(ip);
    }

    private void addLogKey(final HttpServletRequest request) {
        LogKey.put(getLogKey(request));
    }

    private String getLogKey(final HttpServletRequest request) {
        // 요청 header 에 이미 로그키가 있으면 그대로 사용
        String logKey = request.getHeader(LogKey.LOG_KEY_NAME);
        if (!StringUtils.hasLength(logKey)) {
            logKey = LogKey.createLogKey();
        }
        return logKey;
    }

    private void addCorsHeader(final String origin, final HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", origin);
        response.setHeader("Access-Control-Allow-Methods", "POST, GET, PUT, OPTIONS, DELETE, PATCH");
        response.setHeader("Access-Control-Max-Age", "3600");
        response.setHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept, prgmId");
        response.setHeader("Access-Control-Expose-Headers", "Location,Origin");
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader(LogKey.LOG_KEY_NAME, LogKey.get());
    }

    private String getOriginHeader(final HttpServletRequest request) {
        final String origin = request.getHeader(HEADER_ORIGIN);
        if (origin != null) {
            log.info("[Request origin] {}", origin);
        }
        return origin;
    }

    private void removeLogKey() {
        LogKey.remove();
    }

    private void removeXForwardedFor() {
        LogIp.remove();
    }
}
