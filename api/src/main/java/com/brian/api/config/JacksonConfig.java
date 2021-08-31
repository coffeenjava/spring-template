package com.brian.api.config;

import com.brian.api.common.util.DateTimeUtil;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;

@Configuration
public class JacksonConfig {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jsonCustomizer() {
        return builder -> {
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
