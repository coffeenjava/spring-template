package com.brian.api;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.format.DateTimeFormatter;

@Getter
@Setter
public class TestDto {
    @JsonIgnore
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("YYYY-MM-DD");

//    @JsonIgnore
    private String value = "hi";

    public static void main(String[] args) {
        ObjectMapper mapper = new ObjectMapper();
//        String str = "{\"value\":\"hello\"}";
        String str = "{\"dtf\":{\"locale\":\"ko_KR\",\"decimalStyle\":{\"zeroDigit\":\"0\",\"positiveSign\":\"+\",\"negativeSign\":\"-\",\"decimalSeparator\":\".\"},\"resolverStyle\":\"SMART\",\"resolverFields\":null,\"zone\":null,\"chronology\":null}}";
        try {
            TestDto dto = mapper.readValue(str, TestDto.class);
            System.out.println(dto);
            String dtoStr = mapper.writeValueAsString(dto);
            System.out.println(dtoStr);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }
}
