package com.my.api.common.filter;

import com.my.api.common.annotation.Description;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StreamUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

import javax.servlet.FilterChain;
import javax.servlet.ReadListener;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * 요청/응답 로깅 Filter
 */
@Slf4j
@Order(FilterOrder.REQUEST_RESPONSE_LOGGING)
@Component
public class RequestResponseLoggingFilter extends OncePerRequestFilter {

    @Description("로그 메시지의 줄바꿈 여부")
    private boolean isLineBreak;

    @Description("요청/응답 body 최대 길이 (byte)")
    private int maxPayloadLength;

    @Description("요청 query string 포함 여부")
    private boolean isIncludeQueryString;

    @Description("요청 header 포함 여부")
    private boolean isIncludeHeader;

    @Description("요청 body 포함 여부")
    private boolean isIncludePayload;

    @Description("응답 header 포함 여부")
    private boolean isIncludeResponseHeader;

    @Description("응답 body 포함 여부")
    private boolean isIncludeResponsePayload;

    @Description("로깅 제외할 uri 패턴")
    private Set<Pattern> excludeUriPatterns;

    public RequestResponseLoggingFilter(RequestResponseLoggingProperties properties) {
        isLineBreak = properties.isLineBreak();
        maxPayloadLength = properties.getMaxPayloadLength();

        RequestResponseLoggingProperties.Request request = properties.getRequest();
        isIncludeQueryString = request.isQueryString();
        isIncludeHeader = request.isHeader();
        isIncludePayload = request.isPayload();

        RequestResponseLoggingProperties.Response response = properties.getResponse();
        isIncludeResponseHeader = response.isHeader();
        isIncludeResponsePayload = response.isPayload();

        if (CollectionUtils.isEmpty(properties.getExcludeUri()) == false) {
            excludeUriPatterns = new HashSet<>();
            properties.getExcludeUri().forEach(uri -> excludeUriPatterns.add(Pattern.compile(uri)));
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if (CollectionUtils.isEmpty(excludeUriPatterns)) return false;

        String requestURI = request.getRequestURI();
        return excludeUriPatterns.stream().anyMatch(p -> p.matcher(requestURI).matches());
    }

    @Override
    protected void doFilterInternal(final HttpServletRequest request, final HttpServletResponse response, final FilterChain filterChain) throws ServletException, IOException {
        if (isMultipartFormData(request)) { // 멀티파트는 로깅 제외
            filterChain.doFilter(request, response);
            return;
        }

        final boolean isFirstRequest = !isAsyncDispatch(request);
        HttpServletRequest requestToUse = request;

        if (isFirstRequest) {
            requestToUse = new ReusableRequestWrapper(request);
            String requestInfo = createRequestInfo(requestToUse);

            if (isLineBreak == false) {
                requestInfo = requestInfo.replaceAll("(\\r|\\n)", "");
            }

            log.info(requestInfo);
        }

        final ContentCachingResponseWrapper cachingResponse = new ContentCachingResponseWrapper(response);

        long start = System.currentTimeMillis();
        filterChain.doFilter(requestToUse, cachingResponse);
        final long executionTime = System.currentTimeMillis() - start;

        if (isFirstRequest) {
            String responseInfo = createResponseInfo(requestToUse, cachingResponse, executionTime);

            if (isLineBreak == false) {
                responseInfo = responseInfo.replaceAll("(\\r|\\n)", "");
            }

            log.info(responseInfo);
        }

        // 아래 메서드를 호출해야만 wrapper 가 response 에 body 를 넣어준다.
        cachingResponse.copyBodyToResponse();
    }

    /**
     * Multipart-form-data 인지 확인
     *
     * @param request
     * @return
     */
    private boolean isMultipartFormData(final HttpServletRequest request) {
        return Optional.ofNullable(request)
                .flatMap(request1 -> Optional.ofNullable(request1.getContentType()))
                .map(contentType -> contentType.startsWith(MediaType.MULTIPART_FORM_DATA_VALUE))
                .orElse(false);
    }

    /**
     * 요청 정보 생성
     */
    protected String createRequestInfo(HttpServletRequest request) {
        StringBuilder msg = new StringBuilder();
        msg.append("[Start Request]").append(' ');
        msg.append(request.getMethod()).append(' ');
        msg.append(request.getRequestURI());

        if (isIncludeQueryString) {
            String queryString = request.getQueryString();
            if (queryString != null) {
                msg.append('?').append(queryString);
            }
        }

        msg.append(' ');

        if (isIncludeHeader) {
            HttpHeaders headers = new ServletServerHttpRequest(request).getHeaders();
            msg.append("\nheaders=").append(headers).append(' ');
        }

        if (isIncludePayload) {
            try {
                final byte[] buf = IOUtils.toByteArray(request.getInputStream());
                String payload = getMessagePayload(buf);

                if (payload != null) {
                    payload = payload.replaceAll("(\\r|\\n)","");
                    msg.append("\npayload=").append(payload).append(' ');
                }
            } catch (IOException e) {}
        }

        return msg.toString();
    }

    /**
     * 응답 정보 생성
     */
    protected String createResponseInfo(HttpServletRequest request, ContentCachingResponseWrapper response, long executionTime) {
        StringBuilder msg = new StringBuilder();
        msg.append("[End Response]").append(' ');
        msg.append(response.getStatus()).append(' ');
        msg.append(request.getMethod()).append(' ');
        msg.append(request.getRequestURI());

        if (isIncludeQueryString) {
            String queryString = request.getQueryString();
            if (queryString != null) {
                msg.append('?').append(queryString);
            }
        }

        msg.append(" (").append(executionTime).append("ms) ");

        if (isIncludeResponseHeader) {
            HttpHeaders headers = getResponseHeaders(response);
            msg.append("\nheaders=").append(headers).append(' ');
        }

        if (isIncludeResponsePayload) {
            String payload = getMessagePayload(response.getContentAsByteArray());
            if (payload != null) {
                msg.append("\npayload=").append(payload);
            }
        }

        return msg.toString();
    }

    /**
     * maxPayload 에 맞게 내용 추출
     */
    protected String getMessagePayload(byte[] buf) {
        if (buf.length > 0) {
            int length = Math.min(buf.length, maxPayloadLength);
            return new String(buf, 0, length, StandardCharsets.UTF_8);
        }
        return null;
    }

    protected HttpHeaders getResponseHeaders(HttpServletResponse response) {
        HttpHeaders headers = new HttpHeaders();
        Collection<String> headerNames = response.getHeaderNames();

        if (headerNames != null) {
            headerNames.stream()
                    .distinct()
                    .forEach(name -> {
                        for (String value : response.getHeaders(name)) {
                            headers.add(name, value);
                        }
                    });
        }

        return headers;
    }

    /**
     * InputStream 재활용을 위한 wrapper
     *
     * 생성 시 inputStream 의 내용을 저장해두고 getInputStream 호출될 때마다
     * 새로운 inputStream 을 만들어 리턴한다.
     */
    static class ReusableRequestWrapper extends HttpServletRequestWrapper {

        /**
         * 재사용할 inputStream 내용
         */
        private byte[] buf;

        public ReusableRequestWrapper(HttpServletRequest request) throws IOException {
            super(request);
            buf = StreamUtils.copyToByteArray(request.getInputStream());
        }

        @Override
        public ServletInputStream getInputStream() {
            return new CustomServletInputStream(buf);
        }

        static class CustomServletInputStream extends ServletInputStream {

            private ByteArrayInputStream in;

            public CustomServletInputStream(final byte[] bytes) {
                in = new ByteArrayInputStream(bytes);
            }

            @Override
            public boolean isFinished() {
                return in.available() == 0;
            }

            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public void setReadListener(ReadListener listener) {
            }

            @Override
            public int read() throws IOException {
                return in.read();
            }
        }
    }
}
