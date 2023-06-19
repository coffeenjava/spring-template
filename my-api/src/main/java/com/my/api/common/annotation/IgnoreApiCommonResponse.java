package com.my.api.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 특정 컨트롤러 전체 응답 혹은 특정 메서드의 응답을
 * <p>>공통 응답 형식 내려주지 않을 용도
 *
 * @see com.my.api.common.model.ApiCommonResponse
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface IgnoreApiCommonResponse {
}
