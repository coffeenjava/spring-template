package com.my.api.config;

import com.my.api.common.util.LogIp;
import com.my.api.common.util.LogKey;
import com.my.api.common.util.WebUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.ValveBase;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 톰캣 커스텀 Valve 설정
 *
 * 에러 로깅
 * 요청 초기화/종료 시 필요한 기능
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class TomcatCustomizer implements WebServerFactoryCustomizer<TomcatServletWebServerFactory> {

    @Override
    public void customize(TomcatServletWebServerFactory factory) {
        factory.addEngineValves(new CustomEngineValve());
    }

    class CustomEngineValve extends ValveBase {

        @Override
        public void invoke(Request request, Response response) throws IOException, ServletException {
            // 요청 초기화
            initializeRequest(request, response);

            // 톰캣 오류 처리
            // 서블릿 필터 접근 전 발생하므로 여기서 로깅한다.
            if (response.isError()) {
                logError(request, response);
            }

            getNext().invoke(request, response);

            // 요청 종료
            destroyRequest();
        }

        /**
         * 에러 정보 생성
         */
        void logError(Request request, Response response) {
            StringBuilder msg = new StringBuilder();
            msg.append("[WebServer Error]").append(' ');
            msg.append(response.getStatus()).append(' ');
            msg.append(request.getMethod()).append(' ');
            msg.append(request.getRequestURI());

            String queryString = request.getQueryString();
            if (queryString != null) {
                msg.append('?').append(queryString);
            }

            HttpHeaders headers = new ServletServerHttpRequest(request).getHeaders();
            msg.append("\nheaders=").append(headers).append(' ');

            Throwable throwable = (Throwable) request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);

            log.error(msg.toString(), throwable);
        }

        void initializeRequest(Request request, Response response) {
            LogKey.put(request);
            addXForwardedFor(request);
            addCorsHeader(request, response);
        }

        void destroyRequest() {
            LogKey.remove();
            LogIp.remove();
        }

        void addXForwardedFor(final HttpServletRequest request) {
            String ip = null;
            for (final String headerName : WebUtils.CLIENT_IP_HEADER_NAMES) {
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

        void addCorsHeader(final HttpServletRequest request, final HttpServletResponse response) {
//        response.setHeader("Access-Control-Allow-Origin", "*"); // CORS 전부 허용

            final String originHeader = WebUtils.getOriginHeader(request);
            response.setHeader("Access-Control-Allow-Origin", originHeader);
            response.setHeader("Access-Control-Allow-Methods", "POST, GET, PUT, OPTIONS, DELETE, PATCH");
            response.setHeader("Access-Control-Max-Age", "3600");
            response.setHeader("Access-Control-Allow-Headers",
                    "Origin, X-Requested-With, Content-Type, Accept, Authorization, Keep-Alive," +
                            "User-Agent, If-Modified-Since, Cache-Control, Content-Range, Range");
            response.setHeader("Access-Control-Expose-Headers", "Location, Origin, X-Requested-With," +
                    "Content-Type, Accept, Authorization, Keep-Alive, User-Agent, If-Modified-Since, Cache-Control, Content-Range, Range");
            response.setHeader("Access-Control-Allow-Credentials", "true");
            response.setHeader(LogKey.LOG_KEY_NAME, LogKey.get());
        }
    }
}
