package com.my.api.common.convert;

import com.my.api.common.util.DateTimeUtil;
import org.springframework.core.convert.converter.Converter;

import java.time.LocalTime;

/**
 * DTO 가 아닌, query 파라미터로 들어오는 LocalTime 객체 변환기
 *
 * @see DateTimeUtil#TIME_FORMATTER
 */
public class StringToLocalTimeConverter implements Converter<String, LocalTime> {
    @Override
    public LocalTime convert(String source) {
        return LocalTime.parse(source, DateTimeUtil.TIME_FORMATTER);
    }
}
