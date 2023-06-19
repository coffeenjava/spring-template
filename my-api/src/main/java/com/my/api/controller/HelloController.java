package com.my.api.controller;

import com.my.api.common.consts.Gender;
import com.my.api.common.consts.YesNo;
import com.my.api.common.exception.ApiRuntimeException;
import com.my.api.common.exception.message.CommonErrorCode;
import com.my.api.common.model.BaseRequest;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;

@RequestMapping("/hello")
@RestController
public class HelloController {

    @GetMapping
    public String hello(@RequestParam String name) {
        return "hello. "+name;
    }

    @GetMapping("/dto")
    public SampleResponseDto hello(@Valid @ModelAttribute SampleRequestDto request) {
        System.out.println(request);
        return new SampleResponseDto(LocalDateTime.now(), request.getName(), request.isAdult());
    }

    @PostMapping
    public SampleResponseDto save(@RequestParam LocalDate date, @RequestParam Gender gender, @Valid @RequestBody SampleRequestDto request) {
        System.out.println(date);
        System.out.println(gender);
        System.out.println(request);

        return new SampleResponseDto(LocalDateTime.now(), request.getName(), request.isAdult());
    }

    @Getter
    @Setter
    @ToString
    static class SampleRequestDto implements BaseRequest {

        @NotNull
        Integer age;

        @NotBlank
        String name;

        String hobby;

        boolean isAdult;

        /**
         * validate() 호출 전 기본 데이터 설정 등
         */
        @Override
        public void beforeValidate() {
            isAdult = age >= 18;
        }

        /**
         * 필드 관계 비지니스 검증
         */
        @Override
        public void validate() {
            if (isAdult && hobby == null) {
                throw new ApiRuntimeException(CommonErrorCode.INVALID_REQUEST_PARAM, "성인은 취미를 가져야 합니다.");
            }
        }
    }

    @Getter
    @AllArgsConstructor
    static class SampleResponseDto {
        LocalDateTime requestTime;
        String yourName;
        Boolean isAdult;
    }
}
