package com.brian.api.common.interceptor;

import com.brian.api.common.model.BaseRequest;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdviceAdapter;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * request body 공통 선처리
 * 지정된 패키지 내의 컨트롤러에만 동작
 */
@ControllerAdvice("com.brian.api.controller")
public class RequestBodyInterceptor extends RequestBodyAdviceAdapter {

    /**
     * BaseDto or Collection<BaseDto>
     */
    @Override
    public boolean supports(MethodParameter methodParameter, Type type, Class<? extends HttpMessageConverter<?>> aClass) {
        Class cls = methodParameter.getParameterType();
        if (type instanceof ParameterizedType) {
            cls =  (Class) ((ParameterizedType) type).getActualTypeArguments()[0];
        }
        return BaseRequest.class.isAssignableFrom(cls);
    }

    @Override
    public Object afterBodyRead(Object body, HttpInputMessage inputMessage, MethodParameter parameter, Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
        return super.afterBodyRead(body, inputMessage, parameter, targetType, converterType);
    }
}
