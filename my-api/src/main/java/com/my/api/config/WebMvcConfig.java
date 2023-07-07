package com.my.api.config;

import com.my.api.common.convert.StringToEnumConverterFactory;
import com.my.api.common.convert.StringToLocalDateConverter;
import com.my.api.common.convert.StringToLocalDateTimeConverter;
import com.my.api.common.convert.StringToLocalTimeConverter;
import com.my.api.common.interceptor.ApiResponseBodyAdvice;
import com.my.api.common.model.ApiCommonResponse;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.MethodParameter;
import org.springframework.format.FormatterRegistry;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.mvc.method.annotation.AbstractMessageConverterMethodProcessor;
import org.springframework.web.servlet.mvc.method.annotation.RequestResponseBodyMethodProcessor;

import java.util.List;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addFormatters(FormatterRegistry registry) {
        /**
         * request parameter converters
         * springboot 에서는 converter 를 bean 으로 선언만 해도 동작하나
         * 어떤 커스텀 컨버터들이 사용되는지 명시적으로 알 수 있도록 등록한다.
         */
        registry.addConverter(new StringToLocalTimeConverter());
        registry.addConverter(new StringToLocalDateConverter());
        registry.addConverter(new StringToLocalDateTimeConverter());
        registry.addConverterFactory(new StringToEnumConverterFactory());
    }

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        /**
         * Jackson 컨버터를 최상위로 설정
         * 이 설정이 없으면 Controller 응답값을 String 으로 구현하였을 때 오류 발생한다.
         *
         * why?
         * 1. {@link RequestResponseBodyMethodProcessor} 에서 응답을 만들 때 converters 를 순차적으로 찾는다.
         *    실제 메서드는 상위 클래스에 위치. {@link AbstractMessageConverterMethodProcessor#writeWithMessageConverters(Object, MethodParameter, ServletServerHttpRequest, ServletServerHttpResponse)}
         * 2. String 타입이므로 {@link StringHttpMessageConverter#supports(Class)} 가 true. converter 선정 완료
         * 3. {@link ApiResponseBodyAdvice} 가 응답 String 을 공통 응답형식인 {@link ApiCommonResponse} 로 wrapping
         * 4. AbstractMessageConverterMethodProcessor 에서 StringHttpMessageConverter 로 변환 시도
         * 5. ApiCommonResponse 는 String 타입이 아니므로 casting 오류 발생
         */
        for (int i = 0; i < converters.size(); i++) {
            if (converters.get(i) instanceof MappingJackson2HttpMessageConverter) {
                converters.add(0, converters.remove(i));
                break;
            }
        }
    }
}
