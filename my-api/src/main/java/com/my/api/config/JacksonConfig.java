package com.my.api.config;

import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.my.api.common.util.DateTimeUtil;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Configuration
public class JacksonConfig {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jsonCustomizer() {
        return builder -> {
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
        };
    }
}
