package com.my.api.common.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;

@Component
public class CustomObjectMapper {

    @Autowired
    private ObjectMapper mapper;

    public String writeInputStreamAsPrettyString(InputStream is) {
        Object value = readValue(is, Object.class);
        if (value == null) return null;
        return writeValueAsString(value);
    }

    public String writeValueAsString(Object o) {
        try {
            return mapper.writeValueAsString(o);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Object 컨버팅 오류 발생", e);
        }
    }

    public <T> T readValue(String content, Class<T> valueType) {
        try {
            return mapper.readValue(content, valueType);
        } catch (IOException e) {
            throw new RuntimeException("Object 컨버팅 오류 발생", e);
        }
    }

    private  <T> T readValue(InputStream src, Class<T> valueType) {
        try {
            return mapper.readValue(src, valueType);
        } catch (JsonProcessingException pe) {
            /**
             * json 형태가 아닌 경우 에러 발생시키지 않는다.
             */
            return null;
        } catch (IOException e) {
            throw new RuntimeException("Object 컨버팅 오류 발생", e);
        }
    }

    public String writeValueAsPrettyString(Object o) {
        try {
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(o);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Object 컨버팅 오류 발생", e);
        }
    }
}
