package com.my.api.config.prop;

import com.my.api.common.annotation.Description;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Set;

/**
 *  요청/응답 로깅 properties
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "custom.request-response-logging")
public class RequestResponseLoggingProperties {

    @Description("로깅 사용 여부")
    private boolean enabled = true;

    @Description("로그 메시지의 줄바꿈 여부")
    private boolean lineBreak = false;

    @Description("요청/응답 body 최대 길이 (byte)")
    private int maxPayloadLength = 1024 * 1024 * 10;

    @Description("로깅 제외할 uri 패턴 문자열")
    private Set<String> excludeUri;

    private Request request = new Request();
    private Response response = new Response();

    @Description("요청 정보 필터링 옵션")
    @Getter
    @Setter
    public class Request {

        @Description("query string 포함 여부")
        private boolean queryString = true;

        @Description("header 포함 여부")
        private boolean header = true;

        @Description("body 포함 여부")
        private boolean payload = true;
    }

    @Description("응답 정보 필터링 옵션")
    @Getter
    @Setter
    public class Response {

        @Description("header 포함 여부")
        private boolean header = true;

        @Description("body 포함 여부")
        private boolean payload = true;
    }
}
