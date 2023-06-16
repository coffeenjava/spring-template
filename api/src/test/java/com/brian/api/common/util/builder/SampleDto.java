package com.brian.api.common.util.builder;


import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.Getter;

@Getter
public class SampleDto {

    private String name;

    private String userPassword;

    private int age;

    @Builder
    public SampleDto(String userPassword, int age) {
        this.userPassword = userPassword;
        this.age = age;
    }

    public static void main(String[] args) throws Exception {
        SampleDto dto = SampleDto.builder().age(10).build();

        ObjectMapper mapper = new ObjectMapper();
        final String str = mapper.writeValueAsString(dto);
        System.out.println(str);
    }
}
