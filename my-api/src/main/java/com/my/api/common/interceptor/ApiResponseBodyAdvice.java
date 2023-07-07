package com.my.api.common.interceptor;

import com.my.api.common.annotation.IgnoreApiCommonResponse;
import com.my.api.common.model.ApiCommonResponse;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * 공통 응답 형식으로 내려주기 위한 advice
 *
 * @see ApiCommonResponse
 */
@RestControllerAdvice
public class ApiResponseBodyAdvice implements ResponseBodyAdvice {

    @Override
    public boolean supports(final MethodParameter returnType, final Class converterType) {
        // @IgnoreApiCommonResponse 선언된 클래스나 메서드는 제외
        if (returnType.getMethodAnnotation(IgnoreApiCommonResponse.class) != null ||
                returnType.getExecutable().getDeclaringClass().getAnnotation(IgnoreApiCommonResponse.class) != null) {
            return false;
        }

        return true;
    }

    @Override
    public Object beforeBodyWrite(final Object body,
                                  final MethodParameter returnType,
                                  final MediaType selectedContentType,
                                  final Class selectedConverterType,
                                  final ServerHttpRequest request,
                                  final ServerHttpResponse response) {
        // 응답이 DTO 객체인 경우에만 변환
        if (MappingJackson2HttpMessageConverter.class.isAssignableFrom(selectedConverterType)) {
            return new ApiCommonResponse(body);
        }

        // 응답이 String 인 케이스는 그냥 내려준다
        return body;
    }

}
