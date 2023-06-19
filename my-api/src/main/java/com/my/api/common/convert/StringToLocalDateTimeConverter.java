package com.my.api.common.convert;

import com.my.api.common.util.DateTimeUtil;
import org.springframework.core.convert.converter.Converter;

import java.time.LocalDateTime;

/**
 * DTO 가 아닌, query 파라미터로 들어오는 LocalDateTime 객체 변환기
 *
 * @see DateTimeUtil#DATE_TIME_FORMATTER
 */
public class StringToLocalDateTimeConverter implements Converter<String, LocalDateTime> {
    @Override
    public LocalDateTime convert(String source) {
        return LocalDateTime.parse(source, DateTimeUtil.DATE_TIME_FORMATTER);
    }
}
