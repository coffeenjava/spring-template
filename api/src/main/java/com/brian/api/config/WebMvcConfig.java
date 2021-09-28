package com.brian.api.config;

import com.brian.api.common.convert.StringToEnumConverterFactory;
import com.brian.api.common.convert.StringToLocalDateConverter;
import com.brian.api.common.convert.StringToLocalDateTimeConverter;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addFormatters(FormatterRegistry registry) {
        /**
         * request parameter converters
         * springboot 에서는 converter 를 bean 으로 선언만 해도 동작하나
         * 어떤 커스텀 컨버터들이 사용되는지 명시적으로 알 수 있도록 등록한다.
         */
        registry.addConverter(new StringToLocalDateConverter());
        registry.addConverter(new StringToLocalDateTimeConverter());
        registry.addConverterFactory(new StringToEnumConverterFactory());
    }
}
