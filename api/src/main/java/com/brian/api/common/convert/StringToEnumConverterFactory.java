package com.brian.api.common.convert;

import com.brian.api.common.model.BaseEnum;
import com.brian.api.config.WebMvcConfig;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;

import java.util.Objects;

/**
 * 컨트롤러 메서드에 @RequestParam 으로 정의된 파라미터의 경우
 * Jackson 컨퍼터가 아닌 Spring 컨버터(ConverterFactory 구현체)가 동작하여 구현
 *
 * @see WebMvcConfig FormatterRegistry 에 등록해야함
 */
public class StringToEnumConverterFactory implements ConverterFactory<String, BaseEnum> {

    @Override
    public <T extends BaseEnum> Converter<String, T> getConverter(Class<T> targetType) {
        return new StringToEnumConverter(targetType);
    }

    private static class StringToEnumConverter<T extends BaseEnum> implements Converter<String, T> {

        private Class<T> enumType;

        public StringToEnumConverter(Class<T> enumType) {
            this.enumType = enumType;
        }

        public T convert(String source) {
            if (Objects.isNull(source)) return null;

            return BaseEnum.getEnum(enumType, source);
        }
    }
}
