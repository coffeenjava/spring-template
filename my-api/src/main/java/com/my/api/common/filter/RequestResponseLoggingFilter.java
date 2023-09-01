package com.my.api.common.filter;

import com.my.api.common.annotation.Description;
import com.my.api.common.util.WebUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
        // Multipart 는 로깅 제외
        if (isMultipartFormData(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        final boolean isFirstRequest = !isAsyncDispatch(request);
        HttpServletRequest requestToUse = request;

        if (isFirstRequest) {
            // 재사용 가능한 요청으로 래핑
            requestToUse = new ReusableRequestWrapper(request);

            // 요청 정보 생성
            String requestInfo = createRequestInfo(requestToUse);

            if (isLineBreak == false) {
                requestInfo = requestInfo.replaceAll("(\\r|\\n)", "");
            }

            // 요청 정보 로깅
            log.info(requestInfo);
        }

        // 재사용 가능한 응답으로 래핑
        final ContentCachingResponseWrapper cachingResponse = new ContentCachingResponseWrapper(response);

        // 필터 체인 호출 + 실행시간 계산
        long start = System.currentTimeMillis();
        filterChain.doFilter(requestToUse, cachingResponse);
        final long executionTime = System.currentTimeMillis() - start;

        if (isFirstRequest) {
            // 응답 정보 생성
            String responseInfo = createResponseInfo(requestToUse, cachingResponse, executionTime);

            if (isLineBreak == false) {
                responseInfo = responseInfo.replaceAll("(\\r|\\n)", "");
            }

            // 응답 로깅
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
        return request.getContentType() != null
                && request.getContentType().contains(MediaType.MULTIPART_FORM_DATA_VALUE);
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
                String payload = getMessagePayload(buf, request.getCharacterEncoding());

                if (payload != null) {
                    // form post 요청이면 url decoding
                    if (WebUtils.isFormPost(request)) {
                        payload = URLDecoder.decode(payload, request.getCharacterEncoding());
                    }

                    payload = payload.replaceAll("(\\r|\\n)","");
                    msg.append("\npayload=").append(payload).append(' ');
                }
            } catch (Exception e) {
                log.warn("request body parsing 실패", e);
            }
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
            // 응답 payload 는 항상 UTF-8 로 파싱
            String payload = getMessagePayload(response.getContentAsByteArray(), StandardCharsets.UTF_8.name());
            if (payload != null) {
                msg.append("\npayload=").append(payload);
            }
        }

        return msg.toString();
    }

    /**
     * 최대 로깅 길이만큼만 내용 추출
     */
    protected String getMessagePayload(byte[] buf, String characterEncoding) {
        if (buf.length > 0) {
            int length = Math.min(buf.length, maxPayloadLength);

            Charset charset;

            try {
                charset = Charset.forName(characterEncoding);
            } catch (Exception e) {
                charset = StandardCharsets.UTF_8;
            }

            return new String(buf, 0, length, charset);
        }
        return null;
    }

    /**
     * 응답 헤더 추출
     */
    protected HttpHeaders getResponseHeaders(HttpServletResponse response) {
        HttpHeaders headers = new HttpHeaders();

        List<String> headerNameList = Optional.ofNullable(response.getHeaderNames())
                .stream()
                .flatMap(names -> names.stream())
                .distinct()
                .collect(Collectors.toList());

        for (String name : headerNameList) {
            for (String value : response.getHeaders(name)) {
                headers.add(name, value);
            }
        }

        return headers;
    }

    /**
     * Form & Post 요청 여부
     */
    protected static boolean isFormPost(HttpServletRequest request) {
        String contentType = request.getContentType();
        return (contentType != null && contentType.contains(MediaType.APPLICATION_FORM_URLENCODED_VALUE) &&
                HttpMethod.POST.matches(request.getMethod()));
    }

    /**
     * InputStream 재활용을 위한 wrapper
     *
     * 생성 시 inputStream 의 내용을 저장해두고 getInputStream 호출될 때마다
     * 새로운 inputStream 을 만들어 리턴한다.
     */
    static class ReusableRequestWrapper extends HttpServletRequestWrapper {

        /**
         * 재사용할 inputStream buffer
         */
        private byte[] buf;

        private final String characterEncoding;

        /**
         * Form Post 요청(application/x-www-form-urlencoded) 일 경우 body 파라미터를 담아둘 map
         */
        private Map<String, List<String>> parameterMap;


        public ReusableRequestWrapper(HttpServletRequest request) throws IOException {
            super(request);
            buf = StreamUtils.copyToByteArray(request.getInputStream());
            characterEncoding = StringUtils.hasText(request.getCharacterEncoding())
                    ? request.getCharacterEncoding() : StandardCharsets.UTF_8.name();
        }

        @Override
        public ServletInputStream getInputStream() {
            return new CustomServletInputStream(buf);
        }

        /**
         * Form Post 요청의 body 파라미터를 꺼내올 수 있도록 하기 위해 오버라이드
         *
         * <p>관련 내용 참고: https://jira-hanatour.atlassian.net/browse/MGTT-8466
         */
        @Override
        public String[] getParameterValues(String name) {
            String[] parameterValues = super.getParameterValues(name);

            if (isFormPost(this) == false) {
                return parameterValues;
            }

            if (parameterMap == null) {
                parseFormParameter();
            }

            List<String> paramList = parameterMap.get(name);

            if (CollectionUtils.isEmpty(paramList)) {
                return parameterValues;
            }

            if (parameterValues != null) {
                for (String parameterValue : parameterValues) {
                    paramList.add(parameterValue);
                }
            }

            return paramList.toArray(String[]::new);
        }

        /**
         * query string URLDecoding 을 위해 오버라이드
         */
        @Override
        public String getQueryString() {
            String queryString = super.getQueryString();

            if (StringUtils.hasText(queryString)) {
                try {
                    queryString = URLDecoder.decode(queryString, characterEncoding);
                } catch (UnsupportedEncodingException e) {
                }
            }

            return queryString;
        }

        @Override
        public String getCharacterEncoding() {
            return characterEncoding;
        }

        /**
         * Form Post 요청의 body 파라미터를 map 에 담아둔다.
         */
        private void parseFormParameter() {
            try {
                parameterMap = new HashMap<>();
                String payload = new String(buf, characterEncoding);

                // name=b&age=1,5&name=c,d
                if (StringUtils.hasText(payload)) {
                    String[] split = payload.split("&");

                    for (String param: split) {
                        String[] keyValue = param.split("=");
                        if (keyValue.length == 2) {
                            String key = URLDecoder.decode(keyValue[0], characterEncoding);
                            if (parameterMap.get(key) == null) {
                                parameterMap.put(key, new ArrayList<>());
                            }

                            String[] values = keyValue[1].split(",");
                            for (String value : values) {
                                value = URLDecoder.decode(value, characterEncoding);
                                parameterMap.get(key).add(value);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("post form body parsing 실패", e);
            }
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
