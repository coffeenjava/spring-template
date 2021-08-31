package com.brian.api.common.annotation;

import com.brian.api.common.util.ObjectUtil;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 값이 null 이면 copy 제외 용도
 *
 * 지정된 클래스가 있으면 해당 클래스로의 copy 시에만 해당
 * 지정하지 않을 경우 모든 클래스에 대해 해당
 *
 * @see ObjectUtil  copyProperties()
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface SkipNull {

    /**
     * 적용될 클래스 목록
     */
    Class[] value() default Void.class;
}
