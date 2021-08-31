package com.brian.api.common.convert;

import com.brian.api.common.util.DateTimeUtil;
import org.springframework.core.convert.converter.Converter;

import java.time.LocalDateTime;

public class StringToLocalDateTimeConverter implements Converter<String, LocalDateTime> {
    @Override
    public LocalDateTime convert(String source) {
        return LocalDateTime.parse(source, DateTimeUtil.DATE_TIME_FORMATTER);
    }
}