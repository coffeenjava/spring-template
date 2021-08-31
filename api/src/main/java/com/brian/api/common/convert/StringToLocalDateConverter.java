package com.brian.api.common.convert;

import com.brian.api.common.util.DateTimeUtil;
import org.springframework.core.convert.converter.Converter;

import java.time.LocalDate;

public class StringToLocalDateConverter implements Converter<String, LocalDate> {
    @Override
    public LocalDate convert(String source) {
        return LocalDate.parse(source, DateTimeUtil.DATE_FORMATTER);
    }
}