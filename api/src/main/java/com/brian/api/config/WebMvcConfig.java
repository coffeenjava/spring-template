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
         */
        registry.addConverter(new StringToLocalDateConverter());
        registry.addConverter(new StringToLocalDateTimeConverter());
        registry.addConverterFactory(new StringToEnumConverterFactory());
    }
}
