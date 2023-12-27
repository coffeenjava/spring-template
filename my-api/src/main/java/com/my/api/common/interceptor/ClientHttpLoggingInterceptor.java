package com.my.api.common.interceptor;

import com.my.api.common.annotation.Description;
import com.my.api.config.prop.ClientHttpLoggingProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.StreamUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * API 호출 요청/응답 로깅
 */
@Slf4j
public class ClientHttpLoggingInterceptor implements ClientHttpRequestInterceptor {

    private String localHost;

    @Description("로그 메시지의 줄바꿈 여부")
    private final boolean isLineBreak;

    @Description("요청/응답 body 최대 길이 (byte)")
    private final int maxResponsePayloadLength;

    @Description("요청 header 포함 여부")
    private final boolean isIncludeHeader;

    @Description("요청 body 포함 여부")
    private final boolean isIncludePayload;

    @Description("응답 header 포함 여부")
    private final boolean isIncludeResponseHeader;

    @Description("응답 body 포함 여부")
    private final boolean isIncludeResponsePayload;


    public ClientHttpLoggingInterceptor(ClientHttpLoggingProperties properties) {
        isLineBreak = properties.isLineBreak();
        maxResponsePayloadLength = properties.getMaxPayloadLength();

        ClientHttpLoggingProperties.Request request = properties.getRequest();
        isIncludeHeader = request.isHeader();
        isIncludePayload = request.isPayload();

        ClientHttpLoggingProperties.Response response = properties.getResponse();
        isIncludeResponseHeader = response.isHeader();
        isIncludeResponsePayload = response.isPayload();

        try {
            localHost = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            localHost = "unknown";
        }
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        long start = System.currentTimeMillis();

        StringBuilder sb = new StringBuilder();
        sb.append("[Api Request/Response]").append(' ');
        sb.append("{response-status}").append(' ');
        sb.append(request.getMethod()).append(' ');
        sb.append(request.getURI());
        sb.append(" ({execution-time}ms)").append(' ');
        sb.append("\nlocal-host=").append(localHost).append(' ');

        if (isIncludeHeader) {
            sb.append("\nheaders=").append(request.getHeaders()).append(' ');
        }

        if (isIncludePayload && body != null && body.length > 0) {
            sb.append("\npayload=").append(new String(body, StandardCharsets.UTF_8)).append(' ');
        }

        String status = "ERROR";
        ReusableBodyHttpResponse reusableResponse;

        try {
            ClientHttpResponse response = execution.execute(request, body);
            reusableResponse = new ReusableBodyHttpResponse(response);

            if (isIncludeResponseHeader) {
                sb.append("\nresponse-headers=").append(reusableResponse.getHeaders()).append(' ');
            }

            if (isIncludeResponsePayload && reusableResponse.hasBody()) {
                byte[] bodyBytes = StreamUtils.copyToByteArray(reusableResponse.getBody());
                int bodySize = Math.min(bodyBytes.length, maxResponsePayloadLength);
                String responseBody = new String(bodyBytes, 0, bodySize, StandardCharsets.UTF_8);
                sb.append("\nresponse-payload=").append(responseBody);
            }

            status = String.valueOf(reusableResponse.getRawStatusCode());

        } catch (Exception e) {
            // Todo error 는 이미 남을 텐데 여기에 포함시킬 필요가 있을까? 차후 불필요하면 제거하자.
            sb.append("\nerror-message=").append(e);
            throw e;
        } finally {
            String logMessage = sb.toString();
            final String executionTime = String.valueOf(System.currentTimeMillis() - start);

            logMessage = logMessage.replaceFirst("\\{response-status\\}",status)
                    .replaceFirst("\\{execution-time\\}", executionTime);

            if (isLineBreak == false) {
                logMessage = logMessage.replaceAll("(\\r|\\n)","");
            }

            log.info(logMessage);
        }

        return reusableResponse;
    }

    /**
     * InputStream 재활용을 위한 wrapper
     */
    private static class ReusableBodyHttpResponse implements ClientHttpResponse {
        private final ClientHttpResponse response;
        private byte[] body;

        public ReusableBodyHttpResponse(ClientHttpResponse response) {
            this.response = response;

            try {
                InputStream bodyStream = response.getBody();

                if (Objects.nonNull(bodyStream)) {
                    body = StreamUtils.copyToByteArray(bodyStream);
                }
            } catch (IOException e) {
            }
        }

        public boolean hasBody() {
            return Objects.nonNull(body);
        }

        @Override
        public InputStream getBody() throws IOException {
            return new ByteArrayInputStream(body);
        }

        @Override
        public HttpHeaders getHeaders() {
            return response.getHeaders();
        }

        @Override
        public HttpStatusCode getStatusCode() throws IOException {
            return response.getStatusCode();
        }

        @Override
        public int getRawStatusCode() throws IOException {
            return response.getRawStatusCode();
        }

        @Override
        public String getStatusText() throws IOException {
            return response.getStatusText();
        }

        @Override
        public void close() {
            response.close();
        }
    }
}
