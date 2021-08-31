package com.brian.api.common.annotation;


import com.brian.api.common.interceptor.ControllerInterceptor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 컨트롤러 진입시 검증이 필요한 요청용
 *
 * @see ControllerInterceptor
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ControllerValidation {
    Class[] value();
}
