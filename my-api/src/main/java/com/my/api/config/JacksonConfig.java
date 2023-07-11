package com.my.api.config;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import com.my.api.common.util.DateTimeUtil;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Configuration
public class JacksonConfig {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jsonCustomizer() {
        return builder -> {
            /**
             * Serialize LocalTime to 'HH:mm:ss'
             */
            builder.serializerByType(LocalTime.class, new LocalTimeSerializer(DateTimeUtil.TIME_FORMATTER));
            /**
             * Deserialize LocalTime from 'HH:mm:ss'
             */
            builder.deserializerByType(LocalTime.class, new LocalTimeDeserializer(DateTimeUtil.TIME_FORMATTER));
            /**
             * Serialize LocalDate to 'yyyy-MM-dd'
             */
            builder.serializerByType(LocalDate.class, new LocalDateSerializer(DateTimeUtil.DATE_FORMATTER));
            /**
             * Deserialize LocalDate from 'yyyy-MM-dd'
             */
            builder.deserializerByType(LocalDate.class, new LocalDateDeserializer(DateTimeUtil.DATE_FORMATTER));
            /**
             * Serialize LocalDateTime to 'yyyy-MM-dd HH:mm:ss'
             */
            builder.serializerByType(LocalDateTime.class, new LocalDateTimeSerializer(DateTimeUtil.DATE_TIME_FORMATTER));
            /**
             * Deserialize LocalDateTime from 'yyyy-MM-dd HH:mm:ss'
             */
            builder.deserializerByType(LocalDateTime.class, new LocalDateTimeDeserializer(DateTimeUtil.DATE_TIME_FORMATTER));

            /**
             * 알 수 없는 ENUM 값은 NULL 로 간주
             */
            builder.featuresToEnable(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL);

            /**
             * {@link JsonValue} 값이 숫자형의 문자열인 경우, 매칭되는 Enum 을 찾지 못하면
             * 문자열을 숫자로 변환하여 index 로 활용하여 Enum 을 찾아낸다.
             * jackson 의 기본 설정이 왜 그렇게 되어있는지 이유는 알 수 없지만
             * 개발자의 의도를 벗어나는 자동변환이므로 동작하지 않도록 아래 설정을 추가
             */
            builder.featuresToEnable(DeserializationFeature.FAIL_ON_NUMBERS_FOR_ENUMS);
        };
    }
}
